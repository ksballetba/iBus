package com.ksballetba.ibus.activity

import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.baidu.mapapi.search.route.PlanNode
import com.ksballetba.ibus.R
import com.ksballetba.ibus.data.source.remote.PoiDataRepository
import com.ksballetba.ibus.ui.adapter.ViewPagerAdapter
import com.ksballetba.ibus.ui.fragment.BikingRouteFragment
import com.ksballetba.ibus.ui.fragment.DrivingRouteFragment
import com.ksballetba.ibus.ui.fragment.TransitRouteFragment
import com.ksballetba.ibus.ui.fragment.WalkingRouteFragment
import kotlinx.android.synthetic.main.activity_route.*

class RouteActivity : AppCompatActivity() {

    companion object {
        const val IS_ROUTE_SEARCH = "IS_ROUTE_SEARCH"
        const val IS_START_POI = "IS_START_POI"
        const val POI_NAME = "POI_NAME"
        const val POI_CITY = "POI_CITY"
        const val POI_AREA = "POI_AREA"
    }

    private var mIsStartPoi = false
    var mStartNode:PlanNode? = null
    var mEndNode:PlanNode? = null
    private lateinit var mTransitRouteFragment:TransitRouteFragment
    private lateinit var mDrivingRouteFragment:DrivingRouteFragment
    private lateinit var mWalkingRouteFragment:WalkingRouteFragment
    private lateinit var mBikingRouteFragment:BikingRouteFragment
    private val mFragmentList = mutableListOf<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_route)
        initToolbar()
        initFragments()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val poiName = intent?.getStringExtra(POI_NAME)
        val poiCity = intent?.getStringExtra(POI_CITY)
        val poiArea = intent?.getStringExtra(POI_AREA)
        if(mIsStartPoi){
            if(poiName!=null){
                tvStartLocation.text = "$poiName $poiCity$poiArea"
                mStartNode = PlanNode.withCityNameAndPlaceName(poiCity,poiName)
            }else{
                tvStartLocation.text = getString(R.string.default_start)
                mStartNode = PlanNode.withLocation(PoiDataRepository.currentLatLng)
            }
        }else{
            if(poiName!=null){
                tvEndLocation.text = "$poiName $poiCity$poiArea"
                mEndNode = PlanNode.withCityNameAndPlaceName(poiCity,poiName)
            }else{
                tvEndLocation.hint = getString(R.string.input_end)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home->{
                finish()
            }
            R.id.route_exchange_direction->{

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.route_menu,menu)
        return true
    }

    private fun initToolbar(){
        setSupportActionBar(tbRoute)
        tvStartLocation.setOnClickListener {
            mIsStartPoi = true
            toSearchActivity()
        }
        tvEndLocation.setOnClickListener {
            mIsStartPoi = false
            toSearchActivity()
        }
    }

    private fun initFragments(){
        mTransitRouteFragment = TransitRouteFragment()
        mDrivingRouteFragment = DrivingRouteFragment()
        mWalkingRouteFragment = WalkingRouteFragment()
        mBikingRouteFragment = BikingRouteFragment()
        mFragmentList.add(mTransitRouteFragment)
        mFragmentList.add(mDrivingRouteFragment)
        mFragmentList.add(mWalkingRouteFragment)
        mFragmentList.add(mBikingRouteFragment)
        vpRoute.offscreenPageLimit = 1
        vpRoute.adapter = ViewPagerAdapter(mFragmentList,supportFragmentManager)
        tabRoute.setupWithViewPager(vpRoute)
        tabRoute.getTabAt(0)?.icon = ContextCompat.getDrawable(this,R.drawable.ic_directions_bus_grey_800_24dp)
        tabRoute.getTabAt(1)?.icon = ContextCompat.getDrawable(this,R.drawable.ic_directions_car_grey_800_24dp)
        tabRoute.getTabAt(2)?.icon = ContextCompat.getDrawable(this,R.drawable.ic_directions_walk_grey_800_24dp)
        tabRoute.getTabAt(3)?.icon = ContextCompat.getDrawable(this,R.drawable.ic_directions_bike_grey_800_24dp)
    }

    private fun toSearchActivity(){
        val intent = Intent(this,SearchActivity::class.java)
        intent.putExtra(IS_ROUTE_SEARCH,true)
        intent.putExtra(IS_START_POI,mIsStartPoi)
        startActivity(intent)
    }

}


