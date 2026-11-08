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
 *
 * Flow:
 * 1. Reactively observes Firebase Auth state (`authRepository.authState`) and DataStore session.
 * 2. When signed in, fetches fresh profile from Firestore and updates DataStore.
 * 3. Dynamically routes inside Jetpack Compose:
 *    - super_admin -> SuperAdminDashboard
 *    - madrasa_admin -> MainApp(showAdminTab = true, showStaffTools = true)
 *    - staff -> MainApp(showAdminTab = false, showStaffTools = true)
 *    - alumni:
 *        - none -> JoinMadrasaByCodeScreen
 *        - pending -> PendingApprovalScreen
 *        - approved -> MainApp(showAdminTab = false, showStaffTools = false)
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

    // Reactive auth state stream so login/signup triggers immediate recomposition!
    val authStateUid by authRepository.authState.collectAsState(initial = authRepository.currentUserId)
    var manualUid by remember { mutableStateOf(authRepository.currentUserId) }
    val currentUid = authStateUid ?: manualUid

    val cachedSession by dataStore.userSession.collectAsState(initial = CachedUserSession())

    var isCheckingFirestore by remember { mutableStateOf(currentUid != null) }
    var authScreenMode by remember { mutableStateOf("login") } // "login" or "signup"

    val scope = rememberCoroutineScope()

    // Refresh profile whenever currentUid becomes non-null
    LaunchedEffect(currentUid) {
        if (currentUid != null) {
            isCheckingFirestore = true
            authRepository.fetchUserProfile(currentUid)
            isCheckingFirestore = false
        } else {
            isCheckingFirestore = false
        }
    }

    // Perform navigation callbacks inside LaunchedEffect to avoid side-effects during composition
    LaunchedEffect(currentUid, isCheckingFirestore, cachedSession.uid, cachedSession.role, cachedSession.status) {
        if (currentUid != null && !isCheckingFirestore && cachedSession.uid == currentUid) {
            when (cachedSession.role) {
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
                    if (cachedSession.status == UserStatus.APPROVED) {
                        onNavigateToMainApp(false, false)
                    }
                }
            }
        }
    }

    if (currentUid == null) {
        // Not logged in -> Show Login or Sign-up
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

    // Wait while Firestore profile is loading or DataStore session hasn't synced the current UID yet
    if (isCheckingFirestore || cachedSession.uid != currentUid) {
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
    if (cachedSession.role == UserRole.ALUMNI) {
        when (cachedSession.status) {
            UserStatus.NONE -> {
                JoinMadrasaByCodeScreen(
                    viewModel = joinViewModel,
                    initialName = cachedSession.name,
                    onSubmitSuccess = {
                        scope.launch {
                            isCheckingFirestore = true
                            authRepository.fetchUserProfile(currentUid)
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
                            authRepository.fetchUserProfile(currentUid)
                            isCheckingFirestore = false
                        }
                    },
                    onSignOut = {
                        manualUid = null
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
