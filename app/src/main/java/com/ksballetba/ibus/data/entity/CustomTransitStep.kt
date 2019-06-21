package com.ksballetba.ibus.data.entity

import com.baidu.mapapi.search.route.TransitRouteLine
import com.chad.library.adapter.base.entity.MultiItemEntity

class CustomTransitStep(val stepType: TransitRouteLine.TransitStep.TransitRouteStepType?,
                        val entrence:String?,val exit:String?,val vechicleName:String?,val instruction:String?):MultiItemEntity{
    companion object {
        const val WALKING_TYPE = 1
        const val BUS_TYPE = 2
        const val SUBWAY_TYPE = 3
        const val NULL = -1
    }

    private val mItemType = when(stepType){
        TransitRouteLine.TransitStep.TransitRouteStepType.WAKLING-> WALKING_TYPE
        TransitRouteLine.TransitStep.TransitRouteStepType.BUSLINE-> BUS_TYPE
        TransitRouteLine.TransitStep.TransitRouteStepType.SUBWAY-> SUBWAY_TYPE
        null-> NULL
    }


    override fun getItemType(): Int {
        return mItemType
    }
}