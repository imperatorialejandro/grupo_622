package com.tpsoa.common

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*

object GpsUtils {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder

    private lateinit var currentLocation : String

    fun init(context: Context) {
        fusedLocationClient =  LocationServices.getFusedLocationProviderClient(context)
        geocoder = Geocoder(context, Locale.getDefault())
    }
    @SuppressLint("MissingPermission")

    fun getLocation(context: Context, callback: (String) -> Unit) {
        if(!Utils.isOnline(context)) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                var address = geocoder.getFromLocation(location!!.latitude, location!!.longitude, 1)[0]
                currentLocation = buildLocation(address)
                callback.invoke(this.currentLocation)
            }
    }

    private fun buildLocation(address: Address):String {
        return address.locality + ", " + address.adminArea + ", " + address.countryName
    }
}