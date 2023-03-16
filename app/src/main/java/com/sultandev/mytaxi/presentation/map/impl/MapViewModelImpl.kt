package com.sultandev.mytaxi.presentation.map.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.sultandev.mytaxi.domain.usecase.MapUseCase
import com.sultandev.mytaxi.presentation.map.MapViewModel
import com.sultandev.mytaxi.utils.UiState
import com.sultandev.mytaxi.utils.hasConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MapViewModelImpl(private val mapUseCase: MapUseCase) :
    MapViewModel, ViewModel() {

    override val userLocationFlow: MutableSharedFlow<UiState<LatLng>> = MutableSharedFlow()

    override fun getUserLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!hasConnection()) {
                userLocationFlow.emit(UiState.NetworkError("Необходим интернет"))
            } else {
                userLocationFlow.emit(UiState.Loading)
                mapUseCase.getUserLocation().collectLatest {
                    when (it) {
                        is UiState.Success -> {
                            userLocationFlow.emit(UiState.Success(it.data))
                        }
                        is UiState.Error -> {
                            userLocationFlow.emit(UiState.Error(it.msg))
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}