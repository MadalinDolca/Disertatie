package com.madalin.disertatie.core.domain

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.madalin.disertatie.core.di.appModule
import com.madalin.disertatie.core.di.viewModelModule
import com.madalin.disertatie.core.domain.util.LOCATION_NOTIFICATION_CHANNEL_ID
import org.koin.core.context.startKoin

class DisertatieApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // initialize Koin with the defined modules
        startKoin {
            modules(appModule, viewModelModule)
        }

        createLocationNotificationChannel()
    }

    private fun createLocationNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                LOCATION_NOTIFICATION_CHANNEL_ID,
                "Location Channel",
                NotificationManager.IMPORTANCE_LOW
            )

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}