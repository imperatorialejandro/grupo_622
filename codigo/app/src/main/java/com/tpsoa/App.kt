package com.tpsoa

import android.app.Application
import com.tpsoa.common.GpsUtils
import com.tpsoa.sharedpreferences.SharedPreferencesManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        GpsUtils.init(applicationContext)
        SharedPreferencesManager.init(applicationContext)
    }
}