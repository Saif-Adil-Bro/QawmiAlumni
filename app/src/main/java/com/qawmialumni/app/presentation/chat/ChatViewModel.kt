package com.qawmialumni.app.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.ChatRoom
import com.qawmialumni.app.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    val dataStore: UserPreferencesDataStore,
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
}
