package com.xiaobaotv.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltAndroidApp
class XiaobaoApp : Application(), ImageLoaderFactory {

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Firebase init on background thread to avoid blocking startup
        backgroundScope.launch {
            FirebaseApp.initializeApp(this@XiaobaoApp)

            if (BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())
            }

            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(
                !BuildConfig.DEBUG
            )
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(128 * 1024 * 1024)
                    .build()
            }
            .crossfade(100)
            .build()
    }
}
