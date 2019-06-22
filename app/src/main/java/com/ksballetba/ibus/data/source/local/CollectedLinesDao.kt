package com.ksballetba.ibus.data.source.local

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.ksballetba.ibus.data.entity.CollectedLineEntity

@Dao
interface CollectedLinesDao {

    /**
     * 插入一条线路
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLine(line: CollectedLineEntity)

    /**
     * 插入多条线路
     */

    @Insert
    fun insertLines(lines: List<CollectedLineEntity>)
    /**
     * 查询全部线路
     */
    @Query("select * from collected_lines")
    fun queryAllLines(): LiveData<List<CollectedLineEntity>>

    /**
     * 查询是否有特定uid线路
     */
    @Query("select * from collected_lines where uid = :collectedLineUid")
    fun queryLinesByUid(collectedLineUid:String?): LiveData<List<CollectedLineEntity>>

    /**
     * 删除特定线路
     */
    @Delete
    fun deleteLine(line: CollectedLineEntity)
}