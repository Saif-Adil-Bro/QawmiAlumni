package com.qawmialumni.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.qawmialumni.app.core.datastore.UserPreferencesDataStore
import com.qawmialumni.app.core.notification.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class QawmiAlumniApp : Application(), ImageLoaderFactory {

    @Inject
    lateinit var dataStore: UserPreferencesDataStore

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // A notification belongs to ONE recipient. Bind the delivery listener to the
        // currently signed-in uid, and reset it when the account changes or signs out —
        // this is what stops "my own messages notifying me" and cross-account leakage.
        appScope.launch {
            dataStore.userSession
                .map { it.uid }
                .distinctUntilChanged()
                .collect { uid ->
                    if (uid.isBlank()) {
                        notificationHelper.onSignedOut()
                    } else {
                        notificationHelper.bindUser(uid)
                    }
                }
        }
    }

    /**
     * Global Coil loader with aggressive memory + disk caching (Blueprint §3),
     * so album photos are downloaded from the image host only once.
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(150L * 1024 * 1024) // 150 MB
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false) // Image hosts send short cache headers; URLs are immutable.
            .crossfade(true)
            .build()
}
