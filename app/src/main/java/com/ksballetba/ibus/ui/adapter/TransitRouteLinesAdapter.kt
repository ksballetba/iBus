package com.ksballetba.ibus.ui.adapter

import android.util.Log
import com.baidu.mapapi.search.route.TransitRouteLine
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ksballetba.ibus.R
import org.jetbrains.anko.collections.forEachByIndex

class TransitRouteLinesAdapter(layoutResId:Int,data:List<TransitRouteLine>): BaseQuickAdapter<TransitRouteLine, BaseViewHolder>(layoutResId,data) {

    companion object {
        const val TAG = "TransitRouteLines"
    }
    override fun convert(helper: BaseViewHolder?, item: TransitRouteLine?) {
        val minutes = item!!.duration/60
        var walkingDistance = 0
        val stepTitles = StringBuffer()
        var totalPrice = 0
        var totalStationNum = 0
        var entrance = ""
        if(minutes>=60){
            val hours = minutes/60
            val leftMinutes = minutes%60
            helper?.setText(R.id.tvRouteLineDuration,"${hours}小时${leftMinutes}分")
        }else{
            helper?.setText(R.id.tvRouteLineDuration,"${minutes}分钟")
        }
        item.allStep.forEachByIndex {
            if(it.vehicleInfo!=null){
                stepTitles.append(it.vehicleInfo.title+"->")
                totalPrice+=it.vehicleInfo.zonePrice
                totalStationNum+=it.vehicleInfo.passStationNum
            }
            if(it.stepType == TransitRouteLine.TransitStep.TransitRouteStepType.WAKLING){
                walkingDistance+=it.distance
            }else if(entrance.isEmpty()){
                entrance = it.entrance.title
            }
        }
        stepTitles.delete(stepTitles.length-2,stepTitles.length)
        if(walkingDistance>=1000){
            helper?.setText(R.id.tvWalkingDistance,"${(walkingDistance/1000)}公里")
        }else{
            helper?.setText(R.id.tvWalkingDistance,"${walkingDistance}米")
        }

        helper?.setText(R.id.tvRouteLineSteps,stepTitles)
        helper?.setText(R.id.tvRouteLinePrice,"${totalStationNum}站·${totalPrice}元·${entrance}上车")
    }
}