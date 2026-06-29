package com.example.ble_connect.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ble_connect.domain.model.BleDevice
import com.example.ble_connect.domain.model.BleGattService
import com.example.ble_connect.viewmodel.BleViewModel

@Composable
fun DeviceScreen(
    modifier: Modifier,
    viewModel: BleViewModel = viewModel()
) {
    val receivedValue by viewModel.receivedValue
    val services = viewModel.services

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(text = "Received: $receivedValue")
        }

        item {
            services.value.firstOrNull()?.let { service ->
                Text(text = service.serviceUuid)
            }
        }
    }
}