package com.sultandev.mytaxi.utils

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

val locationStateListener: MutableLiveData<LatLng> = MutableLiveData()

val successPermissionListener: MutableLiveData<Unit> = MutableLiveData()