package com.tungnk123.soundalarm

import android.app.Application
import android.content.Context
import android.os.StrictMode
import com.tungnk123.soundalarm.BuildConfig
import com.tungnk123.soundalarm.util.LocaleManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SoundAlarmApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.applyLocale(base))
    }
}
