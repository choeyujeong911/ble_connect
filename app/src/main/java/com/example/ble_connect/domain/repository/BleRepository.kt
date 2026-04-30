package com.example.ble_connect.domain.repository

import com.example.ble_connect.domain.model.BleDevice

interface BleRepository {
    fun startScan()
    fun stopScan()
    fun getScannedCount(): Int
    fun getScannedDevices(): List<BleDevice>

    fun connectToDevice(
        device: BleDevice,
        onConnected: (Boolean) -> Unit
    )

    fun disconnectDevice()

}