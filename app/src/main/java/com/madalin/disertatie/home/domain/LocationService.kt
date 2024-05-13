package com.madalin.disertatie.home.domain

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Service that tracks and reports the device's location in the background as a foreground service.
 * The service will display the location in a persistent notification.
 * The service uses a [CoroutineScope] to manage background tasks.
 */
class LocationService : Service() {
    /**
     * [CoroutineScope] bound to the lifecycle of this [LocationService]. The scope's context has a
     * [SupervisorJob] to make sure that if one job in this scope fails the others will keep running.
     */
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    /**
     * Initializes the location client.
     */
    override fun onCreate() {
        super.onCreate()
        /* TODO inject this */
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    /**
     * Handles start and stop actions sent to the service via intents.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Cancels the [serviceScope] and stops the service.
     */
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    /**
     * Stops the service when the app is swiped away from recent tasks.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    /**
     * Starts the foreground service with notification, requests location updates, and updates the
     * notification with the latest location. The flow collection is launched in [locationClient].
     */
    private fun start() {
        TODO()
        /*        val stopIntent = Intent(this, LocationService::class.java).setAction(ACTION_STOP)
        val pendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val stopAction = NotificationCompat.Action.Builder(R.drawable.ic_launcher_foreground, "Stop", pendingIntent).build()

        // build a notification to show while tracking location
        val notification = NotificationCompat.Builder(this, LOCATION_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Tracking location...")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(stopAction)
            .setOngoing(true) // notification cannot be swiped away

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // collect location updates from the locationClient
        locationClient
            .getLocationUpdates(5000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val latitude = location.latitude.toString()
                val longitude = location.longitude.toString()

                // update the notification content with new location data
                val updatedNotification = notification.setContentText("Location: $latitude, $longitude")
                notificationManager.notify(1, updatedNotification.build())
            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())*/
    }

    /**
     * Stops the foreground service and removes the notification.
     */
    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}

/**
 * Starts a [LocationService].
 */
fun startLocationService(applicationContext: Context) {
    Intent(applicationContext, LocationService::class.java).apply {
        action = LocationService.ACTION_START
        applicationContext.startService(this)
    }
}

/**
 * Stops the [LocationService].
 */
fun stopLocationService(applicationContext: Context) {
    Intent(applicationContext, LocationService::class.java).apply {
        action = LocationService.ACTION_STOP
        applicationContext.startService(this)
    }
}