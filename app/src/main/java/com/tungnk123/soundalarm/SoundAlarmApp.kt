package com.tungnk123.soundalarm

import android.app.Application
import android.content.Context
import com.tungnk123.soundalarm.util.LocaleManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SoundAlarmApp : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.applyLocale(base))
    }
}
