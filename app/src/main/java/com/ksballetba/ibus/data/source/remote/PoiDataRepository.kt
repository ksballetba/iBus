package com.ksballetba.ibus.data.source.remote

import com.baidu.location.BDLocation
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener
import com.baidu.mapapi.search.poi.PoiCitySearchOption
import com.baidu.mapapi.search.poi.PoiNearbySearchOption
import com.baidu.mapapi.search.poi.PoiSearch

class PoiDataRepository(onGetPoiSearchResultListener:OnGetPoiSearchResultListener?){

    companion object {
        private val RADIUS = 20000
        @Volatile
        var currentLocation:BDLocation? = null
        const val SUBWAY_STATION = "地铁站"
        const val BUS_STATION = "公交车站"
        const val SUBWAY_LINE = "地铁线路"
        const val BUS_LINE = "公交线路"
        const val POINT = "地点"
    }

    private val mPoiSearch = PoiSearch.newInstance()

    init {
        mPoiSearch.setOnGetPoiSearchResultListener(onGetPoiSearchResultListener)
    }

    fun startPoiSearch(city:String?,query:String?){
        mPoiSearch.searchInCity(
            PoiCitySearchOption()
                .city(city)
                .cityLimit(false)
                .pageCapacity(25)
                .keyword(query)
                .scope(2)
        )
    }

    fun startNearbyPoiSearch(location:LatLng?,query: String?){
        mPoiSearch.searchNearby(
            PoiNearbySearchOption()
                .location(location)
                .radius(RADIUS)
                .pageCapacity(25)
                .keyword(query)
                .scope(2)
        )
    }

    fun destroy(){
        mPoiSearch.destroy()
    }
}