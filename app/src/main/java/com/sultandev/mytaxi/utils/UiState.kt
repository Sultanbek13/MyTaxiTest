package com.sultandev.mytaxi.utils

sealed class UiState<out R> {

    object Loading : UiState<Nothing>()

    data class Success<out T>(val data: T) : UiState<T>()

    data class Error(val msg: String?) : UiState<Nothing>()

    data class NetworkError(val msg: String?) : UiState<Nothing>()

}