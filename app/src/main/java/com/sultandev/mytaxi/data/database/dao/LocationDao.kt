package com.sultandev.mytaxi.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sultandev.mytaxi.data.entity.LocationEntity
import com.sultandev.mytaxi.utils.UiState
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserLocation(location: LocationEntity)

    @Query("SELECT * FROM user_location")
    fun getUserLocation(): Flow<List<LocationEntity>>

}