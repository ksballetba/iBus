package com.ksballetba.ibus.data.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.core.RouteNode
import com.baidu.mapapi.search.route.PlanNode

@Entity(tableName = "collected_lines")
data class CollectedLineEntity constructor(
    @ColumnInfo(name = "uid")
    @PrimaryKey
    var uid: String = "",
    @ColumnInfo(name = "entranceName")
    var entranceName: String? = null,
    @ColumnInfo(name = "entranceCity")
    var entranceCity: String? = null,
    @ColumnInfo(name = "entranceLatitude")
    var entranceLatitude: Double? = null,
    @ColumnInfo(name = "entranceLongitude")
    var entranceLongitude: Double? = null,
    @ColumnInfo(name = "entranceArea")
    var entranceArea: String? = null,
    @ColumnInfo(name = "entranceType")
    var entranceType: String? = null,
    @ColumnInfo(name = "exitName")
    var exitName: String? = null,
    @ColumnInfo(name = "exitCity")
    var exitCity: String? = null,
    @ColumnInfo(name = "exitLatitude")
    var exitLatitude: Double? = null,
    @ColumnInfo(name = "exitLongitude")
    var exitLongitude: Double? = null,
    @ColumnInfo(name = "exitArea")
    var exitArea: String? = null,
    @ColumnInfo(name = "exitType")
    var exitType: String? = null
) : Parcelable {
    constructor() : this("")

    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readString(),
        source.readValue(Double::class.java.classLoader) as Double?,
        source.readValue(Double::class.java.classLoader) as Double?,
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readValue(Double::class.java.classLoader) as Double?,
        source.readValue(Double::class.java.classLoader) as Double?,
        source.readString(),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(uid)
        writeString(entranceName)
        writeString(entranceCity)
        writeValue(entranceLatitude)
        writeValue(entranceLongitude)
        writeString(entranceArea)
        writeString(entranceType)
        writeString(exitName)
        writeString(exitCity)
        writeValue(exitLatitude)
        writeValue(exitLongitude)
        writeString(exitArea)
        writeString(exitType)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CollectedLineEntity> = object : Parcelable.Creator<CollectedLineEntity> {
            override fun createFromParcel(source: Parcel): CollectedLineEntity = CollectedLineEntity(source)
            override fun newArray(size: Int): Array<CollectedLineEntity?> = arrayOfNulls(size)
        }
    }
}