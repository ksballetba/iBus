package com.ksballetba.ibus.data.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "collected_lines")
class CollectedLineEntity constructor(
    @field:ColumnInfo(name = "id")
    @field:PrimaryKey
    val id: String,
    @field:ColumnInfo(name = "creationTimestamp")
    val creationDateTime: Long,
    @field:ColumnInfo(name = "expiresTimestamp")
    val expiresDateTime: Long?,
    @field:ColumnInfo(name = "data", typeAffinity = ColumnInfo.BLOB)
    val data: ByteArray)