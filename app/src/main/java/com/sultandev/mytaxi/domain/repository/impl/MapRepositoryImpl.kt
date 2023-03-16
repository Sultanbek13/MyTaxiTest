package com.sultandev.mytaxi.domain.repository.impl

import com.google.android.gms.maps.model.LatLng
import com.sultandev.mytaxi.data.database.dao.LocationDao
import com.sultandev.mytaxi.domain.repository.MapRepository
import com.sultandev.mytaxi.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

class MapRepositoryImpl(private val locationDao: LocationDao) :
    MapRepository {

    override fun getUserLocation(): Flow<UiState<LatLng>> = callbackFlow {
        trySend(UiState.Loading)
        locationDao.getUserLocation().collectLatest {
            if(it.isNotEmpty()) {
                val lat = it.last().lat
                val lng = it.last().lng
                val latLng = LatLng(lat, lng)
                trySend(UiState.Success(latLng))
            } else {
                trySend(UiState.Error("Что-то не так.. Не можем обробатывать вашу локацию"))
            }
        }
        awaitClose {}
    }.catch {
        emit(UiState.Error(it.message.toString()))
    }.flowOn(Dispatchers.IO)

}