package com.qawmialumni.app.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.data.local.dao.AlumniDao
import com.qawmialumni.app.data.local.entity.toDomain
import com.qawmialumni.app.domain.model.ChatRoom
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    val dataStore: UserPreferencesDataStore,
    private val alumniDao: AlumniDao,
    private val notificationHelper: com.qawmialumni.app.core.notification.NotificationHelper,
) : ViewModel() {

    private val seenRoomTimestamps = mutableMapOf<String, Long>()

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

    init {
        viewModelScope.launch {
            chatRooms.collect { res ->
                if (res is Resource.Success) {
                    val currentUid = runCatching { dataStore.userSession.first().uid }.getOrDefault("")
                    res.data.forEach { room ->
                        val lastSeen = seenRoomTimestamps[room.roomId]
                        if (lastSeen != null && room.lastTimestamp > lastSeen) {
                            val lastMsg = room.recentMessages.lastOrNull()
                            if (lastMsg != null && lastMsg.senderId != currentUid && currentUid.isNotBlank()) {
                                val otherName = room.participantNames.entries.firstOrNull { it.key != currentUid }?.value
                                    ?: "সহপাঠী"
                                notificationHelper.showChatMessageNotification(
                                    senderName = otherName,
                                    messageText = lastMsg.text,
                                    roomId = room.roomId,
                                )
                            }
                        }
                        seenRoomTimestamps[room.roomId] = room.lastTimestamp
                    }
                }
            }
        }
    }

    fun observeRoom(roomId: String): StateFlow<Resource<ChatRoom>> {
        return communityRepository.observeChatRoom(roomId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Resource.Loading,
            )
    }

    fun sendMessage(
        roomId: String,
        recipientId: String,
        recipientName: String,
        recipientPhoto: String,
        text: String,
    ) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val session = dataStore.userSession.stateIn(viewModelScope).value
            communityRepository.sendMessage(
                roomId = roomId,
                madrasaId = session.madrasaId,
                senderId = session.uid,
                recipientId = recipientId,
                senderName = session.name,
                recipientName = recipientName,
                senderPhoto = session.photoUrl,
                recipientPhoto = recipientPhoto,
                text = text.trim(),
            )
        }
    }

    fun getUserProfile(uid: String): Flow<UserProfile?> {
        return alumniDao.observeByUid(uid).map { it?.toDomain() }
    }
}
