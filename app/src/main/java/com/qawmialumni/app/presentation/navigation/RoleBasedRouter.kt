package com.qawmialumni.app.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qawmialumni.app.core.datastore.CachedUserSession
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.util.Resource
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.domain.model.UserStatus
import com.qawmialumni.app.domain.repository.AuthRepository
import com.qawmialumni.app.presentation.auth.AuthViewModel
import com.qawmialumni.app.presentation.auth.JoinMadrasaByCodeScreen
import com.qawmialumni.app.presentation.auth.JoinMadrasaViewModel
import com.qawmialumni.app.presentation.auth.LoginScreen
import com.qawmialumni.app.presentation.auth.PendingApprovalScreen
import com.qawmialumni.app.presentation.auth.SignUpScreen
import com.qawmialumni.app.presentation.theme.EmeraldPrimary
import kotlinx.coroutines.launch

/**
 * Single-App Multi-Role System & Startup Router (Blueprint §2 & §5.A).
 */
@Composable
fun RoleBasedRouter(
    authRepository: AuthRepository,
    dataStore: UserPreferencesDataStore,
    onNavigateToMainApp: (showAdminTab: Boolean, showStaffTools: Boolean) -> Unit,
    onNavigateToSuperAdmin: () -> Unit,
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val joinViewModel: JoinMadrasaViewModel = hiltViewModel()

    val authStateUid by authRepository.authState.collectAsState(initial = authRepository.currentUserId)
    var manualUid by remember { mutableStateOf(authRepository.currentUserId) }
    val currentUid = authStateUid ?: manualUid

    val cachedSession by dataStore.userSession.collectAsState(initial = CachedUserSession())
    var fetchedProfile by remember { mutableStateOf<UserProfile?>(null) }

    var isCheckingFirestore by remember { mutableStateOf(currentUid != null) }
    var authScreenMode by remember { mutableStateOf("login") }

    val scope = rememberCoroutineScope()

    // Refresh profile whenever currentUid becomes non-null
    LaunchedEffect(currentUid) {
        if (currentUid != null) {
            isCheckingFirestore = true
            when (val res = authRepository.fetchUserProfile(currentUid)) {
                is Resource.Success -> fetchedProfile = res.data
                else -> fetchedProfile = UserProfile(uid = currentUid)
            }
            isCheckingFirestore = false
        } else {
            fetchedProfile = null
            isCheckingFirestore = false
        }
    }

    val effectiveRole = fetchedProfile?.role ?: cachedSession.role
    val effectiveStatus = fetchedProfile?.status ?: cachedSession.status
    val effectiveName = fetchedProfile?.name?.ifBlank { cachedSession.name } ?: cachedSession.name

    // Perform navigation callbacks inside LaunchedEffect once profile is resolved
    LaunchedEffect(currentUid, isCheckingFirestore, effectiveRole, effectiveStatus) {
        if (currentUid != null && !isCheckingFirestore) {
            when (effectiveRole) {
                UserRole.SUPER_ADMIN -> {
                    onNavigateToSuperAdmin()
                }
                UserRole.MADRASA_ADMIN -> {
                    onNavigateToMainApp(true, true)
                }
                UserRole.STAFF -> {
                    onNavigateToMainApp(false, true)
                }
                UserRole.ALUMNI -> {
                    if (effectiveStatus == UserStatus.APPROVED) {
                        onNavigateToMainApp(false, false)
                    }
                }
            }
        }
    }

    if (currentUid == null) {
        if (authScreenMode == "signup") {
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    authViewModel.resetState()
                    authScreenMode = "login"
                },
                onSignUpSuccess = {
                    authViewModel.resetState()
                    manualUid = authRepository.currentUserId
                },
            )
        } else {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = {
                    authViewModel.resetState()
                    authScreenMode = "signup"
                },
                onLoginSuccess = {
                    authViewModel.resetState()
                    manualUid = authRepository.currentUserId
                },
            )
        }
        return
    }

    if (isCheckingFirestore) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = EmeraldPrimary,
                )
            }
        }
        return
    }

    // For ALUMNI who are not yet APPROVED, render their onboarding / pending / rejected screen
    if (effectiveRole == UserRole.ALUMNI) {
        when (effectiveStatus) {
            UserStatus.NONE -> {
                JoinMadrasaByCodeScreen(
                    viewModel = joinViewModel,
                    initialName = effectiveName,
                    onSubmitSuccess = {
                        scope.launch {
                            isCheckingFirestore = true
                            when (val res = authRepository.fetchUserProfile(currentUid)) {
                                is Resource.Success -> fetchedProfile = res.data
                                else -> Unit
                            }
                            isCheckingFirestore = false
                        }
                    },
                )
            }
            UserStatus.PENDING -> {
                PendingApprovalScreen(
                    onCheckStatus = {
                        scope.launch {
                            isCheckingFirestore = true
                            when (val res = authRepository.fetchUserProfile(currentUid)) {
                                is Resource.Success -> fetchedProfile = res.data
                                else -> Unit
                            }
                            isCheckingFirestore = false
                        }
                    },
                    onSignOut = {
                        manualUid = null
                        fetchedProfile = null
                        authViewModel.signOut()
                    },
                )
            }
            UserStatus.REJECTED -> {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "দুঃখিত, আপনার মাদরাসা যুক্ত হওয়ার আবেদনটি গৃহীত হয়নি।",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    manualUid = null
                                    fetchedProfile = null
                                    authViewModel.signOut()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            ) {
                                Text("লগআউট করুন")
                            }
                        }
                    }
                }
            }
            UserStatus.APPROVED -> {
                // Handled by LaunchedEffect above
            }
        }
    }
}
