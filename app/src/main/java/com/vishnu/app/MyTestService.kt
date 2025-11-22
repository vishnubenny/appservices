package com.vishnu.app

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.vishnu.app.common.LocationProvider

class MyTestService : Service() {

    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "LocationServiceChannel"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MyTestService", "Service onStartCommand")
        startForeground(NOTIFICATION_ID, createNotification())
        requestLocation()
        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, 
                "Location Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Services")
            .setContentText("Fetching location...")
            .setSmallIcon(R.mipmap.ic_launcher) // Make sure you have this icon
            .build()
    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        Log.d("MyTestService", "Location retrieved, posting to provider...")
                        LocationProvider.postLocation(location)
                    } else {
                        Log.d("MyTestService", "Could not get location from service.")
                    }
                    stopSelf() // Stop the service after getting the location
                }
                .addOnFailureListener { e ->
                    Log.e("MyTestService", "Failed to get location from service", e)
                    stopSelf() // Also stop the service on failure
                }
        } else {
            Log.d("MyTestService", "Location permission not granted. Stopping service.")
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d("MyTestService", "Service Destroyed")
        super.onDestroy()
    }
}
