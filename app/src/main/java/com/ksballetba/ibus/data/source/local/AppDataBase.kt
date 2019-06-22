package com.ksballetba.ibus.data.source.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.ksballetba.ibus.data.entity.CollectedLineEntity
import com.ksballetba.ibus.data.entity.CollectedPoiEntity

@Database(entities = [CollectedPoiEntity::class, CollectedLineEntity::class], version = 1)
abstract class AppDataBase : RoomDatabase() {
    abstract fun getCollectedPoisDao(): CollectedPoisDao
    abstract fun getCollectedLinesDao(): CollectedLinesDao
}