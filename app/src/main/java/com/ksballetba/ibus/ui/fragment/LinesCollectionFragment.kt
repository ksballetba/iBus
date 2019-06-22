package com.ksballetba.ibus.ui.fragment


import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.baidu.mapapi.search.core.PoiInfo
import com.chad.library.adapter.base.BaseQuickAdapter

import com.ksballetba.ibus.R
import com.ksballetba.ibus.activity.CollectionActivity
import com.ksballetba.ibus.data.entity.CollectedLineEntity
import com.ksballetba.ibus.ui.adapter.CollectionLinesAdapter
import com.ksballetba.ibus.ui.adapter.SuggestPoisAdapter
import com.ksballetba.ibus.util.CommonUtil
import kotlinx.android.synthetic.main.fragment_lines_collection.*


class LinesCollectionFragment : Fragment() {

    private lateinit var mCollectedLinesAdapter: CollectionLinesAdapter
    private var mCollectedLineList = mutableListOf<CollectedLineEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lines_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCollectedLinesRec()
    }

    private fun initCollectedLinesRec(){
        val activity = activity as CollectionActivity
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.VERTICAL
        rvLinesCollection.layoutManager = layoutManager
        mCollectedLinesAdapter = CollectionLinesAdapter(R.layout.layout_collection_line_item,mCollectedLineList)
        mCollectedLinesAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN)
        rvLinesCollection.adapter = mCollectedLinesAdapter
        mCollectedLinesAdapter.setOnItemClickListener { _, _, position ->
            activity.backToRouteActivity(mCollectedLineList[position])
        }
        val appDataBaseHelper = activity.mAppDataBaseHelper
        appDataBaseHelper.queryAllLines().observe(this, Observer{
            if(it!=null){
                mCollectedLineList = it.toMutableList()
                mCollectedLinesAdapter.setNewData(mCollectedLineList)
            }
        })
    }
}
