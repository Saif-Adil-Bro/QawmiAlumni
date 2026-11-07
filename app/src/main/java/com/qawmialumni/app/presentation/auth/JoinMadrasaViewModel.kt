package com.qawmialumni.app.presentation.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qawmialumni.app.core.network.ImageUploadRepository
import com.qawmialumni.app.core.util.ImageTarget
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.KhedmatCategory
import com.qawmialumni.app.domain.model.Madrasa
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface JoinUiState {
    data object Idle : JoinUiState
    data object Loading : JoinUiState
    data class CodeVerified(val madrasa: Madrasa) : JoinUiState
    data object SubmittedSuccess : JoinUiState
    data class Error(val message: String) : JoinUiState
}

@HiltViewModel
class JoinMadrasaViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val imageUploadRepository: ImageUploadRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<JoinUiState>(JoinUiState.Idle)
    val uiState: StateFlow<JoinUiState> = _uiState.asStateFlow()

    private val _uploadProgress = MutableStateFlow(false)
    val uploadProgress: StateFlow<Boolean> = _uploadProgress.asStateFlow()

    var verifiedMadrasa: Madrasa? = null
        private set

    fun verifyCode(code: String) {
        if (code.isBlank()) {
            _uiState.value = JoinUiState.Error("মাদরাসা কোড লিখুন")
            return
        }
        viewModelScope.launch {
            _uiState.value = JoinUiState.Loading
            when (val res = authRepository.verifyMadrasaCode(code)) {
                is Resource.Success -> {
                    verifiedMadrasa = res.data
                    _uiState.value = JoinUiState.CodeVerified(res.data)
                }
                is Resource.Error -> _uiState.value = JoinUiState.Error(res.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun submitProfile(
        name: String,
        fatherName: String,
        phone: String,
        whatsApp: String,
        dakhilaNumber: String,
        batchHijri: String,
        batchEng: String,
        supervisedUstadName: String,
        currentKhedmat: String,
        khedmatCategory: KhedmatCategory,
        bloodGroup: String,
        district: String,
        bio: String,
        photoUri: Uri?,
    ) {
        val madrasa = verifiedMadrasa ?: return
        val uid = authRepository.currentUserId ?: return

        if (name.isBlank() || batchHijri.isBlank() || phone.isBlank()) {
            _uiState.value = JoinUiState.Error("নাম, ফোন ও হিজরি শিক্ষাবর্ষ প্রদান বাধ্যতামূলক")
            return
        }

        viewModelScope.launch {
            _uiState.value = JoinUiState.Loading

            var photoUrl = ""
            if (photoUri != null) {
                _uploadProgress.value = true
                when (val uploadRes = imageUploadRepository.uploadImage(photoUri, ImageTarget.FULL)) {
                    is Resource.Success -> photoUrl = uploadRes.data
                    is Resource.Error -> {
                        _uploadProgress.value = false
                        _uiState.value = JoinUiState.Error("ছবি আপলোড ব্যর্থ: ${uploadRes.message}")
                        return@launch
                    }
                    Resource.Loading -> Unit
                }
                _uploadProgress.value = false
            }

            val profile = UserProfile(
                uid = uid,
                name = name.trim(),
                fatherName = fatherName.trim(),
                phone = phone.trim(),
                whatsApp = whatsApp.ifBlank { phone }.trim(),
                photoUrl = photoUrl,
                madrasaId = madrasa.madrasaId,
                dakhilaNumber = dakhilaNumber.trim(),
                batchYearHijri = batchHijri.trim(),
                batchYearEng = batchEng.trim(),
                supervisedByUstadName = supervisedUstadName.trim(),
                currentKhedmat = currentKhedmat.trim(),
                khedmatCategory = khedmatCategory,
                bloodGroup = bloodGroup.trim(),
                district = district.trim(),
                bio = bio.trim(),
            )

            when (val subRes = authRepository.submitMadrasaJoinRequest(profile)) {
                is Resource.Success -> _uiState.value = JoinUiState.SubmittedSuccess
                is Resource.Error -> _uiState.value = JoinUiState.Error(subRes.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun reset() {
        _uiState.value = JoinUiState.Idle
        verifiedMadrasa = null
    }
}
