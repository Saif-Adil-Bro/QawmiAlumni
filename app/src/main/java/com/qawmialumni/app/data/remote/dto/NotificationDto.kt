package com.qawmialumni.app.data.remote.dto

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Firestore `notifications/{docId}` — a recipient-addressed notification document.
 *
 * Delivery model (Spark / zero-backend friendly):
 *  - The device of the user who performs an action WRITES one document per recipient
 *    (`recipientUid`), never for itself.
 *  - Every signed-in device LISTENS to `notifications.whereEqualTo("recipientUid", myUid)`
 *    (single-field filter only — no composite index needed) and raises the local
 *    in-app + system notification only for documents that belong to it.
 *
 * This is what makes "I send a message → the other person is notified" possible without
 * a Cloud Functions backend.
 */
@IgnoreExtraProperties
data class NotificationDto(
    var notificationId: String = "",
    var recipientUid: String = "",
    var senderUid: String = "",
    var senderName: String = "",
    var type: String = "NOTICE",
    var title: String = "",
    var message: String = "",
    var targetId: String = "",
    var createdAt: Long = 0L,
    var isRead: Boolean = false,
) {
    companion object {
        const val COLLECTION = "notifications"
    }
}
