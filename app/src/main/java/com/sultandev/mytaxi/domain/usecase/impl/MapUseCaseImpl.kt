package com.sultandev.mytaxi.domain.usecase.impl

import com.google.android.gms.maps.model.LatLng
import com.sultandev.mytaxi.domain.repository.MapRepository
import com.sultandev.mytaxi.domain.usecase.MapUseCase
import com.sultandev.mytaxi.utils.UiState
import kotlinx.coroutines.flow.Flow

class MapUseCaseImpl(private val mapRepository: MapRepository) : MapUseCase {

    override fun getUserLocation(): Flow<UiState<LatLng>> {
        return mapRepository.getUserLocation()
    }
}