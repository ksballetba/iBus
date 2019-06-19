package com.ksballetba.ibus.data.source.remote

import com.baidu.mapapi.search.route.*

class RoutePlanDataRepository(onGetRoutePlanResultListener:OnGetRoutePlanResultListener?){

    private val mRoutePlanSearch = RoutePlanSearch.newInstance()

    init {
        mRoutePlanSearch.setOnGetRoutePlanResultListener(onGetRoutePlanResultListener)
    }

    fun startWalkingSearch(stNode:PlanNode?,enNode: PlanNode?){
        mRoutePlanSearch.walkingSearch(WalkingRoutePlanOption()
            .from(stNode)
            .to(enNode))
    }

    fun startBikingSearch(stNode:PlanNode?,enNode: PlanNode?){
        mRoutePlanSearch.bikingSearch(BikingRoutePlanOption()
            .from(stNode)
            .to(enNode)
            .ridingType(0))
    }

    fun startDrivingSearch(stNode:PlanNode?,enNode: PlanNode?){
        mRoutePlanSearch.drivingSearch(
            DrivingRoutePlanOption()
            .from(stNode)
            .to(enNode))
    }

    fun startTransitSearch(city:String?,stNode:PlanNode?,enNode: PlanNode?){
        mRoutePlanSearch.transitSearch(TransitRoutePlanOption()
            .from(stNode)
            .to(enNode)
            .city(city))
    }

    fun startMassTransitSearch(stNode:PlanNode?,enNode: PlanNode?){
        mRoutePlanSearch.masstransitSearch(
            MassTransitRoutePlanOption()
            .from(stNode)
            .to(enNode))
    }

    fun destroy(){
        mRoutePlanSearch.destroy()
    }
}