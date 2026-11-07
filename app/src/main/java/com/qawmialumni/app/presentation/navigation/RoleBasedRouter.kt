package com.qawmialumni.app.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.qawmialumni.app.core.datastore.CachedUserSession
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
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

/**
 * Single-App Multi-Role System & Startup Router (Blueprint §2 & §5.A).
 *
 * Flow:
 * 1. Checks current Auth state (Firebase Auth). If not signed in -> Login / Sign-up.
 * 2. Checks local DataStore session. If absent or status == "pending", refreshes from Firestore.
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

    val currentUid = authRepository.currentUserId
    val cachedSession by dataStore.userSession.collectAsState(initial = CachedUserSession())

    var isCheckingFirestore by remember { mutableStateOf(false) }
    var authScreenMode by remember { mutableStateOf("login") } // "login" or "signup"

    val scope = rememberCoroutineScope()

    // Refresh profile if user is logged in but session is empty or pending
    LaunchedEffect(currentUid) {
        if (currentUid != null) {
            isCheckingFirestore = true
            authRepository.fetchUserProfile(currentUid)
            isCheckingFirestore = false
        }
    }

    if (currentUid == null) {
        // Not logged in -> Show Login or Sign-up
        if (authScreenMode == "signup") {
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { authScreenMode = "login" },
                onSignUpSuccess = { /* Automatically handled by auth state change */ },
            )
        } else {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = { authScreenMode = "signup" },
                onLoginSuccess = { /* Automatically handled by auth state change */ },
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

    // Role-based routing logic (Blueprint §5.A)
    when (cachedSession.role) {
        UserRole.SUPER_ADMIN -> {
            onNavigateToSuperAdmin()
        }
        UserRole.MADRASA_ADMIN -> {
            onNavigateToMainApp(/* showAdminTab = */ true, /* showStaffTools = */ true)
        }
        UserRole.STAFF -> {
            onNavigateToMainApp(/* showAdminTab = */ false, /* showStaffTools = */ true)
        }
        UserRole.ALUMNI -> {
            when (cachedSession.status) {
                UserStatus.NONE -> {
                    JoinMadrasaByCodeScreen(
                        viewModel = joinViewModel,
                        initialName = cachedSession.name,
                        onSubmitSuccess = {
                            scope.launch { authRepository.fetchUserProfile(currentUid) }
                        },
                    )
                }
                UserStatus.PENDING -> {
                    PendingApprovalScreen(
                        onCheckStatus = {
                            scope.launch { authRepository.fetchUserProfile(currentUid) }
                        },
                        onSignOut = {
                            authViewModel.signOut()
                        },
                    )
                }
                UserStatus.APPROVED -> {
                    onNavigateToMainApp(/* showAdminTab = */ false, /* showStaffTools = */ false)
                }
                UserStatus.REJECTED -> {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "দুঃখিত, আপনার মাদরাসা যুক্ত হওয়ার আবেদনটি গৃহীত হয়নি।",
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}
