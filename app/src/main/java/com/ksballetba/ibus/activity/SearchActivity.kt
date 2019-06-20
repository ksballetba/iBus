package com.ksballetba.ibus.activity

import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.poi.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.ksballetba.ibus.R
import com.ksballetba.ibus.data.source.remote.PoiDataRepository
import com.ksballetba.ibus.data.source.remote.PoiDataRepository.Companion.currentCity
import com.ksballetba.ibus.data.source.remote.PoiDataRepository.Companion.currentLatLng
import com.ksballetba.ibus.ui.adapter.SuggestPoisAdapter
import com.ksballetba.ibus.ui.adapter.SurroundingsAdapter
import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.toast

class SearchActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SearchActivity"
        private var mIsSearchNearby = false
    }

    private lateinit var mSuggestPoisAdapter:SuggestPoisAdapter
    private var mSuggestPoisList = mutableListOf<PoiInfo>()
    private var mQuery = ""
    private lateinit var mOnGetPoiSearchResultListener:OnGetPoiSearchResultListener
    private var mIsRouteSearch = false
    val mPoiDataRepository: PoiDataRepository by lazy {
        PoiDataRepository(mOnGetPoiSearchResultListener)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_search)
        initSearchView()
        initSuggestPoisRec()
        if(!mIsRouteSearch){
            initSurroundingRec()
            initCheckBoxIsSearchNearby()
        }
        initPoiSearchListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPoiDataRepository.destroy()
    }

    private fun initSearchView(){
        if(intent.getBooleanExtra(RouteActivity.IS_ROUTE_SEARCH,false)){
            mIsRouteSearch = true
        }
        svSearch.requestFocusFromTouch()
        tvCurrentCity.text = currentCity
        val iconView = svSearch.findViewById<ImageView>(android.support.v7.appcompat.R.id.search_mag_icon)
        iconView.setOnClickListener {
            if(!mIsRouteSearch){
                backToMainActivity("",0)
            }else{
                backToRouteActivity(null,null,null)
            }

        }
        svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextChange(query: String?): Boolean {
                if(!TextUtils.isEmpty(query)){
                    mQuery = query!!
                    if(mIsSearchNearby){
                        mPoiDataRepository.startNearbyPoiSearch(currentLatLng,query)
                    }else{
                        mPoiDataRepository.startPoiSearch(currentCity,query)
                    }
                }else{
                    mSuggestPoisList.clear()
                    mSuggestPoisAdapter.setNewData(mSuggestPoisList)
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if(!mIsRouteSearch){
                    backToMainActivity(mQuery,0)
                }else{
                    if(mSuggestPoisList.size>0){
                        backToRouteActivity(mSuggestPoisList[0].name,mSuggestPoisList[0].city,mSuggestPoisList[0].area)
                    }else{
                        toast(getString(R.string.err_no_poi_result))
                    }
                }
                return true
            }
        })
        btnSearch.setOnClickListener {
            if(!mIsRouteSearch){
                backToMainActivity(mQuery,0)
            }else{
                if(mSuggestPoisList.size>0){
                    backToRouteActivity(mSuggestPoisList[0].name,mSuggestPoisList[0].city,mSuggestPoisList[0].area)
                }else{
                    toast(getString(R.string.err_no_poi_result))
                }
            }
        }
    }

    private fun initPoiSearchListener(){
        mOnGetPoiSearchResultListener = object : OnGetPoiSearchResultListener{
            override fun onGetPoiIndoorResult(poiResult: PoiIndoorResult?) {

            }

            override fun onGetPoiResult(poiResult: PoiResult?) {
                if(poiResult?.allPoi==null){
                    toast(getString(R.string.err_no_poi_result))
                }else{
                    mSuggestPoisList = poiResult.allPoi
                    mSuggestPoisAdapter.setNewData(mSuggestPoisList)
                }
            }

            override fun onGetPoiDetailResult(poiResult: PoiDetailResult?) {

            }

            override fun onGetPoiDetailResult(poiResult: PoiDetailSearchResult?) {

            }
        }
    }

    private fun initSuggestPoisRec(){
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        rvSuggestPois.layoutManager = layoutManager
        mSuggestPoisAdapter = SuggestPoisAdapter(R.layout.layout_suggest_poi_item,mSuggestPoisList)
        mSuggestPoisAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN)
        rvSuggestPois.adapter = mSuggestPoisAdapter
        mSuggestPoisAdapter.setOnItemClickListener { _, _, position ->
            if(!mIsRouteSearch){
                backToMainActivity(mQuery,position)
            }else{
                backToRouteActivity(mSuggestPoisList[position].name,mSuggestPoisList[position].city,mSuggestPoisList[position].area)
            }
        }
        mSuggestPoisAdapter.setOnItemChildClickListener { _, _, position ->
            backToRouteActivity(mSuggestPoisList[position].name,mSuggestPoisList[position].city,mSuggestPoisList[position].area)
        }
    }

    private fun initSurroundingRec() {
        val surroundingList = arrayListOf("美食","超市","酒店","银行","景点","医院","商场","影院","停车场","公交站","地铁站","收藏夹")
        val layoutManager = GridLayoutManager(this, 4)
        rvSurrounding.layoutManager = layoutManager
        val surroundingsAdapter = SurroundingsAdapter(R.layout.layout_surrounding_item,surroundingList)
        surroundingsAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN)
        rvSurrounding.adapter = surroundingsAdapter
        surroundingsAdapter.setOnItemClickListener { _, _, position ->
            if(position!=11){
                backToMainActivity(surroundingList[position],0)
            }
        }
    }

    private fun initCheckBoxIsSearchNearby(){
        cbSearchNearby.setOnCheckedChangeListener { _, isChecked ->
            mIsSearchNearby = isChecked
        }
        cbSearchNearby.isChecked = mIsSearchNearby
    }

    private fun backToMainActivity(query:String?,position:Int){
        val intent = Intent(this, MainActivity::class.java)
        if(!TextUtils.isEmpty(query)){
            intent.putExtra(MainActivity.QUERY,query)
        }
        intent.putExtra(MainActivity.MARKED_PLACE_ID, position)
        intent.putExtra(MainActivity.IS_SEARCH_NEARBY,mIsSearchNearby)
        startActivity(intent)
        finish()
    }

    private fun backToRouteActivity(poiName:String?,poiCity:String?,poiAera:String?){
        val intent = Intent(this,RouteActivity::class.java)
        intent.putExtra(RouteActivity.POI_NAME,poiName)
        intent.putExtra(RouteActivity.POI_CITY,poiCity)
        intent.putExtra(RouteActivity.POI_AREA,poiAera)
        startActivity(intent)
    }

}
