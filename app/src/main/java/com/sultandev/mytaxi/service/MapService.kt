package com.sultandev.mytaxi.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.sultandev.mytaxi.R
import com.sultandev.mytaxi.data.database.dao.LocationDao
import com.sultandev.mytaxi.data.entity.LocationEntity
import com.sultandev.mytaxi.utils.Constants.DEFAULT_INTERVAL_IN_MILLISECONDS
import com.sultandev.mytaxi.utils.Constants.DEFAULT_MAX_WAIT_TIME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.lang.Exception

class MapService : Service() {

    private lateinit var locationEngine: LocationEngine

    val locationDao: LocationDao by inject()

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var callback: LocationEngineCallback<LocationEngineResult>

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotification()
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        callback = object: LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult?) {
                result?.lastLocation ?: return
                coroutineScope.launch {
                    if(result.lastLocation != null) {
                        val lat = result.lastLocation?.latitude!!
                        val lng = result.lastLocation?.longitude!!
                        locationDao.insertUserLocation(LocationEntity(0, lat, lng))
                    }
                }
            }

            override fun onFailure(e: Exception) {
                Timber.tag("SERVICE").d(e.localizedMessage?.toString())
            }
        }

        val request = LocationEngineRequest
            .Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
            .build()

            locationEngine.requestLocationUpdates(
                request,
                callback,
                Looper.getMainLooper()
            )

        locationEngine.getLastLocation(callback)

        showNotification()

        return START_STICKY

    }

    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun showNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.my_taxi))
            .setContentText(getString(R.string.location_processing))
            .setSmallIcon(R.drawable.location_on)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "notify"
        const val CHANNEL_NAME = "channel_location"
        const val NOTIFICATION_ID = 12
    }
}