package com.ksballetba.ibus.ui.adapter

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ksballetba.ibus.R
import com.ksballetba.ibus.data.entity.CustomTransitStep
import com.ksballetba.ibus.data.entity.CustomTransitStep.Companion.BUS_TYPE
import com.ksballetba.ibus.data.entity.CustomTransitStep.Companion.NULL
import com.ksballetba.ibus.data.entity.CustomTransitStep.Companion.SUBWAY_TYPE
import com.ksballetba.ibus.data.entity.CustomTransitStep.Companion.WALKING_TYPE

class TransitStepsAdapter(data: List<CustomTransitStep>):BaseMultiItemQuickAdapter<CustomTransitStep,BaseViewHolder>(data){

    init {
        addItemType(CustomTransitStep.WALKING_TYPE,R.layout.layout_walking_routeline_step_item)
        addItemType(CustomTransitStep.BUS_TYPE,R.layout.layout_transit_routeline_step_item)
        addItemType(CustomTransitStep.SUBWAY_TYPE,R.layout.layout_transit_routeline_step_item)
    }

    override fun convert(helper: BaseViewHolder?, item: CustomTransitStep?) {
        when(helper?.itemViewType){
            WALKING_TYPE->{
                helper.setText(R.id.tvRouteStepInstruction,item?.instruction)
            }
            BUS_TYPE->{
                helper.setImageResource(R.id.ivRouteStepType,R.drawable.ic_directions_bus_grey_800_24dp)
                    .setText(R.id.tvRouteStepVehicleName,item?.vechicleName)
                    .setText(R.id.tvRouteStepStartName,item?.entrence)
                    .setText(R.id.tvRouteStepEndName,item?.exit)
                    .setText(R.id.tvRouteStepInstruction,item?.instruction)
            }
            SUBWAY_TYPE->{
                helper.setImageResource(R.id.ivRouteStepType,R.drawable.ic_directions_subway_grey_800_24dp)
                    .setText(R.id.tvRouteStepVehicleName,item?.vechicleName)
                    .setText(R.id.tvRouteStepStartName,item?.entrence)
                    .setText(R.id.tvRouteStepEndName,item?.exit)
                    .setText(R.id.tvRouteStepInstruction,item?.instruction)
            }
            NULL->{

            }
        }
    }
}