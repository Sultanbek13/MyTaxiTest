package com.sultandev.mytaxi.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.sultandev.mytaxi.utils.UiState
import kotlinx.coroutines.flow.Flow

interface MapRepository {

    fun getUserLocation(): Flow<UiState<LatLng>>

}