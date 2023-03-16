package com.sultandev.mytaxi.domain.usecase

import com.google.android.gms.maps.model.LatLng
import com.sultandev.mytaxi.utils.UiState
import kotlinx.coroutines.flow.Flow

interface MapUseCase {

    fun getUserLocation(): Flow<UiState<LatLng>>

}