package com.mlc.nordic_sdk.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChoseActivityViewModel : ViewModel() {
    val isPermissionGranted = MutableLiveData<Boolean>()
    val requestPermission = MutableLiveData<Boolean>()

    fun requestPermission() {
        requestPermission.value = true
    }
}