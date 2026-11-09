package com.qawmialumni.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.qawmialumni.app.MainActivity
import com.qawmialumni.app.R
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.data.remote.dto.NotificationDto
import com.qawmialumni.app.domain.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
enum class NotificationType {
    CHAT,
    NOTICE,
    FORUM,
    ADMIN,
}

@Serializable
data class InAppNotification(
    val id: String = UUID.randomUUID().toString(),
    val type: NotificationType = NotificationType.NOTICE,
    val title: String = "",
    val message: String = "",
    val targetId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    /** UID this notification belongs to. Notifications are ALWAYS recipient-addressed. */
    val recipientUid: String = "",
)

/**
 * NotificationHelper — recipient-addressed notification engine.
 *
 * ## Why the old behaviour was wrong
 * Previously every notification was generated **locally on the device of whoever performed
 * the action** (`sendMessage()` called `showChatMessageNotification()` in the *sender's*
 * process, `postNotice()` in the *poster's* process, ...). A local function can never reach
 * another phone, so the actor always notified himself and the real recipient got nothing.
 *
 * ## Correct model (used now)
 * 1. **Publish** — the actor's device writes one `notifications/{id}` Firestore document per
 *    recipient (`recipientUid`), and never one for itself. Chat messages notify the other
 *    participant, notices/forums notify the madrasa community + admins/super-admins,
 *    join requests / profile updates notify the madrasa admins, moderation results notify
 *    the post owner.
 * 2. **Deliver** — every signed-in device listens to
 *    `notifications.whereEqualTo("recipientUid", myUid)` (single-field filter, no composite
 *    index) and raises the in-app banner + system push only for documents addressed to it.
 *
 * No Cloud Functions / FCM server key is required: on the free Spark plan this keeps the
 * delivery working while the app process is alive (foreground service / open app).
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: UserPreferencesDataStore,
    private val firestore: FirebaseFirestore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val notificationIdCounter = AtomicInteger(1000)
    private val jsonSerializer = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _inAppNotifications = MutableSharedFlow<InAppNotification>(extraBufferCapacity = 64)
    val inAppNotifications: SharedFlow<InAppNotification> = _inAppNotifications.asSharedFlow()

    private val _notificationsList = MutableStateFlow<List<InAppNotification>>(emptyList())
    val notificationsList: StateFlow<List<InAppNotification>> = _notificationsList.asStateFlow()

    // -------- Remote (recipient-addressed) delivery state --------
    private var remoteListener: ListenerRegistration? = null
    private var listenedUid: String = ""
    private var listenStartedAt: Long = 0L
    private val deliveredRemoteIds: MutableSet<String> =
        java.util.Collections.synchronizedSet(mutableSetOf<String>())

    companion object {
        const val CHANNEL_CHAT = "channel_qawmi_chat"
        const val CHANNEL_NOTICE = "channel_qawmi_notices"
        const val CHANNEL_FORUM = "channel_qawmi_forum"

        const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"
        const val EXTRA_TARGET_ID = "extra_target_id"
    }

    init {
        createNotificationChannels()
        loadPersistedNotifications()
    }

    // =========================================================================
    // 1. LOCAL DELIVERY  (runs on the device that RECEIVES the notification)
    // =========================================================================

    private fun loadPersistedNotifications() {
        scope.launch {
            val jsonStr = preferences.rawNotificationsJson.first()
            if (jsonStr.isNotBlank()) {
                val parsed = runCatching {
                    jsonSerializer.decodeFromString<List<InAppNotification>>(jsonStr)
                }.getOrDefault(emptyList())
                if (parsed.isNotEmpty()) {
                    _notificationsList.value = parsed
                    return@launch
                }
            }

            // Initial seed if empty
            val initial = listOf(
                InAppNotification(
                    id = "init_notice_1",
                    type = NotificationType.NOTICE,
                    title = "স্বাগতম কওমি আলামনাই প্ল্যাটফর্মে",
                    message = "কওমি মাদরাসা ফারেগীন ও আসাতিযায়ে কেরামের ডিজিটাল ঐক্য প্ল্যাটফর্মে আপনাকে স্বাগতম।",
                    timestamp = System.currentTimeMillis() - 3600000,
                    isRead = false,
                ),
                InAppNotification(
                    id = "init_forum_1",
                    type = NotificationType.FORUM,
                    title = "ইলমি মুযাকারা ও ফোরাম চালু হয়েছে",
                    message = "মাদরাসা ও গ্লোবাল ফোরামে দ্বীনি আলোচনা ও ফতোয়া শেয়ার করতে পারেন।",
                    timestamp = System.currentTimeMillis() - 7200000,
                    isRead = false,
                )
            )
            _notificationsList.value = initial
            persistNotifications(initial)
        }
    }

    private fun recordNotification(notification: InAppNotification) {
        _notificationsList.update { current ->
            val updated = listOf(notification) + current.filter { it.id != notification.id }.take(49)
            persistNotifications(updated)
            updated
        }
    }

    private fun persistNotifications(list: List<InAppNotification>) {
        scope.launch {
            val jsonStr = runCatching { jsonSerializer.encodeToString(list.take(50)) }.getOrDefault("")
            preferences.saveNotificationsJson(jsonStr)
        }
    }

    fun markAsRead(id: String) {
        _notificationsList.update { list ->
            val updated = list.map { if (it.id == id) it.copy(isRead = true) else it }
            persistNotifications(updated)
            updated
        }
        // Keep the shared Firestore copy in sync so it is not shown as unread on reinstall/re-login.
        scope.launch {
            runCatching {
                firestore.collection(NotificationDto.COLLECTION).document(id)
                    .update("isRead", true)
                    .await()
            }
        }
    }

    fun markAllAsRead() {
        _notificationsList.update { list ->
            val updated = list.map { it.copy(isRead = true) }
            persistNotifications(updated)
            updated
        }
        val uid = listenedUid
        if (uid.isNotBlank()) {
            scope.launch {
                runCatching {
                    // Single-field filter only (no composite index needed); unread ones are
                    // filtered client-side.
                    firestore.collection(NotificationDto.COLLECTION)
                        .whereEqualTo("recipientUid", uid)
                        .get().await()
                        .documents
                        .filter { it.getBoolean("isRead") != true }
                        .forEach { it.reference.update("isRead", true).await() }
                }
            }
        }
    }

    fun deleteNotification(id: String) {
        _notificationsList.update { list ->
            val updated = list.filter { it.id != id }
            persistNotifications(updated)
            updated
        }
        scope.launch {
            runCatching { firestore.collection(NotificationDto.COLLECTION).document(id).delete().await() }
        }
    }

    fun clearAll() {
        val current = _notificationsList.value
        _notificationsList.value = emptyList()
        persistNotifications(emptyList())
        scope.launch {
            current.forEach { notif ->
                runCatching {
                    firestore.collection(NotificationDto.COLLECTION).document(notif.id).delete().await()
                }
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val chatChannel = NotificationChannel(
                CHANNEL_CHAT,
                "মেসেজ ও বার্তা",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "সহপাঠী ও ফারেগীনদের ব্যক্তিগত বার্তার নোটিফিকেশন"
                enableVibration(true)
                setShowBadge(true)
            }

            val noticeChannel = NotificationChannel(
                CHANNEL_NOTICE,
                "মাদরাসা নোটিশ ও ঘোষণা",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "মাদরাসার দস্তারবন্দী, মাহফিল ও প্রশাসনিক নোটিশ"
                enableVibration(true)
                setShowBadge(true)
            }

            val forumChannel = NotificationChannel(
                CHANNEL_FORUM,
                "ফোরাম আলোচনা ও মুযাকারা",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "ফোরামে নতুন পোস্ট, ফতোয়া ও ইলমি মুযাকারা"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(listOf(chatChannel, noticeChannel, forumChannel))
        }
    }

    fun areNotificationsEnabled(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPerm) return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Local-only display on THIS device. Never use it to announce somebody else's action —
     * that is exactly the bug this class fixes. Kept for self-testing only.
     */
    @Deprecated(
        message = "Local-only notification: it cannot reach another user's phone. " +
            "Use publishChatNotification / publishNoticeNotification / publishForumNotification / " +
            "publishAdminNotification / publishDirectNotification instead.",
        replaceWith = ReplaceWith("sendTestNotification()"),
    )
    fun showChatMessageNotification(senderName: String, messageText: String, roomId: String) {
        scope.launch { deliver(NotificationType.CHAT, "নতুন বার্তা: $senderName", messageText, roomId) }
    }

    @Deprecated("Local-only notification. Use publishNoticeNotification().")
    fun showNoticeNotification(title: String, madrasaName: String, noticeId: String) {
        scope.launch { deliver(NotificationType.NOTICE, "📢 নতুন নোটিশ: $madrasaName", title, noticeId) }
    }

    @Deprecated("Local-only notification. Use publishForumNotification().")
    fun showForumPostNotification(title: String, authorName: String, postId: String) {
        scope.launch { deliver(NotificationType.FORUM, "📝 ফোরাম আলোচনা: $authorName", title, postId) }
    }

    @Deprecated("Local-only notification. Use publishAdminNotification() / publishDirectNotification().")
    fun showAdminNotification(title: String, message: String, targetId: String = "") {
        scope.launch { deliver(NotificationType.ADMIN, title, message, targetId) }
    }

    fun sendTestNotification() {
        scope.launch {
            deliver(
                type = NotificationType.CHAT,
                title = "🔔 কওমি আলামনাই নোটিফিকেশন টেস্ট",
                message = "আলহামদুলিল্লাহ, আপনার ডিভাইসে নোটিফিকেশন সফলভাবে চালু রয়েছে।",
                targetId = "test",
            )
        }
    }

    /**
     * Records + shows a notification on *this* device only.
     * When [existing] is supplied (a remote document that already has a stable id) that exact
     * entry is used instead of a fresh random one, so read/delete stay in sync with Firestore.
     */
    private suspend fun deliver(
        type: NotificationType,
        title: String,
        message: String,
        targetId: String,
        existing: InAppNotification? = null,
    ) {
        val settings = preferences.notificationSettings.first()
        val enabled = when (type) {
            NotificationType.CHAT -> settings.chatNotificationsEnabled
            NotificationType.NOTICE -> settings.noticeNotificationsEnabled
            NotificationType.FORUM -> settings.forumNotificationsEnabled
            NotificationType.ADMIN -> true
        }

        val event = existing ?: InAppNotification(
            type = type,
            title = title,
            message = message,
            targetId = targetId,
            recipientUid = listenedUid,
        )
        recordNotification(event)
        _inAppNotifications.emit(event)

        if (!enabled) return

        val channelId = when (type) {
            NotificationType.CHAT -> CHANNEL_CHAT
            NotificationType.NOTICE -> CHANNEL_NOTICE
            NotificationType.FORUM -> CHANNEL_FORUM
            NotificationType.ADMIN -> CHANNEL_NOTICE
        }
        sendSystemNotification(
            channelId = channelId,
            title = title,
            text = message,
            type = type,
            targetId = targetId,
            priority = if (type == NotificationType.CHAT) {
                NotificationCompat.PRIORITY_HIGH
            } else {
                NotificationCompat.PRIORITY_DEFAULT
            },
        )
    }

    private fun sendSystemNotification(
        channelId: String,
        title: String,
        text: String,
        type: NotificationType,
        targetId: String,
        priority: Int,
    ) {
        if (!areNotificationsEnabled()) {
            android.util.Log.w("NotificationHelper", "Notifications disabled or POST_NOTIFICATIONS permission not granted.")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, type.name)
            putExtra(EXTRA_TARGET_ID, targetId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationIdCounter.get(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val smallIcon = R.drawable.ic_stat_notification

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(priority)
            .setColor(0xFF0F766E.toInt())
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationIdCounter.incrementAndGet(), notification)
        }.onFailure { e ->
            android.util.Log.e("NotificationHelper", "Failed to post system notification", e)
        }
    }

    // =========================================================================
    // 2. REMOTE RECEIVING  (listen to notifications addressed to THIS user)
    // =========================================================================

    /**
     * Binds this device to [uid] and starts listening for recipient-addressed notifications.
     * Must be called whenever the session changes (login / profile switch).
     */
    fun bindUser(uid: String) {
        if (uid.isBlank() || uid == listenedUid) return
        listenedUid = uid
        deliveredRemoteIds.clear()
        startRemoteListener(uid)
    }

    /** Stops delivery and wipes the local list — call on sign-out so the next account starts clean. */
    fun onSignedOut() {
        remoteListener?.remove()
        remoteListener = null
        listenedUid = ""
        deliveredRemoteIds.clear()
        _notificationsList.value = emptyList()
        persistNotifications(emptyList())
        runCatching {
            NotificationManagerCompat.from(context).cancelAll()
        }
    }

    private fun startRemoteListener(uid: String) {
        remoteListener?.remove()
        listenStartedAt = System.currentTimeMillis()
        remoteListener = firestore.collection(NotificationDto.COLLECTION)
            .whereEqualTo("recipientUid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.w("NotificationHelper", "Notification listener error: ${error.localizedMessage}")
                    return@addSnapshotListener
                }
                val docs = snapshot?.documents.orEmpty()
                // Register every document in the in-app list (history), but only pop up a
                // system notification for ones that arrived after this listener started and
                // that this session has not already announced.
                docs.forEach { doc ->
                    val dto = doc.toObject(NotificationDto::class.java) ?: return@forEach
                    val inApp = InAppNotification(
                        id = doc.id,
                        type = runCatching { NotificationType.valueOf(dto.type) }.getOrDefault(NotificationType.NOTICE),
                        title = dto.title,
                        message = dto.message,
                        targetId = dto.targetId,
                        timestamp = dto.createdAt,
                        isRead = dto.isRead,
                        recipientUid = dto.recipientUid,
                    )
                    val isNew = doc.id !in deliveredRemoteIds && dto.createdAt >= listenStartedAt
                    if (doc.id !in deliveredRemoteIds) {
                        deliveredRemoteIds.add(doc.id)
                        if (isNew) {
                            // New arrival → record once + raise the system notification.
                            scope.launch {
                                deliver(
                                    type = inApp.type,
                                    title = inApp.title,
                                    message = inApp.message,
                                    targetId = inApp.targetId,
                                    existing = inApp,
                                )
                            }
                        } else {
                            // Older history → only add to the in-app list, stay silent.
                            recordNotification(inApp)
                        }
                    }
                }
            }
    }

    // =========================================================================
    // 3. REMOTE PUBLISHING  (runs on the device of the user who performs the action)
    // =========================================================================

    suspend fun publishChatNotification(
        senderUid: String,
        senderName: String,
        recipientUid: String,
        messageText: String,
        roomId: String,
    ) {
        if (recipientUid.isBlank() || recipientUid == senderUid) return
        pushRemote(
            recipients = listOf(recipientUid),
            type = NotificationType.CHAT,
            title = "নতুন বার্তা: ${senderName.ifBlank { "সহপাঠী" }}",
            message = messageText,
            targetId = roomId,
            senderUid = senderUid,
            senderName = senderName,
        )
    }

    /**
     * Notice published by an admin: goes to every approved member of the madrasa
     * (and to the madrasa's admins too — they are members), never to the author himself.
     */
    suspend fun publishNoticeNotification(
        madrasaId: String,
        authorUid: String,
        authorName: String,
        title: String,
        noticeId: String,
    ) {
        val recipients = runCatching {
            firestore.collection("users")
                .whereEqualTo("madrasaId", madrasaId)
                .get().await()
                .documents
                .map { it.id }
        }.getOrDefault(emptyList())
        pushRemote(
            recipients = recipients,
            type = NotificationType.NOTICE,
            title = "📢 নতুন নোটিশ: ${authorName.ifBlank { "মাদরাসা প্রশাসন" }}",
            message = title,
            targetId = noticeId,
            senderUid = authorUid,
            senderName = authorName,
        )
    }

    /** Forum post → all members of the madrasa (global forum → every user) + super admins. */
    suspend fun publishForumNotification(
        madrasaId: String,
        authorUid: String,
        authorName: String,
        title: String,
        postId: String,
    ) {
        val memberUids = if (madrasaId.isBlank() || madrasaId == "GLOBAL") {
            runCatching {
                firestore.collection("users").get().await().documents.map { it.id }
            }.getOrDefault(emptyList())
        } else {
            runCatching {
                firestore.collection("users")
                    .whereEqualTo("madrasaId", madrasaId)
                    .get().await()
                    .documents.map { it.id }
            }.getOrDefault(emptyList())
        }
        val recipients = memberUids + superAdminUids()
        pushRemote(
            recipients = recipients,
            type = NotificationType.FORUM,
            title = "📝 ফোরাম আলোচনা: ${authorName.ifBlank { "সদস্য" }}",
            message = title,
            targetId = postId,
            senderUid = authorUid,
            senderName = authorName,
        )
    }

    /** New join request / new student / role change → the madrasa's admins & staff. */
    suspend fun publishAdminNotification(
        madrasaId: String,
        actorUid: String,
        actorName: String,
        title: String,
        message: String,
        targetId: String = "",
        includeSuperAdmins: Boolean = false,
    ) {
        val admins = madrasaAdminUids(madrasaId)
        val recipients = if (includeSuperAdmins) admins + superAdminUids() else admins
        pushRemote(
            recipients = recipients,
            type = NotificationType.ADMIN,
            title = title,
            message = message,
            targetId = targetId,
            senderUid = actorUid,
            senderName = actorName,
        )
    }

    /** Direct message to a single user (moderation result, role change, approval, ...). */
    suspend fun publishDirectNotification(
        recipientUid: String,
        actorUid: String,
        actorName: String,
        title: String,
        message: String,
        targetId: String = "",
        type: NotificationType = NotificationType.ADMIN,
    ) {
        pushRemote(
            recipients = listOf(recipientUid),
            type = type,
            title = title,
            message = message,
            targetId = targetId,
            senderUid = actorUid,
            senderName = actorName,
        )
    }

    private suspend fun pushRemote(
        recipients: List<String>,
        type: NotificationType,
        title: String,
        message: String,
        targetId: String,
        senderUid: String,
        senderName: String,
    ) {
        // Never notify the actor himself, never notify the same device twice.
        val targets = recipients
            .filter { it.isNotBlank() }
            .filter { it != senderUid && it != listenedUid }
            .distinct()
        if (targets.isEmpty()) return

        targets.forEach { uid ->
            val dto = NotificationDto(
                recipientUid = uid,
                senderUid = senderUid,
                senderName = senderName,
                type = type.name,
                title = title,
                message = message,
                targetId = targetId,
                createdAt = System.currentTimeMillis(),
                isRead = false,
            )
            runCatching {
                firestore.collection(NotificationDto.COLLECTION)
                    .document()
                    .set(dto)
                    .await()
            }.onFailure {
                android.util.Log.w("NotificationHelper", "Failed to publish notification to $uid: ${it.localizedMessage}")
            }
        }
    }

    private suspend fun madrasaAdminUids(madrasaId: String): List<String> {
        if (madrasaId.isBlank()) return emptyList()
        return runCatching {
            firestore.collection("users")
                .whereEqualTo("madrasaId", madrasaId)
                .get().await()
                .documents
                .filter { doc ->
                    val role = doc.getString("role") ?: ""
                    role == UserRole.MADRASA_ADMIN.wireValue || role == UserRole.STAFF.wireValue
                }
                .map { it.id }
        }.getOrDefault(emptyList())
    }

    private suspend fun superAdminUids(): List<String> {
        return runCatching {
            firestore.collection("users")
                .whereEqualTo("role", UserRole.SUPER_ADMIN.wireValue)
                .get().await()
                .documents
                .map { it.id }
        }.getOrDefault(emptyList())
    }
}
