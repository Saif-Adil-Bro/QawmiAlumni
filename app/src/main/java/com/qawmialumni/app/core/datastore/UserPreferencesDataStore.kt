package com.qawmialumni.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.qawmialumni.app.domain.model.UserProfile
import com.qawmialumni.app.domain.model.UserRole
import com.qawmialumni.app.domain.model.UserStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class CachedUserSession(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val role: UserRole = UserRole.ALUMNI,
    val madrasaId: String = "",
    val status: UserStatus = UserStatus.NONE,
    val lastSyncTimestamp: Long = 0L,
) {
    val isLoggedIn: Boolean get() = uid.isNotBlank()
    val isApproved: Boolean get() = status == UserStatus.APPROVED
}

data class NotificationSettings(
    val chatNotificationsEnabled: Boolean = true,
    val noticeNotificationsEnabled: Boolean = true,
    val forumNotificationsEnabled: Boolean = true,
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val UID = stringPreferencesKey("uid")
        val NAME = stringPreferencesKey("name")
        val EMAIL = stringPreferencesKey("email")
        val PHOTO_URL = stringPreferencesKey("photo_url")
        val ROLE = stringPreferencesKey("role")
        val MADRASA_ID = stringPreferencesKey("madrasa_id")
        val STATUS = stringPreferencesKey("status")
        val LAST_SYNC = longPreferencesKey("last_sync_timestamp")

        // Notification Settings
        val NOTIF_CHAT = androidx.datastore.preferences.core.booleanPreferencesKey("notif_chat_enabled")
        val NOTIF_NOTICE = androidx.datastore.preferences.core.booleanPreferencesKey("notif_notice_enabled")
        val NOTIF_FORUM = androidx.datastore.preferences.core.booleanPreferencesKey("notif_forum_enabled")
        val NOTIFICATIONS_JSON = stringPreferencesKey("notifications_json")
    }

    val userSession: Flow<CachedUserSession> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            CachedUserSession(
                uid = prefs[Keys.UID].orEmpty(),
                name = prefs[Keys.NAME].orEmpty(),
                email = prefs[Keys.EMAIL].orEmpty(),
                photoUrl = prefs[Keys.PHOTO_URL].orEmpty(),
                role = UserRole.fromWire(prefs[Keys.ROLE]),
                madrasaId = prefs[Keys.MADRASA_ID].orEmpty(),
                status = UserStatus.fromWire(prefs[Keys.STATUS]),
                lastSyncTimestamp = prefs[Keys.LAST_SYNC] ?: 0L,
            )
        }

    suspend fun saveUserSession(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[Keys.UID] = profile.uid
            prefs[Keys.NAME] = profile.name
            prefs[Keys.EMAIL] = profile.email
            prefs[Keys.PHOTO_URL] = profile.photoUrl
            prefs[Keys.ROLE] = profile.role.wireValue
            prefs[Keys.MADRASA_ID] = profile.madrasaId
            prefs[Keys.STATUS] = profile.status.wireValue
        }
    }

    suspend fun updateLastSyncTimestamp(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC] = timestamp
        }
    }

    val notificationSettings: Flow<NotificationSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            NotificationSettings(
                chatNotificationsEnabled = prefs[Keys.NOTIF_CHAT] ?: true,
                noticeNotificationsEnabled = prefs[Keys.NOTIF_NOTICE] ?: true,
                forumNotificationsEnabled = prefs[Keys.NOTIF_FORUM] ?: true,
            )
        }

    suspend fun updateNotificationSettings(
        chatEnabled: Boolean,
        noticeEnabled: Boolean,
        forumEnabled: Boolean,
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIF_CHAT] = chatEnabled
            prefs[Keys.NOTIF_NOTICE] = noticeEnabled
            prefs[Keys.NOTIF_FORUM] = forumEnabled
        }
    }

    val rawNotificationsJson: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            prefs[Keys.NOTIFICATIONS_JSON].orEmpty()
        }

    suspend fun saveNotificationsJson(json: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_JSON] = json
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
