package com.sultandev.mytaxi.presentation.map

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.maps.model.LatLng
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.animation.CameraAnimatorOptions.Companion.cameraAnimatorOptions
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.locationcomponent.location2
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import com.sultandev.mytaxi.R
import com.sultandev.mytaxi.databinding.FragmentMapBinding
import com.sultandev.mytaxi.presentation.map.impl.MapViewModelImpl
import com.sultandev.mytaxi.utils.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapFragment : Fragment(R.layout.fragment_map) {

    private val viewBinding: FragmentMapBinding by viewBinding()
    private val viewModel: MapViewModelImpl by viewModel()
    private var userLatLng: LatLng? = null
    private var firstAnimState = false
    private var pointAnnotationManager: PointAnnotationManager? = null
    private var pointAnnotation: PointAnnotation? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onCreate(savedInstanceState)

        mapBoxSettings()

        successPermissionListener.observe(viewLifecycleOwner) {
            getUserLocation()
        }

        locationStateListener.observe(viewLifecycleOwner) {
            when (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    onMapReadyDay(it.longitude, it.latitude)
                    firstAnimState = true
                }
                Configuration.UI_MODE_NIGHT_YES -> {
                    onMapReadyNight(it.longitude, it.latitude)
                    firstAnimState = true
                }
            }
        }

        setObserver()

        buttonsObservers()

    }

    private fun onMapReadyDay(lng: Double, lat: Double) {

        setCamera(lng, lat)

        viewBinding.apply {
            mapView.getMapboxMap().loadStyleUri(
                Style.MAPBOX_STREETS
            ) {
                mapApproximationCamera()
            }
        }
    }

    private fun onMapReadyNight(lng: Double, lat: Double) {

        setCamera(lng, lat)

        viewBinding.apply {
            mapView.getMapboxMap().loadStyleUri(
                Style.DARK
            ) {
                mapApproximationCamera()
            }
        }
    }

    private fun mapApproximationCamera() {
        viewBinding.mapView.camera.apply {
            val bearing = createBearingAnimator(cameraAnimatorOptions(-20.0)) {
                duration = 2000
                interpolator = AccelerateDecelerateInterpolator()
            }
            val zoom = createZoomAnimator(
                cameraAnimatorOptions(14.0) {
                    startValue(3.0)
                }
            ) {
                duration = 3000
                interpolator = AccelerateDecelerateInterpolator()
            }
            val pitch = createPitchAnimator(
                cameraAnimatorOptions(20.0) {
                    startValue(0.0)
                }
            ) {
                duration = 2000
                interpolator = AccelerateDecelerateInterpolator()
            }
            playAnimatorsSequentially(zoom, pitch, bearing)
        }
    }

    private fun setCamera(lng: Double, lat: Double) {
        viewBinding.mapView.getMapboxMap().setCamera(cameraOptions {
            if (userLatLng != null) {
                center(Point.fromLngLat(lng, lat))
                zoom(3.0)
            }
        })
    }

    private fun mapBoxSettings() {
        viewBinding.apply {

            mapView.logo.updateSettings {
                this.enabled = false
            }

            mapView.attribution.updateSettings {
                this.enabled = false
            }

            mapView.location.updateSettings {
                this.enabled = false
            }

            mapView.location2.updateSettings {
                this.enabled = false
            }

            mapView.scalebar.updateSettings {
                this.enabled = false
            }

            mapView.compass.updateSettings {
                this.enabled = false
            }
        }
    }

    private fun addMarker(lng: Double, lat: Double) {
        bitmapFromDrawableRes(
            requireContext(),
            R.drawable.car
        )?.let {
            val annotationApi = viewBinding.mapView.annotations
            pointAnnotationManager = annotationApi.createPointAnnotationManager()
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(lng, lat))
                .withIconImage(it)
            if (pointAnnotationManager != null) {
                pointAnnotation = pointAnnotationManager!!.create(pointAnnotationOptions)
            }
        }
    }

    private fun removeMarker() {
        if (pointAnnotation != null) {
            pointAnnotationManager?.delete(pointAnnotation!!)
        }
    }

    private fun getUserLocation() {
        if (isLocationEnabled()) {
            if (checkPermissions()) {
                viewModel.getUserLocation()
            }
        }
    }

    private fun setObserver() {
        viewModel.userLocationFlow.onEach {
            when (it) {
                is UiState.Success -> {
                    setLoading(false)
                    val lat = it.data.latitude
                    val lng = it.data.longitude
                    userLatLng = LatLng(lat, lng)
                    if (userLatLng != null) {
                        if (!firstAnimState) {
                            addMarker(lng, lat)
                            locationStateListener.value = userLatLng
                        } else {
                            removeMarker()
                            addMarker(lng, lat)
                            changeLocationWithAnim(userLatLng!!)
                        }
                    }
                }

                is UiState.Error -> {
                    setLoading(false)
                    showMessage(it.msg!!)
                }

                is UiState.Loading -> {
                    setLoading(true)
                }

                is UiState.NetworkError -> {
                    setLoading(false)
                    showMessage(it.msg.toString())
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun changeLocationWithAnim(userLatLng: LatLng) {

        val cameraOptions = cameraOptions {
            center(Point.fromLngLat(userLatLng.longitude, userLatLng.latitude))
        }

        val animationOptions = MapAnimationOptions.mapAnimationOptions {
            this.duration(1000)
        }

        viewBinding.mapView.camera.apply {
            flyTo(cameraOptions, animationOptions)
        }
    }

    private fun myLocation(userLatLng: LatLng) {

        val cameraOptions = cameraOptions {
            center(Point.fromLngLat(userLatLng.longitude, userLatLng.latitude))
        }

        val animationOptions = MapAnimationOptions.mapAnimationOptions {
            this.duration(2000)
        }

        viewBinding.mapView.camera.apply {
            flyTo(cameraOptions, animationOptions)
            val zoom = createZoomAnimator(cameraAnimatorOptions(14.0)) {
                duration = 2000
                interpolator = AnticipateOvershootInterpolator()
            }
            val pitch = createPitchAnimator(cameraAnimatorOptions(10.0)) {
                duration = 2000
            }
            playAnimatorsSequentially(zoom, pitch)
        }
    }

    private fun zoomIn() {
        val mapboxMap = viewBinding.mapView.getMapboxMap()
        val zoomLevel = mapboxMap.cameraState.zoom + 1

        mapboxMap.flyTo(cameraOptions {
            zoom(zoomLevel)
        })
    }

    private fun zoomOut() {
        val mapboxMap = viewBinding.mapView.getMapboxMap()
        val zoomLevel = mapboxMap.cameraState.zoom - 1

        mapboxMap.flyTo(cameraOptions {
            zoom(zoomLevel)
        })
    }


    private fun buttonsObservers() {
        viewBinding.apply {
            btnZoomin.setOnClickListener {
                zoomIn()
            }

            btnZoomout.setOnClickListener {
                zoomOut()
            }

            btnMyLocation.setOnClickListener {
                if (userLatLng != null) {
                    myLocation(userLatLng!!)
                } else {
                    showMessage(getString(R.string.no_location))
                }
            }

            btnMore.setOnClickListener {
                showMessage(getString(R.string.more))
            }

            btnNotification.setOnClickListener {
                showMessage(getString(R.string.notification))
            }

            btnSpark.setOnClickListener {}

            btnOrders.setOnClickListener {
                showMessage(getString(R.string.orders))
            }

            btnCurb.setOnClickListener {
                showMessage(getString(R.string.curb))
            }

            btnRates.setOnClickListener {
                showMessage(getString(R.string.rates))
            }
        }
    }

    private fun setLoading(loadingState: Boolean) {
        viewBinding.apply {
            progressBar.isVisible = loadingState
            mapView.isVisible = !loadingState
        }
    }


    @SuppressLint("Lifecycle")
    override fun onStart() {
        super.onStart()
        viewBinding.mapView.onStart()
    }

    @SuppressLint("Lifecycle")
    override fun onStop() {
        super.onStop()
        viewBinding.mapView.onStop()
    }

    @SuppressLint("Lifecycle")
    override fun onLowMemory() {
        super.onLowMemory()
        viewBinding.mapView.onLowMemory()
    }

    @SuppressLint("Lifecycle")
    override fun onDestroy() {
        super.onDestroy()
        viewBinding.mapView.onDestroy()
    }
}