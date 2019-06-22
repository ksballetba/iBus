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
import com.ksballetba.ibus.ui.adapter.SuggestPoisAdapter
import com.ksballetba.ibus.util.CommonUtil
import kotlinx.android.synthetic.main.fragment_pois_collection.*


class PoisCollectionFragment : Fragment() {

    private lateinit var mCollectedPoisAdapter: SuggestPoisAdapter
    private var mCollectedPoiList = mutableListOf<PoiInfo>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pois_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCollectedPoisRec()
    }

    private fun initCollectedPoisRec(){
        val activity = activity as CollectionActivity
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.VERTICAL
        rvPoisCollection.layoutManager = layoutManager
        mCollectedPoisAdapter = SuggestPoisAdapter(R.layout.layout_suggest_poi_item,mCollectedPoiList)
        mCollectedPoisAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN)
        rvPoisCollection.adapter = mCollectedPoisAdapter
        mCollectedPoisAdapter.setOnItemClickListener { _, _, position ->
            activity.backToMainActivity(mCollectedPoiList[position])
        }
        mCollectedPoisAdapter.setOnItemChildClickListener { _, _, position ->
            activity.backToRouteActivity(mCollectedPoiList[position])
        }

        val appDataBaseHelper = activity.mAppDataBaseHelper
        appDataBaseHelper.queryAllPois().observe(this, Observer{
            mCollectedPoiList = CommonUtil.convertToPoiInfoList(it).toMutableList()
            mCollectedPoisAdapter.setNewData(mCollectedPoiList)
        })
    }

}
