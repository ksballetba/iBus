package com.ksballetba.ibus.ui.adapter

import android.util.Log
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.utils.DistanceUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ksballetba.ibus.R
import com.ksballetba.ibus.activity.MainActivity
import com.ksballetba.ibus.data.source.remote.PoiDataRepository.Companion.BUS_LINE
import com.ksballetba.ibus.data.source.remote.PoiDataRepository.Companion.BUS_STATION
import com.ksballetba.ibus.data.source.remote.PoiDataRepository.Companion.SUBWAY_LINE
import com.ksballetba.ibus.data.source.remote.PoiDataRepository.Companion.SUBWAY_STATION
import com.ksballetba.ibus.data.source.remote.PoiDataRepository.Companion.currentLatLng

class SuggestPoisAdapter(layoutResId:Int,data:List<PoiInfo>):BaseQuickAdapter<PoiInfo,BaseViewHolder>(layoutResId,data) {

    companion object {
        const val TAG = "SuggestPoisAdapter"
    }
    override fun convert(helper: BaseViewHolder?, item: PoiInfo?) {
        helper?.setText(R.id.tvPoiName,item?.name)
        val distance = DistanceUtil.getDistance(currentLatLng,item?.location)/1000
        val distanceFormat = String.format("%.1f",distance)
        helper?.setText(R.id.tvPoiAddress,"[${distanceFormat}km]${item?.address}")
        when(item?.getPoiDetailInfo()?.tag){
            SUBWAY_STATION->{
                helper?.setImageResource(R.id.ivPoiType,R.drawable.ic_directions_subway_grey_800_24dp)
            }
            BUS_STATION->{
                helper?.setImageResource(R.id.ivPoiType,R.drawable.ic_directions_bus_grey_800_24dp)
            }
            SUBWAY_LINE, BUS_LINE->{
                helper?.setImageResource(R.id.ivPoiType,R.drawable.ic_subdirectory_arrow_right_grey_800_24dp)
            }
            else->{
                helper?.setImageResource(R.id.ivPoiType,R.drawable.ic_place_grey_800_24dp)
            }
        }
    }
}