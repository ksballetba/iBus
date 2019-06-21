package com.ksballetba.ibus.activity

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import com.baidu.mapapi.search.core.PoiInfo
import com.chad.library.adapter.base.BaseQuickAdapter
import com.ksballetba.ibus.R
import com.ksballetba.ibus.data.entity.CollectedPoiEntity
import com.ksballetba.ibus.data.source.local.AppDataBaseHelper
import com.ksballetba.ibus.ui.adapter.SuggestPoisAdapter
import com.ksballetba.ibus.util.CommonUtil
import kotlinx.android.synthetic.main.activity_collection.*

class CollectionActivity : AppCompatActivity() {

    private lateinit var mCollectedPoisAdapter: SuggestPoisAdapter
    private var mCollectedPoiList = mutableListOf<PoiInfo>()
    private val mAppDataBaseHelper: AppDataBaseHelper by lazy {
        AppDataBaseHelper.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_collection)
        initToolbar()
        initCollectedPoisRec()
    }

    private fun initToolbar(){
        setSupportActionBar(tbCollection)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home->{
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initCollectedPoisRec(){
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        rvCollections.layoutManager = layoutManager
        mCollectedPoisAdapter = SuggestPoisAdapter(R.layout.layout_suggest_poi_item,mCollectedPoiList)
        mCollectedPoisAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN)
        rvCollections.adapter = mCollectedPoisAdapter
        mCollectedPoisAdapter.setOnItemClickListener { _, _, position ->
            backToMainActivity(mCollectedPoiList[position])
        }
        mCollectedPoisAdapter.setOnItemChildClickListener { _, _, position ->
            backToRouteActivity(mCollectedPoiList[position].name,mCollectedPoiList[position].city,mCollectedPoiList[position].area
                ,mCollectedPoiList[position].location.latitude,mCollectedPoiList[position].location.longitude)
        }
        mAppDataBaseHelper.queryAllPois().observe(this, Observer{
            mCollectedPoiList = CommonUtil.convertToPoiInfoList(it).toMutableList()
            mCollectedPoisAdapter.setNewData(mCollectedPoiList)
        })
    }

    private fun backToMainActivity(collectionPoi:PoiInfo?){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.COLLECTED_POI, collectionPoi)
        startActivity(intent)
        finish()
    }

    private fun backToRouteActivity(poiName:String?,poiCity:String?,poiAera:String?,poiLantitude:Double?,poiLongitude:Double?){
        val intent = Intent(this,RouteActivity::class.java)
        intent.putExtra(RouteActivity.POI_NAME,poiName)
        intent.putExtra(RouteActivity.POI_CITY,poiCity)
        intent.putExtra(RouteActivity.POI_AREA,poiAera)
        intent.putExtra(RouteActivity.POI_LATITUDE,poiLantitude)
        intent.putExtra(RouteActivity.POI_LONGITUDE,poiLongitude)
        startActivity(intent)
    }

}
