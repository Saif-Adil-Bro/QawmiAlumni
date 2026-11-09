package com.qawmialumni.app.presentation.chat

import com.qawmialumni.app.domain.model.ChatMessage
import com.qawmialumni.app.domain.model.ChatRoom
import com.qawmialumni.app.domain.model.MessageStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * WhatsApp-style tick ladder:
 * SENDING (clock) -> SENT (one grey tick) -> DELIVERED (two grey ticks) -> READ (two blue ticks).
 */
class ChatTickStatusTest {

    private val me = "uid_me"
    private val peer = "uid_peer"
    private val room = ChatRoom(roomId = "uid_me_uid_peer", participants = listOf(me, peer))

    private fun msg(ts: Long, status: MessageStatus = MessageStatus.SENT) = ChatMessage(
        messageId = "m_$ts",
        senderId = me,
        text = "আসসালামু আলাইকুম",
        timestamp = ts,
        status = status,
    )

    @Test
    fun `pending message shows sending indicator`() {
        assertEquals(
            MessageStatus.SENDING,
            room.tickStatusFor(msg(1000L, MessageStatus.SENDING), peer),
        )
    }

    @Test
    fun `written message shows single grey tick before the peer syncs`() {
        assertEquals(MessageStatus.SENT, room.tickStatusFor(msg(1000L), peer))
    }

    @Test
    fun `peer sync turns the tick into double grey`() {
        val delivered = room.copy(lastDeliveredAt = mapOf(peer to 2000L))
        assertEquals(MessageStatus.DELIVERED, delivered.tickStatusFor(msg(1000L), peer))
    }

    @Test
    fun `peer opening the room turns the tick blue`() {
        val read = room.copy(
            lastDeliveredAt = mapOf(peer to 2000L),
            lastReadAt = mapOf(peer to 3000L),
        )
        assertEquals(MessageStatus.READ, read.tickStatusFor(msg(1000L), peer))
        // A message sent after the peer's last sync is still just "sent".
        assertEquals(MessageStatus.SENT, read.tickStatusFor(msg(3500L), peer))
    }

    @Test
    fun `deleted messages render as a placeholder instead of text`() {
        val deleted = msg(1000L).copy(text = "", isDeleted = true)
        assertTrue(deleted.isDeletedPlaceholder)
    }

    @Test
    fun `room id is stable regardless of participant order`() {
        assertEquals(ChatRoom.buildRoomId(me, peer), ChatRoom.buildRoomId(peer, me))
    }
}
