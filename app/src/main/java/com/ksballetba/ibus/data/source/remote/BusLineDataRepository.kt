package com.ksballetba.ibus.data.source.remote

import com.baidu.mapapi.search.busline.BusLineSearch
import com.baidu.mapapi.search.busline.BusLineSearchOption
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener

class BusLineDataRepository(onGetBusLineSearchResultListener: OnGetBusLineSearchResultListener?){

    private val mBusLineSearch = BusLineSearch.newInstance()

    init {
        mBusLineSearch.setOnGetBusLineSearchResultListener(onGetBusLineSearchResultListener)
    }

    fun startBusLineSearch(city:String?,uid:String?){
        mBusLineSearch.searchBusLine(BusLineSearchOption()
            .city(city)
            .uid(uid))
    }

    fun destroy(){
        mBusLineSearch.destroy()
    }
}