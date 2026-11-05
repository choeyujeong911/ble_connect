package com.example.ble_connect

import android.bluetooth.BluetoothDevice

data class BleDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val device: BluetoothDevice,
    val services: List<BleGattService> = emptyList()
)

data class BleGattService(
    val serviceUuid: String,
    val characteristic: List<BleGattCharacteristic>
)

data class BleGattCharacteristic(
    val characteristicUuid: String,
    val properties: Int
)
