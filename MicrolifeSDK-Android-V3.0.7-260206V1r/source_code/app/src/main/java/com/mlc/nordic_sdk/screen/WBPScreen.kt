package com.mlc.nordic_sdk.screen

import com.mlc.nordic_sdk.viewModel.WBPActivityViewModel
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun WBPScreen(viewModel: WBPActivityViewModel) {
    val state by mutableStateOf(viewModel.state)
    val data by viewModel.listData.observeAsState(initial = emptyList())
    var show by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopBar("WBP") }, modifier = Modifier.safeDrawingPadding()) {
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
                            onClick = { viewModel.readDiagnosticModeHistory() }, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(text = "Read diagnostic mode history", fontSize = 8.sp)
                        }
                        Button(
                            onClick = { viewModel.clearCurrentModeHistory() }, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(text = "Clear current mode history", fontSize = 8.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.readUsualModeHistory() }, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(text = "Read usual mode history", fontSize = 8.sp)
                        }
                        Button(
                            onClick = { viewModel.clearSelectedModeHistory() }, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(text = "Clear selected mode history", fontSize = 8.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.disconnectBluetooth() }, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(text = "Disconnect", fontSize = 8.sp)
                        }
                        Button(
                            onClick = { viewModel.readDeviceIDAndInfo() }, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(text = "Read device ID and info", fontSize = 8.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.writeDeviceTime() }, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(text = "Write device time", fontSize = 8.sp)
                        }
                        Button(
                            onClick = { viewModel.readDeviceTime() }, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(text = "Read device time", fontSize = 8.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.readUserIDAndVersionData() }, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(text = "Read user ID and version data", fontSize = 8.sp)
                        }
                        Button(
                            onClick = { viewModel.writeNewUserID() }, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(text = "Write new user ID", fontSize = 8.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.readMeasurementSetting() }, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(text = "Read measurement setting", fontSize = 8.sp)
                        }
                        Button(
                            onClick = {
                                show = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(text = "Write measurement setting", fontSize = 8.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.readSerialNumber() }, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(text = "Read SN", fontSize = 8.sp)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.readUsualModeHistoryEachMeasurement() }, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(text = "Read usual mode history data each measurement", fontSize = 8.sp)
                        }
                    }
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

    var step by remember { mutableStateOf(0) }
    var measureTimes by remember { mutableStateOf(0) }
    var restTime by remember { mutableStateOf(0) }
    var intervalTime by remember { mutableStateOf(0) }
    var excludeAverage by remember { mutableStateOf(0) }
    var afibOption by remember { mutableStateOf(0) }

    when(step) {
        0 -> {
            NumberPickerDialog(
                title = "Measure Times",
                show = show,
                initialValue = measureTimes,
                list = listOf(0, 1, 2, 3),
                onConfirm = {
                    measureTimes = it
                    step++
                    viewModel.addDataList("Measure Times: $it")
                },
                onDismiss = { show = false }
            )
        }
        1 -> {
            NumberPickerDialog(
                title = "Rest Time",
                show = show,
                initialValue = restTime,
                list = listOf(0, 15, 30, 60, 120, 180, 240, 300),
                onConfirm = {
                    restTime = it
                    step++
                    viewModel.addDataList("Rest Time: $it")
                },
                onDismiss = { show = false }
            )
        }
        2 -> {
            NumberPickerDialog(
                title = "Interval Time",
                show = show,
                initialValue = intervalTime,
                list = listOf(0, 15, 30, 60, 120, 180, 240, 300),
                onConfirm = {
                    intervalTime = it
                    step++
                    viewModel.addDataList("Interval Time: $it")
                },
                onDismiss = { show = false }
            )
        }
        3 -> {
            NumberPickerDialog(
                title = "Exclude Average",
                show = show,
                initialValue = excludeAverage,
                list = listOf(0, 1),
                onConfirm = {
                    excludeAverage = it
                    step++
                    viewModel.addDataList("Exclude Average: $it")
                },
                onDismiss = { show = false }
            )
        }
        4 -> {
            NumberPickerDialog(
                title = "AFIB ON/OFF",
                show = show,
                initialValue = afibOption,
                list = listOf(0, 1),
                onConfirm = {
                    afibOption = it
                    viewModel.addDataList("Exclude Average: $it")
                    viewModel.writeMeasurementSetting(
                        measurement_times = measureTimes,
                        rest_time = restTime,
                        interval_time_seconds = intervalTime,
                        exclude_average = excludeAverage,
                        afib_option = afibOption
                    )

                    measureTimes = 0
                    restTime = 0
                    intervalTime = 0
                    excludeAverage = 0
                    afibOption = 0
                    step = 0
                    show = false
                },
                onDismiss = { show = false }
            )
        }
    }
}

@Composable
fun NumberPickerDialog(
    title: String,
    show: Boolean,
    initialValue: Int,
    list: List<Int>,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    if (!show) return

    Dialog(onDismissRequest = onDismiss) {

        val visibleCount = 3
        val itemHeight = 40.dp
        val paddingCount = visibleCount / 2

        val displayList = remember(list) {
            List(paddingCount) { null } +
                    list +
                    List(paddingCount) { null }
        }

        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex =
                list.indexOf(initialValue).coerceAtLeast(0)
        )

        val snapFlingBehavior =
            rememberSnapFlingBehavior(lazyListState = listState)

        val centeredIndex by remember {
            derivedStateOf {
                val layoutInfo = listState.layoutInfo
                if (layoutInfo.visibleItemsInfo.isEmpty()) return@derivedStateOf null

                val viewportCenter =
                    (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

                layoutInfo.visibleItemsInfo.minByOrNull { item ->
                    abs(
                        (item.offset + item.size / 2) - viewportCenter
                    )
                }?.index
            }
        }

        Column(
            modifier = Modifier
                .width(260.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(title, fontSize = 18.sp)

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .height(itemHeight * visibleCount)
                    .fillMaxWidth()
            ) {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    flingBehavior = snapFlingBehavior,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(displayList.size) { index ->
                        val item = displayList[index]
                        val isSelected =
                            index == centeredIndex && item != null

                        Text(
                            text = item?.toString() ?: "",
                            fontSize = if (isSelected) 26.sp else 18.sp,
                            color = if (isSelected)
                                Color(0xFF7B61FF)
                            else
                                Color.Gray,
                            modifier = Modifier
                                .height(itemHeight)
                                .wrapContentHeight(Alignment.CenterVertically)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = -itemHeight / 2),
                    thickness = 2.dp,
                    color = Color(0xFF7B61FF)
                )

                HorizontalDivider(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = itemHeight / 2),
                    thickness = 2.dp,
                    color = Color(0xFF7B61FF)
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val selected =
                        centeredIndex
                            ?.let { displayList[it] }
                            ?: initialValue

                    onConfirm(selected)
                }
            ) {
                Text("OK")
            }
        }
    }
}

@Composable
@Preview
fun PreviewNumberPickerScreen() {
    NumberPickerDialog(
        title = "Number Picker",
        show = true,
        initialValue = 0,
        list = listOf(0, 1, 2, 3),
        onConfirm = { },
        onDismiss = { }
    )
}
