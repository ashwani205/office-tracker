package com.example.officetracker.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import android.util.Log


class GPSBroadcastReceiver : BroadcastReceiver() {

    private var locationListener: LocationListener? = null

    fun setLocationListener(locationListener: LocationListener) {
        this.locationListener = locationListener
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("locationBroadcast", "onReceive")
        try {
            val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationListener?.onLocationChanged(true) ?: return
            } else {
                locationListener?.onLocationChanged(false) ?: return
            }
        } catch (ex: Exception) {
            Log.d("locationBroadcast", ex.message.toString())
        }
    }
}