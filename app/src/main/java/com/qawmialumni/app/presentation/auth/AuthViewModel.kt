package com.qawmialumni.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val user: UserProfile) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    val dataStore: UserPreferencesDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("ইমেইল ও পাসওয়ার্ড প্রদান করুন")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val res = authRepository.signInWithEmail(email, pass)) {
                is Resource.Success -> _uiState.value = AuthUiState.Success(res.data)
                is Resource.Error -> _uiState.value = AuthUiState.Error(res.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String, name: String, phone: String) {
        if (email.isBlank() || pass.length < 6 || name.isBlank()) {
            _uiState.value = AuthUiState.Error("সঠিক তথ্য ও কমপক্ষে ৬ অক্ষরের পাসওয়ার্ড দিন")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val res = authRepository.signUpWithEmail(email, pass, name, phone)) {
                is Resource.Success -> _uiState.value = AuthUiState.Success(res.data)
                is Resource.Error -> _uiState.value = AuthUiState.Error(res.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun signInWithGoogleToken(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val res = authRepository.signInWithGoogleCredential(idToken)) {
                is Resource.Success -> _uiState.value = AuthUiState.Success(res.data)
                is Resource.Error -> _uiState.value = AuthUiState.Error(res.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = AuthUiState.Idle
        }
    }
}
