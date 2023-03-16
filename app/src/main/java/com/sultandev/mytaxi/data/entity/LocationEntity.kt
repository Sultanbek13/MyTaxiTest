package com.sultandev.mytaxi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_location")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val lat: Double,
    val lng: Double
)