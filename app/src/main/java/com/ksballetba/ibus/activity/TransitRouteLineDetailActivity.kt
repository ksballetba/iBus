package com.ksballetba.ibus.activity

import android.arch.lifecycle.Observer
import android.graphics.Color
import android.graphics.Point
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.route.TransitRouteLine
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import com.ksballetba.ibus.R
import com.ksballetba.ibus.data.entity.CollectedLineEntity
import com.ksballetba.ibus.data.entity.CustomTransitStep
import com.ksballetba.ibus.data.source.local.AppDataBaseHelper
import com.ksballetba.ibus.data.source.remote.PoiDataRepository
import com.ksballetba.ibus.ui.adapter.TransitStepsAdapter
import com.ksballetba.ibus.ui.listener.MyOrientationListener
import com.ksballetba.ibus.util.CommonUtil
import com.ksballetba.ibus.util.overlayutil.TransitRouteOverlay
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_transit_route_line_detail.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.toast

class TransitRouteLineDetailActivity : AppCompatActivity() {

    companion object {
        const val TAG = "RouteLineDetailActivity"
    }

    private lateinit var mStepsAdapter: TransitStepsAdapter
    private var mStepsList = mutableListOf<CustomTransitStep>()
    private lateinit var mBottomBehavior:BottomSheetBehavior<View>
    private lateinit var mBaiduMap: BaiduMap
    private lateinit var mLocationClient: LocationClient
    private var isFirstLocated = true
    private var isZoomToSapn = true
    private var mLine:TransitRouteLine? = null
    private var mEntrancePoi: PoiInfo? = null
    private var mExitPoi: PoiInfo? = null
    private var mIsLineCollected = false
    private val mAppDataBaseHelper: AppDataBaseHelper by lazy {
        AppDataBaseHelper.getInstance(applicationContext)
    }
    private lateinit var mOrientationListener: MyOrientationListener
    private var mCurrentLocation:BDLocation? = null
    private var mAzimuth = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        setContentView(R.layout.activity_transit_route_line_detail)
        mLine = intent?.getParcelableExtra(RouteActivity.TRANSIT_ROUTE_LINE)
        mEntrancePoi = intent?.getParcelableExtra(RouteActivity.ENTRANCE_POI)
        mExitPoi = intent?.getParcelableExtra(RouteActivity.EXIT_POI)
        initMap()
        initFAB()
        initStepRec()
        initBottomSheet()
    }

    override fun onResume() {
        super.onResume()
        mvTransitRouteLine.onResume()
    }

    override fun onStart() {
        super.onStart()
        mOrientationListener.registerListener()
    }

    override fun onPause() {
        super.onPause()
        mvTransitRouteLine.onPause()
    }

    override fun onStop() {
        super.onStop()
        mOrientationListener.unregisterListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationClient.stop()
        mvTransitRouteLine.onDestroy()
        mBaiduMap.isMyLocationEnabled = false
    }

    private fun initStepRec(){
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        rvTransitRouteSteps.layoutManager = layoutManager
        mStepsAdapter = TransitStepsAdapter(mStepsList)
        mStepsAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN)
        rvTransitRouteSteps.adapter = mStepsAdapter
        if(mLine!=null){
            mStepsList = CommonUtil.convertToCustomTransitStep(mLine)
        }
        mStepsAdapter.setNewData(mStepsList)
    }

    private fun initMap(){
        mBaiduMap = mvTransitRouteLine.map
        mBaiduMap.isTrafficEnabled = true
        mBaiduMap.isMyLocationEnabled = true
        mBaiduMap.compassPosition = Point(dip(30), dip(120))
        mLocationClient = LocationClient(applicationContext)
        val option = LocationClientOption()
        option.openGps = true
        option.scanSpan = 1000
        option.isNeedPoiRegion = true
        option.coorType = MainActivity.COOR_TYPE
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        mLocationClient.locOption = option
        val myLocationConfiguration = MyLocationConfiguration(
            MyLocationConfiguration.LocationMode.NORMAL,true,
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

    private fun initFAB() {
        fabLineMyLocation.setOnClickListener {
            isFirstLocated = true
            mLocationClient.start()
        }
        fabLineCollect.setOnClickListener {
            val currentLine = CollectedLineEntity("${mEntrancePoi?.uid}->${mExitPoi?.uid}",
                mEntrancePoi?.name,mEntrancePoi?.city,mEntrancePoi?.location?.latitude,mEntrancePoi?.location?.longitude,
                mEntrancePoi?.area,mEntrancePoi?.getPoiDetailInfo()?.tag,
                mExitPoi?.name,mExitPoi?.city,mExitPoi?.location?.latitude,mExitPoi?.location?.longitude,
                mExitPoi?.area,mExitPoi?.getPoiDetailInfo()?.tag)
            Completable.fromAction{
                if(mIsLineCollected){
                    mAppDataBaseHelper.deleteLine(currentLine)
                }else{
                    mAppDataBaseHelper.insertLine(currentLine)
                }
            }.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        }
        ivBack.setOnClickListener {
            finish()
        }
    }

    private fun initBottomSheet() {
        val bottomSheet = findViewById<View>(R.id.cvLineSteps)
        mBottomBehavior = BottomSheetBehavior.from(bottomSheet)
        mBottomBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, sildeOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        fabLineMyLocation.hide()
                        mvTransitRouteLine.showZoomControls(false)
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        fabLineMyLocation.show()
                        mvTransitRouteLine.showZoomControls(true)
                    }
                }
            }
        })
    }

    private fun navigateTo(latLng: LatLng) {
        var update = MapStatusUpdateFactory.newLatLng(latLng)
        mBaiduMap.animateMapStatus(update)
        update = MapStatusUpdateFactory.zoomTo(16f)
        mBaiduMap.animateMapStatus(update)
    }



    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            if (location == null || mvTransitRouteLine == null) {
                return
            }
            if (!CommonUtil.isLocServiceEnable(this@TransitRouteLineDetailActivity)) {
                toast(getString(R.string.err_no_loc_service))
            }
            if(!CommonUtil.isNetworkAvailable(this@TransitRouteLineDetailActivity)){
                toast(getString(R.string.err_no_network_service))
            }
            if (isFirstLocated) {
                val latLng = LatLng(location.latitude, location.longitude)
                navigateTo(latLng)
                isFirstLocated = false
                mAppDataBaseHelper.queryLinesByUid("${mEntrancePoi?.uid}->${mExitPoi?.uid}").observe(this@TransitRouteLineDetailActivity, Observer{
                    mIsLineCollected = if(it!=null&& it.isNotEmpty()){
                        fabLineCollect.setImageResource(R.drawable.ic_star_white_24dp)
                        true
                    }else{
                        fabLineCollect.setImageResource(R.drawable.ic_star_border_white_24dp)
                        false
                    }
                })
            }
            if(isZoomToSapn){
                val overlay = TransitRouteOverlay(mBaiduMap)
                if(mLine!=null){
                    overlay.setData(mLine)
                    overlay.addToMap()
                    overlay.zoomToSpan()
                }
                isZoomToSapn = false
            }
            mCurrentLocation = location
        }
    }
}
