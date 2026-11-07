package com.qawmialumni.app.presentation.navigation

sealed class Screen(val route: String) {
    // Auth routes
    data object Login : Screen("login")
    data object SignUp : Screen("sign_up")
    data object JoinMadrasa : Screen("join_madrasa")
    data object PendingApproval : Screen("pending_approval")

    // Role-specific destinations
    data object SuperAdminDashboard : Screen("super_admin_dashboard")
    data object MainApp : Screen("main_app")

    // Sub-screens
    data object AddStudent : Screen("add_student")
    data object CreatePost : Screen("create_post")
    data object CreateNotice : Screen("create_notice")
    data object ChatRoom : Screen("chat_room/{roomId}/{recipientName}") {
        fun createRoute(roomId: String, recipientName: String) = "chat_room/$roomId/$recipientName"
    }
}
