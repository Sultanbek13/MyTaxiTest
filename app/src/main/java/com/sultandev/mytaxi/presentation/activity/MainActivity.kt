package com.sultandev.mytaxi.presentation.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.sultandev.mytaxi.R
import com.sultandev.mytaxi.service.MapService
import com.sultandev.mytaxi.utils.*

class MainActivity : AppCompatActivity() {

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.globalContainer) as NavHostFragment
        navController = navHostFragment.findNavController()

        navGraphConnect()

    }

    override fun onResume() {
        super.onResume()
        startService()
    }

    private fun navGraphConnect() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.globalContainer) as NavHostFragment
        navHostFragment.findNavController()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.SUCCESSFULLY_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showMessage(getString(R.string.have_location_permission))
                startService()
            } else {
                showMessage(getString(R.string.not_have_location_permission))
            }
        }
    }

    private fun startService() {
        if (isLocationEnabled()) {
            if (checkPermissions()) {
                val intent = Intent(this, MapService::class.java)
                ContextCompat.startForegroundService(this, intent)
                successPermissionListener.value = Unit
            } else {
                requestPermissions()
            }
        } else {
            confirmationDialog()
        }
    }

    private fun confirmationDialog() {
        AlertDialog.Builder(this)
            .apply {
                setCancelable(false)
                setTitle(getString(R.string.location_needed))
                setMessage("\n")
                setPositiveButton(getString(R.string.turn_on)) { _, _ ->
                    openSetting()
                }
                setNeutralButton(getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                    showMessage(getString(R.string.location_needed))
                }
                create()
                show()
            }
    }
}