package com.ksballetba.ibus.util

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager

object CommonUtil {
    fun isLocServiceEnable(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (gps || network) {
            return true
        }
        return false
    }

    fun isNetworkAvailable(context: Context): Boolean{
        val networkManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = networkManager.activeNetworkInfo
        if(networkInfo==null||!networkInfo.isConnected){
            return false
        }
        return true
    }
}