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
) : ViewModel() {

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

    // NOTE: incoming messages are no longer detected by diffing chat timestamps in the
    // sender's process (that notified the sender himself). Delivery is handled centrally by
    // NotificationHelper's recipient-addressed Firestore listener.

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
            val session = dataStore.userSession.first()
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
