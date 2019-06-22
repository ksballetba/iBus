package com.ksballetba.ibus.activity

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import android.view.OrientationEventListener
import android.view.View
import android.view.WindowManager
import com.apkfuns.logutils.LogUtils
import com.baidu.location.*
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.busline.BusLineResult
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener
import com.baidu.mapapi.search.core.PoiDetailInfo
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.poi.*
import com.blankj.utilcode.util.ScreenUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.ksballetba.ibus.R
import com.ksballetba.ibus.data.entity.CollectedPoiEntity
import com.ksballetba.ibus.data.source.local.AppDataBaseHelper
import com.ksballetba.ibus.data.source.remote.BusLineDataRepository
import com.ksballetba.ibus.data.source.remote.PoiDataRepository
import com.ksballetba.ibus.data.source.remote.PoiDataRepository.Companion.BUS_LINE
import com.ksballetba.ibus.data.source.remote.PoiDataRepository.Companion.SUBWAY_LINE
import com.ksballetba.ibus.data.source.remote.PoiDataRepository.Companion.currentLocation
import com.ksballetba.ibus.ui.adapter.SuggestPoisAdapter
import com.ksballetba.ibus.ui.listener.MyOrientationListener
import com.ksballetba.ibus.util.CommonUtil
import com.ksballetba.ibus.util.overlayutil.BusLineOverlay
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "MainActivity"
        val QUERY = "QUERY"
        val MARKED_PLACE_ID = "MARK_PLACE_ID"
        val COLLECTED_POI = "COLLECTED_POI"
        val IS_SEARCH_NEARBY = "IS_SEARCH_NEARBY"
        val COOR_TYPE = "bd09ll"
    }

    private lateinit var rxPermissions: RxPermissions
    private lateinit var mBaiduMap: BaiduMap
    private lateinit var mLocationClient: LocationClient
    private var mQuery: String? = null
    private var isFirstLocated = true
    private lateinit var mOnGetPoiSearchResultListener:OnGetPoiSearchResultListener
    private lateinit var mOnGetBusLineSearchResultListener: OnGetBusLineSearchResultListener
    private lateinit var mOnMapClickListener:BaiduMap.OnMapClickListener
    private val mPoiDataRepository:PoiDataRepository by lazy {
        PoiDataRepository(mOnGetPoiSearchResultListener)
    }
    private val mBusLineDataRepository:BusLineDataRepository by lazy {
        BusLineDataRepository(mOnGetBusLineSearchResultListener)
    }
    private val mAppDataBaseHelper:AppDataBaseHelper by lazy {
        AppDataBaseHelper.getInstance(applicationContext)
    }
    private lateinit var mSuggestPoisAdapter: SuggestPoisAdapter
    private var mSuggestPoisList = mutableListOf<PoiInfo>()
    private lateinit var mBottomBehavior: BottomSheetBehavior<View>
    private var mMarkedPlaceId: Int = -1
    private var mSelectedPoi:PoiInfo? = null
    private var mIsPoiCollected:Boolean = false
    private lateinit var mOrientationListener: MyOrientationListener
    private var mCurrentLocation:BDLocation? = null
    private var mAzimuth = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        setContentView(R.layout.activity_main)
        rxPermissions = RxPermissions(this)
        requestPermissions()
        initToolbar()
        initFAB()
        initBottomSheet()
        initSuggestPoisRec()
    }

    override fun onResume() {
        super.onResume()
        mvMain.onResume()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent?.getStringExtra(QUERY) != null) {
            mBottomBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            mBottomBehavior.peekHeight = dip(220)
            mQuery = intent.getStringExtra(QUERY)
            if(intent.getBooleanExtra(IS_SEARCH_NEARBY,false)){
                mPoiDataRepository.startNearbyPoiSearch(LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!) ,mQuery)
            }else{
                mPoiDataRepository.startPoiSearch(currentLocation?.city,mQuery)
            }
            supportActionBar?.title = mQuery
            if (intent.getIntExtra(MARKED_PLACE_ID,-1) != -1) {
                mMarkedPlaceId = intent.getIntExtra(MARKED_PLACE_ID,-1)
            }else{
                mMarkedPlaceId = -1
                mSelectedPoi = null
                mBaiduMap.clear()
            }
        }else if(intent?.getParcelableExtra<PoiInfo>(COLLECTED_POI)!=null){
            val poiInfo = intent.getParcelableExtra<PoiInfo>(COLLECTED_POI)
            if(dlMain.isDrawerOpen(GravityCompat.START)){
                dlMain.closeDrawer(GravityCompat.START)
            }
            mBottomBehavior.peekHeight = dip(100)
            mBottomBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            mSuggestPoisList.clear()
            mSuggestPoisList.add(poiInfo)
            mSuggestPoisAdapter.setNewData(mSuggestPoisList)
            mSelectedPoi = poiInfo
            markPlace(poiInfo.location)
            navigateTo(poiInfo.location)
        } else{
            mBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            mSelectedPoi = null
            mMarkedPlaceId = -1
            mBaiduMap.clear()
        }
    }

    override fun onStart() {
        super.onStart()
        mOrientationListener.registerListener()
    }

    override fun onPause() {
        super.onPause()
        mvMain.onPause()
    }

    override fun onStop() {
        super.onStop()
        mOrientationListener.unregisterListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationClient.stop()
        mvMain.onDestroy()
        mPoiDataRepository.destroy()
        mBusLineDataRepository.destroy()
        mBaiduMap.isMyLocationEnabled = false
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                dlMain.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if(mBottomBehavior.state!=BottomSheetBehavior.STATE_HIDDEN){
            mBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }else{
            super.onBackPressed()
        }
    }

    private fun initMap() {
        mBaiduMap = mvMain.map
        mBaiduMap.isTrafficEnabled = true
        mBaiduMap.isMyLocationEnabled = true
        mBaiduMap.compassPosition = Point(dip(30), dip(120))
        mLocationClient = LocationClient(applicationContext)
        val option = LocationClientOption()
        option.openGps = true
        option.scanSpan = 1000
        option.isNeedPoiRegion = true
        option.coorType = COOR_TYPE
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        option.setIsNeedAddress(true)
        mLocationClient.locOption = option
        val myLocationConfiguration = MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,true,
            null, Color.TRANSPARENT, ContextCompat.getColor(this,R.color.accent))
        mBaiduMap.setMyLocationConfiguration(myLocationConfiguration)
        val myLocationListener = MyLocationListener()
        mLocationClient.registerLocationListener(myLocationListener)
        mOrientationListener = MyOrientationListener(this)
        mOrientationListener.setOnOrientationListener(object :MyOrientationListener.OnOrientationListener{
            override fun onOrientationChanged(azimuth: Float, pitch: Float, roll: Float) {
                mAzimuth = azimuth
                if(mCurrentLocation!=null){
                    val locData = MyLocationData.Builder()
                        .accuracy(mCurrentLocation!!.radius)
                        .direction(mAzimuth)
                        .latitude(mCurrentLocation!!.latitude)
                        .longitude(mCurrentLocation!!.longitude)
                        .build()
                    mBaiduMap.setMyLocationData(locData)
                }
            }
        })
        mLocationClient.start()
    }

    private fun initToolbar() {
        setSupportActionBar(tbMain)
        nvMain.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_collection -> {
                    val intent = Intent(this,CollectionActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_change_map -> {
                    if(mBaiduMap.mapType == BaiduMap.MAP_TYPE_NORMAL){
                        nvMain.menu.getItem(1).title = getString(R.string.nav_menu_map_change_normal)
                        mBaiduMap.mapType = BaiduMap.MAP_TYPE_SATELLITE
                    }else{
                        nvMain.menu.getItem(1).title = getString(R.string.nav_menu_map_change_satellite)
                        mBaiduMap.mapType = BaiduMap.MAP_TYPE_NORMAL
                    }
                    dlMain.closeDrawer(GravityCompat.START)
                }

                R.id.nav_compass->{
                    if(mBaiduMap.locationConfiguration.locationMode == MyLocationConfiguration.LocationMode.NORMAL){
                        nvMain.menu.getItem(2).title = getString(R.string.nav_menu_map_navigation_normal)
                        val myLocationConfiguration = MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS,true,
                            null, Color.TRANSPARENT,getColor(R.color.accent))
                        mBaiduMap.setMyLocationConfiguration(myLocationConfiguration)
                    }else{
                        nvMain.menu.getItem(2).title = getString(R.string.nav_menu_map_navigation_compass)
                        val myLocationConfiguration = MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,true,
                            null, Color.TRANSPARENT,getColor(R.color.accent))
                        mBaiduMap.setMyLocationConfiguration(myLocationConfiguration)
                        val status = MapStatus.Builder(mBaiduMap.mapStatus).overlook(0f).rotate(0f).build()
                        val update = MapStatusUpdateFactory.newMapStatus(status)
                        mBaiduMap.animateMapStatus(update);
                    }
                    dlMain.closeDrawer(GravityCompat.START)
                }
            }
            true
        }
        tbMain.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initFAB() {
        fabMyLocation.setOnClickListener {
            isFirstLocated = true
            mLocationClient.start()
        }
        fabDirection.setOnClickListener {
            val intent = Intent(this,RouteActivity::class.java)
            startActivity(intent)
        }
        fabCollect.setOnClickListener {
            val currentPoi = CollectedPoiEntity(uid = mSelectedPoi?.uid!!,name = mSelectedPoi?.name,
                city = mSelectedPoi?.city,latitude = mSelectedPoi?.location?.latitude,longitude = mSelectedPoi?.location?.longitude
                ,address = mSelectedPoi?.address, type = mSelectedPoi?.getPoiDetailInfo()?.tag)
            Completable.fromAction{
                if(mIsPoiCollected){
                    mAppDataBaseHelper.deletePoi(currentPoi)
                }else{
                    mAppDataBaseHelper.insertPoi(currentPoi)
                }
            }.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        }
    }


    @SuppressLint("RestrictedApi")
    private fun initBottomSheet() {
        val bottomSheet = findViewById<View>(R.id.cvPois)
        mBottomBehavior = BottomSheetBehavior.from(bottomSheet)
        mBottomBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, sildeOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        fabMyLocation.hide()
                        fabDirection.hide()
                        fabCollect.visibility = View.VISIBLE
                        mvMain.showZoomControls(false)
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        fabMyLocation.show()
                        fabDirection.show()
                        fabCollect.visibility = View.GONE
                        mQuery = null
                        mSelectedPoi = null
                        supportActionBar?.title = resources.getString(R.string.search_hint)
                        mvMain.showZoomControls(true)
                        mBaiduMap.clear()
                    }
                }
            }
        })
        mBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun initPoiSearchListener() {
        mOnGetPoiSearchResultListener = object : OnGetPoiSearchResultListener {
            override fun onGetPoiIndoorResult(poiResult: PoiIndoorResult?) {

            }

            override fun onGetPoiResult(poiResult: PoiResult?) {
                if (poiResult?.allPoi == null) {
                    toast(getString(R.string.err_no_poi_result))
                } else {
                    mSuggestPoisList = poiResult.allPoi
                    mSuggestPoisAdapter.setNewData(mSuggestPoisList)
                    if(mMarkedPlaceId!=-1){
                        mBaiduMap.clear()
                        val markedPoi = mSuggestPoisList[mMarkedPlaceId]
                        mSelectedPoi = markedPoi
                        if(mSelectedPoi?.getPoiDetailInfo()?.tag!=PoiDataRepository.BUS_LINE&&mSelectedPoi?.getPoiDetailInfo()?.tag!= SUBWAY_LINE){
                            markPlace(markedPoi.location)
                            navigateTo(markedPoi.location)
                        }else{
                            mBusLineDataRepository.startBusLineSearch(currentLocation?.city,mSelectedPoi?.uid)
                        }
                    }
                }
            }

            override fun onGetPoiDetailResult(poiResult: PoiDetailResult?) {

            }

            override fun onGetPoiDetailResult(poiResult: PoiDetailSearchResult?) {

            }
        }
        mOnGetBusLineSearchResultListener = OnGetBusLineSearchResultListener {
           markBusline(it)
        }
    }

    private fun initOnMapClickListener(){
        mOnMapClickListener = object : BaiduMap.OnMapClickListener{
            override fun onMapClick(point: LatLng?) {
                mBottomBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }

            override fun onMapPoiClick(mapPoi: MapPoi?): Boolean {
                mBaiduMap.clear()
                val poiInfo = PoiInfo()
                poiInfo.uid = mapPoi?.uid
                poiInfo.name = mapPoi?.name
                poiInfo.city = currentLocation?.city
                poiInfo.location = mapPoi?.position
                poiInfo.address = ""
                poiInfo.area = ""
                val poiDetailInfo = PoiDetailInfo()
                poiDetailInfo.tag = "地点"
                poiInfo.setPoiDetailInfo(poiDetailInfo)
                mBottomBehavior.peekHeight = dip(100)
                mBottomBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                mSuggestPoisList.clear()
                mSuggestPoisList.add(poiInfo)
                mSuggestPoisAdapter.setNewData(mSuggestPoisList)
                mSelectedPoi = poiInfo
                markPlace(mapPoi!!.position)
                navigateTo(mapPoi.position)
                return true
            }
        }
        mBaiduMap.setOnMapClickListener(mOnMapClickListener)
        mBaiduMap.setOnMapLoadedCallback {
            mvMain.setZoomControlsPosition(Point(ScreenUtils.getScreenWidth()-180,ScreenUtils.getScreenHeight()/2))
        }
    }

    private fun initSuggestPoisRec() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        rvPois.layoutManager = layoutManager
        mSuggestPoisAdapter = SuggestPoisAdapter(R.layout.layout_suggest_poi_item, mSuggestPoisList)
        mSuggestPoisAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN)
        rvPois.adapter = mSuggestPoisAdapter
        mSuggestPoisAdapter.setOnItemClickListener { _, _, position ->
            mBaiduMap.clear()
            mSelectedPoi = mSuggestPoisList[position]
            if(mSelectedPoi?.getPoiDetailInfo()?.tag!= BUS_LINE&&mSelectedPoi?.getPoiDetailInfo()?.tag!= SUBWAY_LINE){
                markPlace(mSuggestPoisList[position].location)
                navigateTo(mSuggestPoisList[position].location)
            }else{
                mBusLineDataRepository.startBusLineSearch(currentLocation?.city,mSelectedPoi?.uid)
            }
        }
        mSuggestPoisAdapter.setOnItemChildClickListener { _, _, position ->
            backToRouteActivity(mSuggestPoisList[position])
        }
    }

    private fun requestPermissions() {
        rxPermissions.request(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
            .subscribe {
                if (it) {
                    initMap()
                    initPoiSearchListener()
                    initOnMapClickListener()
                } else {
                    toast(getString(R.string.err_no_permission_access))
                    requestPermissions()
                }
            }
    }

    private fun navigateTo(latLng: LatLng) {
        var update = MapStatusUpdateFactory.newLatLng(latLng)
        mBaiduMap.animateMapStatus(update)
        update = MapStatusUpdateFactory.zoomTo(16f)
        mBaiduMap.animateMapStatus(update)
    }

    private fun markPlace(latLng: LatLng) {
        val bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on_red_800_36dp)
        val option = MarkerOptions()
            .position(latLng)
            .icon(bitmapDescriptor)
        mBaiduMap.addOverlay(option)
        mAppDataBaseHelper.queryPoisByUid(mSelectedPoi?.uid).observe(this, Observer{
            if(it!=null&&it.size>0){
                fabCollect.setImageResource(R.drawable.ic_star_white_24dp)
                mIsPoiCollected = true
            }else{
                fabCollect.setImageResource(R.drawable.ic_star_border_white_24dp)
                mIsPoiCollected = false
            }
        })
    }

    private fun markBusline(result:BusLineResult?){
        if(result==null||result.error!=SearchResult.ERRORNO.NO_ERROR){
            return
        }
        val buslineOverlay = BusLineOverlay(mBaiduMap)
        buslineOverlay.setData(result)
        buslineOverlay.addToMap()
        buslineOverlay.zoomToSpan()
        mAppDataBaseHelper.queryPoisByUid(mSelectedPoi?.uid).observe(this, Observer{
            if(it!=null&&it.size>0){
                fabCollect.setImageResource(R.drawable.ic_star_white_24dp)
                mIsPoiCollected = true
            }else{
                fabCollect.setImageResource(R.drawable.ic_star_border_white_24dp)
                mIsPoiCollected = false
            }
        })
    }

    private fun backToRouteActivity(poiInfo: PoiInfo?){
        val intent = Intent(this,RouteActivity::class.java)
        intent.putExtra(RouteActivity.POI,poiInfo)
        startActivity(intent)
    }

    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            if (location == null || mvMain == null) {
                return
            }
            if (!CommonUtil.isLocServiceEnable(this@MainActivity)) {
                toast(getString(R.string.err_no_loc_service))
            }
            if(!CommonUtil.isNetworkAvailable(this@MainActivity)){
                toast(getString(R.string.err_no_network_service))
            }
            if (isFirstLocated) {
                currentLocation = location
                val latLng = LatLng(location.latitude, location.longitude)
                navigateTo(latLng)
                isFirstLocated = false
            }
            mCurrentLocation = location
        }
    }
}

