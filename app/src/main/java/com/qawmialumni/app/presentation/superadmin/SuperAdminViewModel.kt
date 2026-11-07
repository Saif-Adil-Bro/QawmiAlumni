package com.qawmialumni.app.presentation.superadmin

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.Madrasa
import com.qawmialumni.app.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

sealed interface CreateMadrasaUiState {
    data object Idle : CreateMadrasaUiState
    data object Loading : CreateMadrasaUiState
    data class Success(val madrasa: Madrasa) : CreateMadrasaUiState
    data class Error(val message: String) : CreateMadrasaUiState
}

@HiltViewModel
class SuperAdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
) : ViewModel() {

    val madrasasList: StateFlow<Resource<List<Madrasa>>> = adminRepository.getAllMadrasas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading,
        )

    private val _createState = MutableStateFlow<CreateMadrasaUiState>(CreateMadrasaUiState.Idle)
    val createState: StateFlow<CreateMadrasaUiState> = _createState.asStateFlow()

    fun generateUniqueCode(name: String): String {
        val initials = name.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .take(4)
            .ifBlank { "MADRASA" }
        val randomSuffix = (1000..9999).random()
        return "$initials-$randomSuffix"
    }

    fun createMadrasa(
        name: String,
        code: String,
        address: String,
        establishedYear: String,
        adminEmail: String,
        logoUri: Uri?,
    ) {
        if (name.isBlank() || code.isBlank()) {
            _createState.value = CreateMadrasaUiState.Error("মাদরাসার নাম ও কোড আবশ্যক")
            return
        }

        viewModelScope.launch {
            _createState.value = CreateMadrasaUiState.Loading
            when (val res = adminRepository.createMadrasa(
                name = name,
                code = code,
                address = address,
                establishedYear = establishedYear,
                adminEmail = adminEmail,
                logoUri = logoUri,
            )) {
                is Resource.Success -> _createState.value = CreateMadrasaUiState.Success(res.data)
                is Resource.Error -> _createState.value = CreateMadrasaUiState.Error(res.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun resetCreateState() {
        _createState.value = CreateMadrasaUiState.Idle
    }
}
