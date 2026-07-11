package com.mlc.nordic_sdk.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mlc.nordic_sdk.viewModel.PFMActivityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun PFMScreen(viewModel: PFMActivityViewModel) {
    val state by mutableStateOf(viewModel.state)
    val data by viewModel.listData.observeAsState(initial = emptyList())

    Scaffold(topBar = { TopBar("PFM") }, modifier = Modifier.safeDrawingPadding()) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when(state) {
                true -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.readAllHistory() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                            Text(text = "Read All History", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { viewModel.clearAllHistory() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                            Text(text = "Clear All History", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { viewModel.disconnectBluetooth() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                            Text(text = "disconnect", fontSize = 10.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.readUserIdAndVersionData() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                            Text(text = "Read User And Version Data", fontSize = 10.sp)
                        }
                        Button(onClick = { viewModel.writeNewUser() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                            Text(text = "Write new user id", fontSize = 10.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.readLastData() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                            Text(text = "Read Last Data", fontSize = 10.sp)
                        }
                        Button(onClick = { viewModel.clearLastData() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                            Text(text = "Clear Last Data", fontSize = 10.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.readDeviceTime() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                            Text(text = "Read Device Time", fontSize = 10.sp)
                        }
                        Button(onClick = { viewModel.writeDeviceTime() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                            Text(text = "Write Device Time", fontSize = 10.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.readSerialNumber() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                            Text(text = "Read SN", fontSize = 10.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.readBestValue() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                            Text(text = "Read Best Value", fontSize = 10.sp)
                        }
                        Button(onClick = { viewModel.writeBestValue() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                            Text(text = "Write Best Value", fontSize = 10.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.checkMode() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                            Text(text = "Check mode", fontSize = 10.sp)
                        }
                    }
                    /*
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.startMeasurement() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                            Text(text = "Start Measurement", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { viewModel.readWaveform() }, modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)) {
                            Text(text = "Read Waveform", fontSize = 10.sp)
                        }
                    }

                     */
                }
                else -> {}
            }
            Text(
                text = "Log",
                modifier = Modifier
                    .background(Color.Gray)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )

            // Remember a CoroutineScope to be able to launch
            val listState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(data) { index ->
                    TextItem(
                        text = index,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, color = Color(220, 220, 220))
                    )
                }
                coroutineScope.launch {
                    // Animate scroll to the last item
                    listState.animateScrollToItem(index = data.size)
                }
            }
        }
    }
}