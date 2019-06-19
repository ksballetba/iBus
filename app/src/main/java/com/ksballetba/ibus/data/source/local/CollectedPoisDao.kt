package com.ksballetba.ibus.data.source.local

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.ksballetba.ibus.data.entity.CollectedPoiEntity

@Dao
interface CollectedPoisDao {

    /**
     * 插入一个地点
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPoi(poi: CollectedPoiEntity)

    /**
     * 插入多个地点
     */

    @Insert
    fun insertPois(pois: List<CollectedPoiEntity>)
    /**
     * 查询全部地点
     */
    @Query("select * from collected_pois")
    fun queryAllPois(): LiveData<List<CollectedPoiEntity>>

    /**
     * 查询是否有特定uid地点
     */
    @Query("select * from collected_pois where uid = :collectedPoisUid")
    fun queryPoisByUid(collectedPoisUid:String?): LiveData<List<CollectedPoiEntity>>

    /**
     * 删除有特定uid地点
     */
    @Delete
    fun deletePoi(poi: CollectedPoiEntity)
}