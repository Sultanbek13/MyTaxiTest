package com.sultandev.mytaxi.presentation.map

import com.google.android.gms.maps.model.LatLng
import com.sultandev.mytaxi.utils.UiState
import kotlinx.coroutines.flow.SharedFlow

interface MapViewModel {

    val userLocationFlow: SharedFlow<UiState<LatLng>>

    fun getUserLocation()

}