package com.qawmialumni.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.notification.NotificationHelper
import com.qawmialumni.app.domain.repository.AuthRepository
import com.qawmialumni.app.presentation.navigation.MainAppScaffold
import com.qawmialumni.app.presentation.navigation.RoleBasedRouter
import com.qawmialumni.app.presentation.superadmin.SuperAdminDashboardScreen
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

    private var pendingNotificationType by mutableStateOf<String?>(null)
    private var pendingNotificationTargetId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNotificationIntent(intent)

        setContent {
            QawmiAlumniTheme {
                // Request Android 13+ Notification Runtime Permission
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { /* User response processed */ },
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS,
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

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
                                deepLinkNotificationType = pendingNotificationType,
                                deepLinkTargetId = pendingNotificationTargetId,
                                onConsumeDeepLink = {
                                    pendingNotificationType = null
                                    pendingNotificationTargetId = null
                                },
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val type = intent?.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TYPE)
        val targetId = intent?.getStringExtra(NotificationHelper.EXTRA_TARGET_ID)
        if (!type.isNullOrBlank() && !targetId.isNullOrBlank()) {
            pendingNotificationType = type
            pendingNotificationTargetId = targetId
        }
    }
}
