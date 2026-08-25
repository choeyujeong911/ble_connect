package com.example.ble_connect.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ble_connect.domain.model.BleDevice
import com.example.ble_connect.domain.model.BleGattService
import com.example.ble_connect.viewmodel.BleViewModel

@Composable
fun DeviceScreen(
    modifier: Modifier,
    viewModel: BleViewModel = viewModel(),
    hasCameraPermission: Boolean = false
) {
    val receivedValue by viewModel.receivedValue
    val services = viewModel.services
    var previousValue by remember { mutableStateOf(receivedValue.trim().uppercase()) }
    var captureRequest by remember { mutableIntStateOf(0) }

    LaunchedEffect(receivedValue) {
        val currentValue = receivedValue.trim().uppercase()
        if (previousValue == "OFF" && currentValue == "ON") {
            captureRequest++
        }
        previousValue = currentValue
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            if (hasCameraPermission) {
                CameraScreen(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    captureRequest = captureRequest
                )
            } else {
                Text("카메라 권한이 필요합니다.")
            }
        }

        item {
            Text(text = "Received: $receivedValue")
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.writeValue("0")
                    }
                ) {
                    Text(text = "0")
                }

                Button(
                    onClick = {
                        viewModel.writeValue("1")
                    }
                ) {
                    Text(text = "1")
                }
            }
        }

        item {
            services.value.firstOrNull()?.let { service ->
                Text(text = service.serviceUuid)
            }
        }
    }
}
