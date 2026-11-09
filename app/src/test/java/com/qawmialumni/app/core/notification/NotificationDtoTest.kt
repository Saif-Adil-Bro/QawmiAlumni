package com.qawmialumni.app.core.notification

import com.qawmialumni.app.data.remote.dto.NotificationDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Guards the notification addressing contract:
 * a notification always belongs to ONE recipient and never to the actor himself.
 */
class NotificationDtoTest {

    @Test
    fun `chat notification is addressed to the recipient and records the sender`() {
        val dto = NotificationDto(
            recipientUid = "user_b",
            senderUid = "user_a",
            senderName = "মাওলানা আব্দুল্লাহ",
            type = NotificationType.CHAT.name,
            title = "নতুন বার্তা: মাওলানা আব্দুল্লাহ",
            message = "আসসালামু আলাইকুম",
            targetId = "user_a_user_b",
            createdAt = 1_700_000_000_000L,
        )

        assertEquals("user_b", dto.recipientUid)
        assertNotEquals(dto.recipientUid, dto.senderUid)
        assertEquals("CHAT", dto.type)
        assertEquals("user_a_user_b", dto.targetId)
    }

    @Test
    fun `notification collection name is stable`() {
        assertEquals("notifications", NotificationDto.COLLECTION)
    }

    @Test
    fun `in-app notification keeps the firestore document id for read delete sync`() {
        val event = InAppNotification(
            id = "doc_123",
            type = NotificationType.ADMIN,
            title = "প্রোফাইল হালনাগাদ হয়েছে",
            message = "নাম পরিবর্তন করা হয়েছে",
            targetId = "user_a",
            recipientUid = "admin_1",
        )
        assertEquals("doc_123", event.id)
        assertEquals(NotificationType.ADMIN, event.type)
        assertEquals("admin_1", event.recipientUid)
    }
}
