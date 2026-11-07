package com.qawmialumni.app.presentation.madrasaadmin

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.Notice
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.Ustad
import com.qawmialumni.app.domain.repository.AdminRepository
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

sealed interface ActionState {
    data object Idle : ActionState
    data object Loading : ActionState
    data class Success(val message: String) : ActionState
    data class Error(val message: String) : ActionState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MadrasaAdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    val dataStore: UserPreferencesDataStore,
) : ViewModel() {

    val currentSession = dataStore.userSession.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()

    // Realtime Pending Join Requests for the admin's madrasa
    val pendingRequests: StateFlow<Resource<List<UserProfile>>> = dataStore.userSession
        .flatMapLatest { session ->
            if (session.madrasaId.isNotBlank()) {
                adminRepository.getPendingJoinRequests(session.madrasaId)
            } else {
                flowOf(Resource.Success(emptyList()))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading,
        )

    // Realtime Ustads for the admin's madrasa
    val ustadsList: StateFlow<Resource<List<Ustad>>> = dataStore.userSession
        .flatMapLatest { session ->
            if (session.madrasaId.isNotBlank()) {
                adminRepository.getUstads(session.madrasaId)
            } else {
                flowOf(Resource.Success(emptyList()))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading,
        )

    // Realtime Notices for the admin's madrasa
    val noticesList: StateFlow<Resource<List<Notice>>> = dataStore.userSession
        .flatMapLatest { session ->
            if (session.madrasaId.isNotBlank()) {
                adminRepository.getNotices(session.madrasaId)
            } else {
                flowOf(Resource.Success(emptyList()))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading,
        )

    fun approveStudent(userId: String) {
        val madrasaId = currentSession.value?.madrasaId ?: return
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            when (val res = adminRepository.approveJoinRequest(userId, madrasaId)) {
                is Resource.Success -> _actionState.value = ActionState.Success("আবেদন সফলভাবে অনুমোদন করা হয়েছে")
                is Resource.Error -> _actionState.value = ActionState.Error(res.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun rejectStudent(userId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            when (val res = adminRepository.rejectJoinRequest(userId)) {
                is Resource.Success -> _actionState.value = ActionState.Success("আবেদন বাতিল করা হয়েছে")
                is Resource.Error -> _actionState.value = ActionState.Error(res.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun addUstad(
        name: String,
        designation: String,
        specialization: String,
        bio: String,
        photoUri: Uri?,
    ) {
        val madrasaId = currentSession.value?.madrasaId ?: return
        if (name.isBlank() || designation.isBlank()) {
            _actionState.value = ActionState.Error("উস্তাদের নাম ও পদবী আবশ্যক")
            return
        }

        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val ustad = Ustad(
                madrasaId = madrasaId,
                name = name.trim(),
                designation = designation.trim(),
                specialization = specialization.trim(),
                bio = bio.trim(),
                isActive = true,
            )
            when (val res = adminRepository.addUstad(ustad, photoUri)) {
                is Resource.Success -> _actionState.value = ActionState.Success("উস্তাদ যুক্ত করা হয়েছে")
                is Resource.Error -> _actionState.value = ActionState.Error(res.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun postNotice(
        title: String,
        details: String,
        eventDate: String,
        imageUri: Uri?,
    ) {
        val session = currentSession.value ?: return
        if (title.isBlank() || details.isBlank()) {
            _actionState.value = ActionState.Error("নোটিশের শিরোনাম ও বিবরণ আবশ্যক")
            return
        }

        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val notice = Notice(
                madrasaId = session.madrasaId,
                title = title.trim(),
                details = details.trim(),
                eventDate = eventDate.trim(),
                createdByUid = session.uid,
            )
            when (val res = adminRepository.postNotice(notice, imageUri)) {
                is Resource.Success -> _actionState.value = ActionState.Success("নোটিশ প্রকাশিত হয়েছে")
                is Resource.Error -> _actionState.value = ActionState.Error(res.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun resetActionState() {
        _actionState.value = ActionState.Idle
    }
}
