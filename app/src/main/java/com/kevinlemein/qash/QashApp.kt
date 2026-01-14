package com.kevinlemein.qash

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

class QashApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}