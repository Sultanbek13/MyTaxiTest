package com.sultandev.mytaxi.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sultandev.mytaxi.data.database.dao.LocationDao
import com.sultandev.mytaxi.data.entity.LocationEntity

@Database(entities = [LocationEntity::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun locationDao(): LocationDao

}