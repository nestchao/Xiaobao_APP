package com.xiaobaotv.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class XiaobaoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase (config loaded from google-services.json)
        FirebaseApp.initializeApp(this)

        // Plant Timber for debug logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Set up Firebase Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(
            !BuildConfig.DEBUG
        )
    }
}
