package com.qawmialumni.app.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.ChatMessage
import com.qawmialumni.app.domain.model.ChatRoom
import com.qawmialumni.app.domain.model.MessageStatus
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.repository.CommunityRepository
import com.qawmialumni.app.data.local.dao.AlumniDao
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.data.local.entity.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    val dataStore: UserPreferencesDataStore,
    private val alumniDao: AlumniDao,
) : ViewModel() {

    /**
     * Messages that are still uploading. They are rendered immediately at the bottom of the
     * chat with a clock/"sending" indicator, exactly like WhatsApp, and disappear from here
     * once Firestore has acknowledged them (the room listener then shows the grey tick).
     */
    private val _pendingMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val pendingMessages: StateFlow<Map<String, List<ChatMessage>>> = _pendingMessages.asStateFlow()

    val chatRooms: StateFlow<Resource<List<ChatRoom>>> = dataStore.userSession
        .flatMapLatest { session ->
            if (session.uid.isBlank()) {
                flowOf(Resource.Success(emptyList()))
            } else {
                communityRepository.getChatRooms(session.uid)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading,
        )

    /**
     * Rooms for which this device already wrote its DELIVERED watermark, so the listener
     * does not spam Firestore with the same write on every snapshot.
     */
    private val deliveredMarked = mutableSetOf<String>()

    fun observeRoom(roomId: String): StateFlow<Resource<ChatRoom>> {
        return communityRepository.observeChatRoom(roomId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Resource.Loading,
            )
    }

    /** Optimistic send: bubble appears instantly as "sending", then becomes "sent". */
    fun sendMessage(
        roomId: String,
        recipientId: String,
        recipientName: String,
        recipientPhoto: String,
        text: String,
        onSendFailed: ((String) -> Unit)? = null,
    ) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val session = dataStore.userSession.first()
            if (session.uid.isBlank()) return@launch

            val messageId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()
            val pending = ChatMessage(
                messageId = messageId,
                senderId = session.uid,
                text = trimmed,
                timestamp = timestamp,
                status = MessageStatus.SENDING,
            )
            _pendingMessages.update { current ->
                current + (roomId to (current[roomId].orEmpty() + pending))
            }

            val result = communityRepository.sendMessage(
                roomId = roomId,
                madrasaId = session.madrasaId,
                senderId = session.uid,
                recipientId = recipientId,
                senderName = session.name,
                recipientName = recipientName,
                senderPhoto = session.photoUrl,
                recipientPhoto = recipientPhoto,
                text = trimmed,
                messageId = messageId,
                timestamp = timestamp,
            )

            if (result is Resource.Error) {
                // Send failed → drop the optimistic bubble and let the UI report the error.
                _pendingMessages.update { current ->
                    val remaining = current[roomId].orEmpty().filterNot { it.messageId == messageId }
                    if (remaining.isEmpty()) current - roomId else current + (roomId to remaining)
                }
                onSendFailed?.invoke(result.message)
            } else {
                // Firestore acknowledged → remove the optimistic copy; the room listener
                // now renders it with a SENT tick. Small delay avoids a visible flicker.
                delay(250)
                _pendingMessages.update { current ->
                    val remaining = current[roomId].orEmpty().filterNot { it.messageId == messageId }
                    if (remaining.isEmpty()) current - roomId else current + (roomId to remaining)
                }
            }
        }
    }

    /** Called when the user opens a room: marks it read → sender's ticks turn blue. */
    fun markRoomRead(roomId: String, readerUid: String) {
        if (roomId.isBlank() || readerUid.isBlank()) return
        viewModelScope.launch { communityRepository.markRoomRead(roomId, readerUid) }
    }

    /** Room list arrived on this device → the sender should see double grey ticks. */
    fun markRoomDelivered(roomId: String, recipientUid: String) {
        if (roomId.isBlank() || recipientUid.isBlank()) return
        if (!deliveredMarked.add(roomId)) return
        viewModelScope.launch {
            val res = communityRepository.markRoomDelivered(roomId, recipientUid)
            if (res is Resource.Error) deliveredMarked.remove(roomId)
        }
    }

    /** WhatsApp-style "delete for everyone" (soft delete keeps the timeline intact). */
    fun deleteMessage(
        roomId: String,
        messageId: String,
        messageTimestamp: Long = 0L,
        onResult: (Resource<Unit>) -> Unit = {},
    ) {
        viewModelScope.launch {
            val session = dataStore.userSession.first()
            val result = communityRepository.deleteMessage(roomId, messageId, session.uid, messageTimestamp)
            onResult(result)
        }
    }

    fun getUserProfile(uid: String): Flow<UserProfile?> {
        return alumniDao.observeByUid(uid).map { it?.toDomain() }
    }

    /** Unread badge helper used by the room list (peer's message after my last read). */
    fun unreadCountFor(room: ChatRoom, myUid: String): Int {
        val myLastRead = room.lastReadAtOf(myUid)
        return room.recentMessages.count { it.senderId != myUid && it.timestamp > myLastRead }
    }
}
