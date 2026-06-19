package com.example.ble_connect.domain.model

import android.bluetooth.BluetoothDevice

data class BleGattCharacteristic (
    val characteristicUuid: String,
    val properties: Int
)