package com.ksballetba.ibus.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ksballetba.ibus.R
import com.ksballetba.ibus.data.entity.CustomOtherStep
import com.ksballetba.ibus.data.entity.CustomOtherStep.Companion.BIKING_TYPE
import com.ksballetba.ibus.data.entity.CustomOtherStep.Companion.DRIVING_TYPE
import com.ksballetba.ibus.data.entity.CustomOtherStep.Companion.WALKING_TYPE

class OtherStepsAdapter(layoutResId:Int,data:List<CustomOtherStep>): BaseQuickAdapter<CustomOtherStep, BaseViewHolder>(layoutResId,data) {
    companion object {
        const val TAG = "OtherStepsAdapter"
    }
    override fun convert(helper: BaseViewHolder?, item: CustomOtherStep?) {
        var iconResId:Int = R.drawable.ic_navigation_grey_800_24dp
        when(item?.mStepType){
            DRIVING_TYPE->{
                iconResId = R.drawable.ic_directions_car_grey_800_24dp
            }
            WALKING_TYPE->{
                iconResId = R.drawable.ic_directions_walk_grey_800_24dp
            }
            BIKING_TYPE->{
                iconResId = R.drawable.ic_directions_bike_grey_800_24dp
            }
        }
        helper?.setImageResource(R.id.ivOtherStepType,iconResId)
        helper?.setText(R.id.tvOtherStepInstruction,item?.mInstruction)
    }
}