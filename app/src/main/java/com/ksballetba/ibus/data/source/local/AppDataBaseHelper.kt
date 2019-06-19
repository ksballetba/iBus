package com.ksballetba.ibus.data.source.local

import android.arch.persistence.room.Room
import android.content.Context
import android.util.Log
import com.ksballetba.ibus.data.entity.CollectedPoiEntity

class AppDataBaseHelper constructor(context: Context) {


     val TAG = "AppDataBaseHelper"


    private val appDataBase = Room.databaseBuilder(
        context, AppDataBase::class.java,
        "daily"
    ).build()

    companion object {
        @Volatile
        private var INSTANCE: AppDataBaseHelper? = null

        fun getInstance(context: Context): AppDataBaseHelper {
            if (INSTANCE == null) {
                synchronized(AppDataBaseHelper::class) {
                    if (INSTANCE == null) {
                        INSTANCE = AppDataBaseHelper(context.applicationContext)
                    }
                }
            }
            return INSTANCE!!
        }
    }

    fun insertPoi(poi: CollectedPoiEntity) {
        appDataBase.getCollectedPoisDao().insertPoi(poi)
        Log.d(TAG, poi.name)
    }

    fun insertPois(pois: List<CollectedPoiEntity>) {
        appDataBase.getCollectedPoisDao().insertPois(pois)
    }

    fun queryAllPois() = appDataBase.getCollectedPoisDao().queryAllPois()

    fun queryPoisByUid(uid: String?) = appDataBase.getCollectedPoisDao().queryPoisByUid(uid)

    fun deletePoi(poi: CollectedPoiEntity) {
        appDataBase.getCollectedPoisDao().deletePoi(poi)
    }

}