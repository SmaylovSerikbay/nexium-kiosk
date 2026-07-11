package com.mlc.nordic_sdk.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.mlc.nordic_sdk.XlogUtils
import com.mlc.nordic_sdk.viewModel.ChoseActivityViewModel
import com.mlc.nordic_sdk.screen.ChoseScreen

class ChoseActivity : ComponentActivity() {

    private val viewModel by viewModels<ChoseActivityViewModel>()

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.isPermissionGranted.value = checkPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        XlogUtils.initXlog(this, true)

        setObserver()

        if(!checkPermission()) {
            requestPermission()
        } else {
            viewModel.isPermissionGranted.value = true
        }

        setContent {
            ChoseScreen(viewModel)
        }
    }

    private fun setObserver() {
        viewModel.requestPermission.observe(this) {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //Android 12以上
            val isScanPS = (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED)
            val isConnectPS = (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED)

            return !(isScanPS || isConnectPS)
        } else {
            val isBlePS = (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED)
            val isLocationPS = (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)

            return !(isBlePS || isLocationPS)
        }
    }

    private fun requestPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //Android 12以上
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }
}