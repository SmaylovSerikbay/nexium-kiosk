package com.mlc.nordic_sdk.screen

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mlc.nordic_sdk.activity.BPMActivity
import com.mlc.nordic_sdk.activity.BGMActivity
import com.mlc.nordic_sdk.activity.BaseActivity
import com.mlc.nordic_sdk.activity.PFMActivity
import com.mlc.nordic_sdk.activity.SPO2Activity
import com.mlc.nordic_sdk.activity.ThermoActivity
import com.mlc.nordic_sdk.activity.WBPActivity
import com.mlc.nordic_sdk.viewModel.ChoseActivityViewModel

@Composable
fun ChoseScreen(viewModel: ChoseActivityViewModel) {
    val context = LocalContext.current
    val isPermissionGranted by viewModel.isPermissionGranted.observeAsState()

    if(isPermissionGranted == true) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { context.startActivity(Intent(context, BPMActivity::class.java)) }) {
                Text(text = "BPM")
            }
            Button(onClick = { context.startActivity(Intent(context, WBPActivity::class.java)) }) {
                Text(text = "WBP")
            }
            Button(onClick = { context.startActivity(Intent(context, ThermoActivity::class.java)) }) {
                Text(text = "Thermo")
            }
            Button(onClick = { context.startActivity(Intent(context, SPO2Activity::class.java)) }) {
                Text(text = "SPO2")
            }
            Button(onClick = { context.startActivity(Intent(context, PFMActivity::class.java)) }) {
                Text(text = "PFM")
            }
            Button(onClick = { context.startActivity(Intent(context, BGMActivity::class.java)) }) {
                Text(text = "BGM")
            }
            Button(onClick = { context.startActivity(Intent(context, BaseActivity::class.java)) }) {
                Text(text = "Base")
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { viewModel.requestPermission() }) {
                Text(text = "Request Permission")
            }
        }
    }
}