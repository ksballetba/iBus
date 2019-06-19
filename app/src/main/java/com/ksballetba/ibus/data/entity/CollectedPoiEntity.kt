package com.ksballetba.ibus.data.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.baidu.mapapi.model.LatLng

@Entity(tableName = "collected_pois")
data class CollectedPoiEntity constructor(
    @ColumnInfo(name = "uid")
    @PrimaryKey
    var uid: String = "",
    @ColumnInfo(name = "name")
    var name: String? = null,
    @ColumnInfo(name = "city")
    var city: String? = null,
    @ColumnInfo(name = "latitude")
    var latitude: Double? = null,
    @ColumnInfo(name = "longitude")
    var longitude: Double? = null,
    @ColumnInfo(name = "address")
    var address: String? = null,
    @ColumnInfo(name = "type")
    var type: String? = null
) {
    constructor() : this("")
}