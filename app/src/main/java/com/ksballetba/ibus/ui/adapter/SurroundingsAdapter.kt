package com.ksballetba.ibus.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ksballetba.ibus.R

class SurroundingsAdapter(layoutResId:Int,data:List<String>):BaseQuickAdapter<String, BaseViewHolder>(layoutResId,data) {

    companion object {
        const val TAG = "SurroundingsAdapter"
    }
    override fun convert(helper: BaseViewHolder?, item: String?) {
        helper?.setText(R.id.tvSurroundingName,item)
    }
}