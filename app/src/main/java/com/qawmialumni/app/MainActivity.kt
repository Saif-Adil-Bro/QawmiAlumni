package com.qawmialumni.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.domain.repository.AuthRepository
import com.qawmialumni.app.presentation.navigation.MainAppScaffold
import com.qawmialumni.app.presentation.navigation.RoleBasedRouter
import com.qawmialumni.app.presentation.navigation.SuperAdminDashboardScreen
import com.qawmialumni.app.presentation.theme.QawmiAlumniTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var dataStore: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QawmiAlumniTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val scope = rememberCoroutineScope()
                    var currentDestination by remember { mutableStateOf("router") }
                    var mainAppShowAdmin by remember { mutableStateOf(false) }
                    var mainAppShowStaff by remember { mutableStateOf(false) }

                    when (currentDestination) {
                        "router" -> {
                            RoleBasedRouter(
                                authRepository = authRepository,
                                dataStore = dataStore,
                                onNavigateToMainApp = { showAdmin, showStaff ->
                                    mainAppShowAdmin = showAdmin
                                    mainAppShowStaff = showStaff
                                    currentDestination = "main_app"
                                },
                                onNavigateToSuperAdmin = {
                                    currentDestination = "super_admin"
                                },
                            )
                        }
                        "main_app" -> {
                            MainAppScaffold(
                                showAdminTab = mainAppShowAdmin,
                                showStaffTools = mainAppShowStaff,
                                onSignOut = {
                                    scope.launch {
                                        authRepository.signOut()
                                        currentDestination = "router"
                                    }
                                },
                            )
                        }
                        "super_admin" -> {
                            SuperAdminDashboardScreen(
                                onSignOut = {
                                    scope.launch {
                                        authRepository.signOut()
                                        currentDestination = "router"
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
