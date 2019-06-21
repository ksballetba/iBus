package com.ksballetba.ibus.activity

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.apkfuns.logutils.LogUtils
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.*
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.facebook.stetho.common.LogUtil
import com.google.gson.Gson
import com.ksballetba.ibus.R
import com.ksballetba.ibus.data.entity.CustomOtherStep
import com.ksballetba.ibus.data.entity.CustomOtherStep.Companion.BIKING_TYPE
import com.ksballetba.ibus.data.entity.CustomOtherStep.Companion.DRIVING_TYPE
import com.ksballetba.ibus.data.entity.CustomOtherStep.Companion.WALKING_TYPE
import com.ksballetba.ibus.data.source.remote.PoiDataRepository
import com.ksballetba.ibus.data.source.remote.PoiDataRepository.Companion.currentCity
import com.ksballetba.ibus.data.source.remote.PoiDataRepository.Companion.currentLatLng
import com.ksballetba.ibus.data.source.remote.RoutePlanDataRepository
import com.ksballetba.ibus.ui.adapter.OtherStepsAdapter
import com.ksballetba.ibus.ui.adapter.SurroundingsAdapter
import com.ksballetba.ibus.ui.adapter.TransitRouteLinesAdapter
import com.ksballetba.ibus.util.CommonUtil
import com.ksballetba.ibus.util.overlayutil.BikingRouteOverlay
import com.ksballetba.ibus.util.overlayutil.DrivingRouteOverlay
import com.ksballetba.ibus.util.overlayutil.WalkingRouteOverlay
import kotlinx.android.synthetic.main.activity_route.*
import org.jetbrains.anko.collections.forEachByIndex
import org.jetbrains.anko.dip
import org.jetbrains.anko.toast

class RouteActivity : AppCompatActivity() {

    companion object {
        const val TAG = "RouteActivity"
        const val IS_ROUTE_SEARCH = "IS_ROUTE_SEARCH"
        const val IS_START_POI = "IS_START_POI"
        const val POI_NAME = "POI_NAME"
        const val POI_CITY = "POI_CITY"
        const val POI_AREA = "POI_AREA"
        const val POI_LATITUDE = "POI_LATITUDE"
        const val POI_LONGITUDE = "POI_LONGITUDE"
        const val TRANSIT_ROUTE_LINE = "TRANSIT_ROUTE_LINE"
        const val SEARCH_BY_TRANSIT = 0
        const val SEARCH_BY_DRIVING = 1
        const val SEARCH_BY_WALKING = 2
        const val SEARCH_BY_BIKING = 3
    }

    private var mIsStartPoi = false
    private var mStartNode: PlanNode? = null
    private var mEndNode: PlanNode? = null
    private val mRoutePlanRepository: RoutePlanDataRepository by lazy {
        RoutePlanDataRepository(mOnGetRoutePlanResultListener)
    }
    private lateinit var mBaiduMap: BaiduMap
    private lateinit var mLocationClient: LocationClient
    private var isFirstLocated = true
    private lateinit var mOnGetRoutePlanResultListener: OnGetRoutePlanResultListener
    private lateinit var mTransitRouteLinesAdapter: TransitRouteLinesAdapter
    private var mTransitRouteLineList = mutableListOf<TransitRouteLine>()
    private var mStartNodeTitle = MutableLiveData<String>()
    private var mEndNodeTitle = MutableLiveData<String>()
    private lateinit var mBottomBehavior: BottomSheetBehavior<View>
    private lateinit var mOtherStepAdapter:OtherStepsAdapter
    private var mOtherStepList = mutableListOf<CustomOtherStep>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_route)
        initToolbar()
        initFAB()
        initOnGetRoutePlanResultListener()
        initTabs()
        initMap()
        initTransitRouteLinesRec()
        initOtherStepRec()
        initBottomSheet()
        mStartNode = PlanNode.withLocation(PoiDataRepository.currentLatLng)
        mStartNodeTitle.value = getString(R.string.default_start)
        mEndNodeTitle.value = getString(R.string.input_end)
        val poiName = intent?.getStringExtra(POI_NAME)
        val poiCity = intent?.getStringExtra(POI_CITY)
        val poiArea = intent?.getStringExtra(POI_AREA)
        val poiLatitude = intent?.getDoubleExtra(POI_LATITUDE, (-1).toDouble())
        val poiLongitude = intent?.getDoubleExtra(POI_LONGITUDE, (-1).toDouble())
        if (poiName != null) {
            mEndNodeTitle.postValue("$poiName $poiCity$poiArea")
            mEndNode = PlanNode.withLocation(LatLng(poiLatitude!!, poiLongitude!!))
            startSearch(tabRoute.selectedTabPosition)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val poiName = intent?.getStringExtra(POI_NAME)
        val poiCity = intent?.getStringExtra(POI_CITY)
        val poiArea = intent?.getStringExtra(POI_AREA)
        val poiLatitude = intent?.getDoubleExtra(POI_LATITUDE, (-1).toDouble())
        val poiLongitude = intent?.getDoubleExtra(POI_LONGITUDE, (-1).toDouble())
        if (mIsStartPoi) {
            if (poiName != null) {
                mStartNodeTitle.postValue("$poiName $poiCity$poiArea")
                mStartNode = PlanNode.withLocation(LatLng(poiLatitude!!, poiLongitude!!))
            } else {
                mStartNodeTitle.postValue(getString(R.string.default_start))
                mStartNode = PlanNode.withLocation(PoiDataRepository.currentLatLng)
            }
        } else {
            if (poiName != null) {
                mEndNodeTitle.postValue("$poiName $poiCity$poiArea")
                mEndNode = PlanNode.withLocation(LatLng(poiLatitude!!, poiLongitude!!))
            } else {
                mEndNodeTitle.postValue(getString(R.string.input_end))
            }
        }
        if (mStartNode != null && mEndNode != null) {
            startSearch(tabRoute.selectedTabPosition)
        }
    }

    override fun onResume() {
        super.onResume()
        mvRoute.onResume()
    }

    override fun onPause() {
        super.onPause()
        mvRoute.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationClient.stop()
        mvRoute.onDestroy()
        mRoutePlanRepository.destroy()
        mBaiduMap.isMyLocationEnabled = false
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.route_exchange_direction -> {
                if (mStartNode != null && mEndNode != null) {
                    val tmpNode = mStartNode
                    mStartNode = mEndNode
                    mEndNode = tmpNode
                    val tmp = mStartNodeTitle.value
                    mStartNodeTitle.postValue(mEndNodeTitle.value)
                    mEndNodeTitle.postValue(tmp)
                    startSearch(tabRoute.selectedTabPosition)
                } else {
                    toast(getString(R.string.err_no_endnode_result))
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.route_menu, menu)
        return true
    }

    private fun initToolbar() {
        setSupportActionBar(tbRoute)
        mStartNodeTitle.observe(this, Observer {
            tvStartLocation.text = it
        })
        mEndNodeTitle.observe(this, Observer {
            tvEndLocation.text = it
        })
        tvStartLocation.setOnClickListener {
            mIsStartPoi = true
            toSearchActivity()
        }
        tvEndLocation.setOnClickListener {
            mIsStartPoi = false
            toSearchActivity()
        }
        tabRoute.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (mStartNode != null && mEndNode != null) {
                    startSearch(tabRoute.selectedTabPosition)
                }
            }
        })
    }

    private fun initFAB() {
        fabRouteMyLocation.setOnClickListener {
            isFirstLocated = true
            mLocationClient.start()
        }
    }

    private fun initBottomSheet() {
        val bottomSheet = findViewById<View>(R.id.cvLine)
        mBottomBehavior = BottomSheetBehavior.from(bottomSheet)
        mBottomBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, sildeOffset: Float) {

            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        fabRouteMyLocation.hide()
                        mvRoute.showZoomControls(false)
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        fabRouteMyLocation.show()
                        mvRoute.showZoomControls(true)
                    }
                }
            }
        })
    }

    private fun initTransitRouteLinesRec() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        rvTransitRouteLines.layoutManager = layoutManager
        mTransitRouteLinesAdapter =
            TransitRouteLinesAdapter(R.layout.layout_transitrouteline_item, mTransitRouteLineList)
        rvTransitRouteLines.adapter = mTransitRouteLinesAdapter
        mTransitRouteLinesAdapter.setOnItemClickListener { _, _, position ->
            val intent = Intent(this, TransitRouteLineDetailActivity::class.java)
            intent.putExtra(TRANSIT_ROUTE_LINE, mTransitRouteLineList[position])
            startActivity(intent)
        }
    }

    private fun initTabs() {
        tabRoute.addTab(tabRoute.newTab().setIcon(R.drawable.ic_directions_bus_grey_800_24dp))
        tabRoute.addTab(tabRoute.newTab().setIcon(R.drawable.ic_directions_car_grey_800_24dp))
        tabRoute.addTab(tabRoute.newTab().setIcon(R.drawable.ic_directions_walk_grey_800_24dp))
        tabRoute.addTab(tabRoute.newTab().setIcon(R.drawable.ic_directions_bike_grey_800_24dp))
    }

    private fun initMap() {
        mBaiduMap = mvRoute.map
        mBaiduMap.isTrafficEnabled = true
        mBaiduMap.isMyLocationEnabled = true
        mvRoute.showZoomControls(false)
        mBaiduMap.compassPosition = Point(dip(30), dip(120))
        mLocationClient = LocationClient(applicationContext)
        val option = LocationClientOption()
        option.openGps = true
        option.scanSpan = 1000
        option.coorType = MainActivity.COOR_TYPE
        option.isNeedPoiRegion = true
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        option.setIsNeedAddress(true)
        mLocationClient.locOption = option
        val myLocationListener = MyLocationListener()
        mLocationClient.registerLocationListener(myLocationListener)
        mLocationClient.start()
    }

    private fun initOtherStepRec() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        rvOtherRouteSteps.layoutManager = layoutManager
        mOtherStepAdapter = OtherStepsAdapter(R.layout.layout_other_routeline_step_item,mOtherStepList)
        mOtherStepAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN)
        rvOtherRouteSteps.adapter = mOtherStepAdapter
    }

    private fun initOnGetRoutePlanResultListener() {
        mOnGetRoutePlanResultListener = object : OnGetRoutePlanResultListener {

            override fun onGetIndoorRouteResult(indoorRouteResult: IndoorRouteResult?) {

            }

            override fun onGetTransitRouteResult(transitRouteResult: TransitRouteResult?) {
                mTransitRouteLineList.clear()
                mTransitRouteLinesAdapter.setNewData(mTransitRouteLineList)
                if (transitRouteResult?.routeLines != null) {
                    mTransitRouteLineList = transitRouteResult.routeLines
                    mTransitRouteLinesAdapter.setNewData(mTransitRouteLineList)
                } else {
                    ToastUtils.showShort(R.string.err_no_routeline_result)
                }
            }

            override fun onGetDrivingRouteResult(drivingRouteResult: DrivingRouteResult?) {
                mOtherStepList.clear()
                mOtherStepAdapter.setNewData(mOtherStepList)
                mBaiduMap.clear()
                val overlay = DrivingRouteOverlay(mBaiduMap)
                if (drivingRouteResult?.routeLines != null) {
                    overlay.setData(drivingRouteResult.routeLines[0])
                    overlay.addToMap()
                    overlay.zoomToSpan()
                    drivingRouteResult.routeLines[0].allStep?.forEachByIndex {
                        val tempStep = CustomOtherStep(DRIVING_TYPE,it.instructions)
                        mOtherStepList.add(tempStep)
                    }
                    mOtherStepAdapter.setNewData(mOtherStepList)
                } else {
                    ToastUtils.showShort(R.string.err_no_routeline_result)
                }
            }

            override fun onGetWalkingRouteResult(walkingRouteResult: WalkingRouteResult?) {
                mOtherStepList.clear()
                mOtherStepAdapter.setNewData(mOtherStepList)
                mBaiduMap.clear()
                LogUtils.tag(TAG).d(walkingRouteResult)
                val overlay = WalkingRouteOverlay(mBaiduMap)
                if (walkingRouteResult?.routeLines != null) {
                    overlay.setData(walkingRouteResult.routeLines[0])
                    overlay.addToMap()
                    overlay.zoomToSpan()
                    walkingRouteResult.routeLines[0].allStep?.forEachByIndex {
                        val tempStep = CustomOtherStep(WALKING_TYPE,it.instructions)
                        mOtherStepList.add(tempStep)
                    }
                    mOtherStepAdapter.setNewData(mOtherStepList)
                } else {
                    ToastUtils.showShort(R.string.err_no_routeline_result)
                }
            }

            override fun onGetMassTransitRouteResult(massTransitRouteResult: MassTransitRouteResult?) {

            }

            override fun onGetBikingRouteResult(bikingRouteResult: BikingRouteResult?) {
                mOtherStepList.clear()
                mOtherStepAdapter.setNewData(mOtherStepList)
                mBaiduMap.clear()
                LogUtils.tag(TAG).d(bikingRouteResult)
                val overlay = BikingRouteOverlay(mBaiduMap)
                if (bikingRouteResult?.routeLines != null) {
                    overlay.setData(bikingRouteResult.routeLines[0])
                    overlay.addToMap()
                    bikingRouteResult.routeLines[0].allStep?.forEachByIndex {
                        val tempStep = CustomOtherStep(BIKING_TYPE,it.instructions)
                        mOtherStepList.add(tempStep)
                    }
                    mOtherStepAdapter.setNewData(mOtherStepList)
                } else {
                    ToastUtils.showShort(R.string.err_no_routeline_result)
                }
            }
        }
    }


    @SuppressLint("RestrictedApi")
    private fun startSearch(searchMethod: Int?) {
        if(searchMethod==0){
            rvTransitRouteLines.visibility = View.VISIBLE
            mvRoute.visibility = View.INVISIBLE
            fabRouteMyLocation.hide()
            fabLineCollect.visibility = View.GONE
            cvLine.visibility = View.GONE
        }else{
            rvTransitRouteLines.visibility = View.GONE
            mvRoute.visibility = View.VISIBLE
            fabRouteMyLocation.show()
            fabLineCollect.visibility = View.VISIBLE
            cvLine.visibility = View.VISIBLE
        }
        mBottomBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        when (searchMethod) {
            SEARCH_BY_TRANSIT -> {
                if (mStartNode?.city == null) {
                    mRoutePlanRepository.startTransitSearch(currentCity, mStartNode, mEndNode)
                } else {
                    mRoutePlanRepository.startTransitSearch(mStartNode?.city, mStartNode, mEndNode)
                }
            }
            SEARCH_BY_DRIVING -> {
                mRoutePlanRepository.startDrivingSearch(mStartNode, mEndNode)
            }
            SEARCH_BY_WALKING -> {
                mRoutePlanRepository.startWalkingSearch(mStartNode, mEndNode)
            }
            SEARCH_BY_BIKING -> {
                mRoutePlanRepository.startBikingSearch(mStartNode, mEndNode)
            }
        }
    }

    private fun navigateTo(latLng: LatLng) {
        var update = MapStatusUpdateFactory.newLatLng(latLng)
        mBaiduMap.animateMapStatus(update)
        update = MapStatusUpdateFactory.zoomTo(16f)
        mBaiduMap.animateMapStatus(update)
    }

    private fun toSearchActivity() {
        val intent = Intent(this, SearchActivity::class.java)
        intent.putExtra(IS_ROUTE_SEARCH, true)
        intent.putExtra(IS_START_POI, mIsStartPoi)
        startActivity(intent)
    }

    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            if (location == null || mvRoute == null) {
                return
            }
            if (!CommonUtil.isLocServiceEnable(this@RouteActivity)) {
                toast(getString(R.string.err_no_loc_service))
            }
            if (!CommonUtil.isNetworkAvailable(this@RouteActivity)) {
                toast(getString(R.string.err_no_network_service))
            }
            if (isFirstLocated) {
                val latLng = LatLng(location.latitude, location.longitude)
                navigateTo(latLng)
                isFirstLocated = false
            }
            val locData = MyLocationData.Builder()
                .accuracy(location.radius)
                .direction(location.direction)
                .latitude(location.latitude)
                .longitude(location.longitude)
                .build()
            mBaiduMap.setMyLocationData(locData)
        }
    }

}


