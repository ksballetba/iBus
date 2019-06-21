package com.ksballetba.ibus.util

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import com.baidu.location.Poi
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.PoiDetailInfo
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.route.TransitRouteLine
import com.ksballetba.ibus.data.entity.CollectedPoiEntity
import com.ksballetba.ibus.data.entity.CustomTransitStep
import org.jetbrains.anko.collections.forEachByIndex

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

    fun convertToCustomTransitStep(line:TransitRouteLine?):MutableList<CustomTransitStep>{
        val result = mutableListOf<CustomTransitStep>()
        line?.allStep?.forEachByIndex {
            val tempStep = CustomTransitStep(it?.stepType,it?.entrance?.title,it?.exit?.title,it.vehicleInfo?.title,it?.instructions)
            result.add(tempStep)
        }
        return result
    }

    fun convertToPoiInfoList(originList:List<CollectedPoiEntity>?):List<PoiInfo>{
        val result = mutableListOf<PoiInfo>()
        originList?.forEachByIndex {
            val poiInfo = PoiInfo()
            poiInfo.uid = it.uid
            poiInfo.name = it.name
            poiInfo.city = it.city
            poiInfo.location = LatLng(it.latitude!!,it.longitude!!)
            poiInfo.address = it.address
            poiInfo.area = ""
            val poiDetailInfo = PoiDetailInfo()
            poiDetailInfo.tag = it.type
            poiInfo.setPoiDetailInfo(poiDetailInfo)
            result.add(poiInfo)
        }
        return result
    }

}