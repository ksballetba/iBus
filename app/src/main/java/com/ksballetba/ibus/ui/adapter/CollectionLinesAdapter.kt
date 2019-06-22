package com.ksballetba.ibus.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ksballetba.ibus.R
import com.ksballetba.ibus.activity.RouteActivity.Companion.MY_LOCATION
import com.ksballetba.ibus.data.entity.CollectedLineEntity

class CollectionLinesAdapter(layoutResId:Int,data:List<CollectedLineEntity>): BaseQuickAdapter<CollectedLineEntity, BaseViewHolder>(layoutResId,data) {

    companion object {
        const val TAG = "SurroundingsAdapter"
    }
    override fun convert(helper: BaseViewHolder?, item: CollectedLineEntity?) {
        if(item?.uid!!.contains(MY_LOCATION)){
            if(item.uid.substring(0,4) == MY_LOCATION){
                helper?.setText(R.id.tvCollectionLineTitle,"$MY_LOCATION->${item.exitName}")
            }else{
                helper?.setText(R.id.tvCollectionLineTitle,"${item.entranceName}->$MY_LOCATION")
            }

        }else{
            helper?.setText(R.id.tvCollectionLineTitle,"${item.entranceName}->${item.exitName}")
        }
    }
}