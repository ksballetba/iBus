package com.ksballetba.ibus.activity

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import com.baidu.mapapi.search.core.PoiInfo
import com.chad.library.adapter.base.BaseQuickAdapter
import com.ksballetba.ibus.R
import com.ksballetba.ibus.data.entity.CollectedLineEntity
import com.ksballetba.ibus.data.entity.CollectedPoiEntity
import com.ksballetba.ibus.data.source.local.AppDataBaseHelper
import com.ksballetba.ibus.ui.adapter.SuggestPoisAdapter
import com.ksballetba.ibus.ui.adapter.ViewPagerAdapter
import com.ksballetba.ibus.ui.fragment.LinesCollectionFragment
import com.ksballetba.ibus.ui.fragment.PoisCollectionFragment
import com.ksballetba.ibus.util.CommonUtil
import kotlinx.android.synthetic.main.activity_collection.*

class CollectionActivity : AppCompatActivity() {

    private lateinit var mPoisCollectionFragment:Fragment
    private lateinit var mLinesCOllectionFragment:Fragment
    private val mFragmentList = mutableListOf<Fragment>()

    val mAppDataBaseHelper: AppDataBaseHelper by lazy {
        AppDataBaseHelper.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_collection)
        initToolbar()
        initFragments()
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

    private fun initFragments(){
        mPoisCollectionFragment = PoisCollectionFragment()
        mLinesCOllectionFragment = LinesCollectionFragment()
        mFragmentList.add(mPoisCollectionFragment)
        mFragmentList.add(mLinesCOllectionFragment)
        vpCollection.adapter = ViewPagerAdapter(mFragmentList,supportFragmentManager)
        tabCollection.setupWithViewPager(vpCollection)
        tabCollection.getTabAt(0)?.setIcon(R.drawable.ic_place_grey_800_24dp)
        tabCollection.getTabAt(1)?.setIcon(R.drawable.ic_subdirectory_arrow_right_grey_800_24dp)
    }


    fun backToMainActivity(collectionPoi:PoiInfo?){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.COLLECTED_POI, collectionPoi)
        startActivity(intent)
        finish()
    }

    fun backToRouteActivity(poi:PoiInfo?){
        val intent = Intent(this,RouteActivity::class.java)
        intent.putExtra(RouteActivity.POI,poi)
        startActivity(intent)
    }

    fun backToRouteActivity(collectionLine:CollectedLineEntity){
        val intent = Intent(this,RouteActivity::class.java)
        intent.putExtra(RouteActivity.LINE,collectionLine)
        startActivity(intent)
    }

}
