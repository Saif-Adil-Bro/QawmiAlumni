package com.qawmialumni.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.qawmialumni.app.MainActivity
import com.qawmialumni.app.R
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

enum class NotificationType {
    CHAT,
    NOTICE,
    FORUM,
}

data class InAppNotification(
    val id: String = UUID.randomUUID().toString(),
    val type: NotificationType,
    val title: String,
    val message: String,
    val targetId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: UserPreferencesDataStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val notificationIdCounter = AtomicInteger(1000)

    private val _inAppNotifications = MutableSharedFlow<InAppNotification>(extraBufferCapacity = 64)
    val inAppNotifications: SharedFlow<InAppNotification> = _inAppNotifications.asSharedFlow()

    companion object {
        const val CHANNEL_CHAT = "channel_qawmi_chat"
        const val CHANNEL_NOTICE = "channel_qawmi_notices"
        const val CHANNEL_FORUM = "channel_qawmi_forum"

        const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"
        const val EXTRA_TARGET_ID = "extra_target_id"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // 1. Chat Channel (High Importance)
            val chatChannel = NotificationChannel(
                CHANNEL_CHAT,
                "মেসেজ ও বার্তা",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "সহপাঠী ও ফারেগীনদের ব্যক্তিগত বার্তার নোটিফিকেশন"
                enableVibration(true)
                setShowBadge(true)
            }

            // 2. Notice Channel (Default Importance)
            val noticeChannel = NotificationChannel(
                CHANNEL_NOTICE,
                "মাদরাসা নোটিশ ও ঘোষণা",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "মাদরাসার দস্তারবন্দী, মাহফিল ও প্রশাসনিক নোটিশ"
                enableVibration(true)
                setShowBadge(true)
            }

            // 3. Forum Channel (Default Importance)
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
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun showChatMessageNotification(senderName: String, messageText: String, roomId: String) {
        scope.launch {
            val settings = preferences.notificationSettings.first()
            val event = InAppNotification(
                type = NotificationType.CHAT,
                title = "নতুন বার্তা: $senderName",
                message = messageText,
                targetId = roomId,
            )
            _inAppNotifications.emit(event)

            if (!settings.chatNotificationsEnabled) return@launch
            sendSystemNotification(
                channelId = CHANNEL_CHAT,
                title = "নতুন বার্তা: $senderName",
                text = messageText,
                type = NotificationType.CHAT,
                targetId = roomId,
                priority = NotificationCompat.PRIORITY_HIGH,
            )
        }
    }

    fun showNoticeNotification(title: String, madrasaName: String, noticeId: String) {
        scope.launch {
            val settings = preferences.notificationSettings.first()
            val event = InAppNotification(
                type = NotificationType.NOTICE,
                title = "নতুন নোটিশ: $madrasaName",
                message = title,
                targetId = noticeId,
            )
            _inAppNotifications.emit(event)

            if (!settings.noticeNotificationsEnabled) return@launch
            sendSystemNotification(
                channelId = CHANNEL_NOTICE,
                title = "📢 নতুন নোটিশ: $madrasaName",
                text = title,
                type = NotificationType.NOTICE,
                targetId = noticeId,
                priority = NotificationCompat.PRIORITY_DEFAULT,
            )
        }
    }

    fun showForumPostNotification(title: String, authorName: String, postId: String) {
        scope.launch {
            val settings = preferences.notificationSettings.first()
            val event = InAppNotification(
                type = NotificationType.FORUM,
                title = "নতুন ফোরাম পোস্ট: $authorName",
                message = title,
                targetId = postId,
            )
            _inAppNotifications.emit(event)

            if (!settings.forumNotificationsEnabled) return@launch
            sendSystemNotification(
                channelId = CHANNEL_FORUM,
                title = "📝 ফোরাম আলোচনা: $authorName",
                text = title,
                type = NotificationType.FORUM,
                targetId = postId,
                priority = NotificationCompat.PRIORITY_DEFAULT,
            )
        }
    }

    fun sendTestNotification() {
        scope.launch {
            val notif = InAppNotification(
                type = NotificationType.CHAT,
                title = "নোটিফিকেশন টেস্ট সফল",
                message = "কওমি আলামনাই অ্যাপের নোটিফিকেশন সিস্টেম সঠিকভাবে সক্রিয় আছে।",
            )
            _inAppNotifications.emit(notif)

            sendSystemNotification(
                channelId = CHANNEL_CHAT,
                title = "🔔 কওমি আলামনাই নোটিফিকেশন টেস্ট",
                text = "আলহামদুলিল্লাহ, আপনার ডিভাইসে নোটিফিকেশন সফলভাবে চালু রয়েছে।",
                type = NotificationType.CHAT,
                targetId = "test",
                priority = NotificationCompat.PRIORITY_HIGH,
            )
        }
    }

    private fun sendSystemNotification(
        channelId: String,
        title: String,
        text: String,
        type: NotificationType,
        targetId: String,
        priority: Int,
    ) {
        if (!areNotificationsEnabled()) return

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

        val smallIcon = R.mipmap.ic_launcher

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationIdCounter.incrementAndGet(), notification)
        }
    }
}
