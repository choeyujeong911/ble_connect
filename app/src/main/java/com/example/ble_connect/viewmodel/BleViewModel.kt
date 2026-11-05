package com.example.ble_connect.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble_connect.BleDevice
import com.example.ble_connect.BleGattCharacteristic
import com.example.ble_connect.BleGattService
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private class BluetoothController(context: Context) {
    private val appContext = context.applicationContext
    private val adapter = (appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val scanner: BluetoothLeScanner? = adapter?.bluetoothLeScanner
    private val scanResults = mutableListOf<ScanResult>()
    private var scanCallback: ScanCallback? = null
    private var gatt: BluetoothGatt? = null
    private var connectedDevice: BleDevice? = null
    private var connectionCallback: ((Boolean) -> Unit)? = null
    private var deviceCallback: ((BleDevice) -> Unit)? = null
    private var valueCallback: ((String) -> Unit)? = null
    private var disconnectCallback: (() -> Unit)? = null
    private val _receivedValue = MutableStateFlow("test")
    val receivedValue: StateFlow<String> = _receivedValue

    @SuppressLint("MissingPermission")
    fun startScan(onDeviceFound: (BleDevice) -> Unit) {
        scanner ?: return
        scanCallback?.let(scanner::stopScan)
        scanResults.clear()
        scanCallback = object : ScanCallback() {
            override fun onScanResult(type: Int, result: ScanResult) {
                val index = scanResults.indexOfFirst { it.device.address == result.device.address }
                if (index < 0) scanResults.add(result) else scanResults[index] = result
                onDeviceFound(result.toBleDevice())
            }
            override fun onScanFailed(errorCode: Int) = Unit
        }.also(scanner::startScan)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanCallback?.let { scanner?.stopScan(it) }
        scanCallback = null
    }

    @SuppressLint("MissingPermission")
    fun connect(
        address: String,
        onConnected: (Boolean) -> Unit,
        onDeviceUpdated: (BleDevice) -> Unit,
        onValueReceived: (String) -> Unit
    ) {
        val result = scanResults.firstOrNull { it.device.address == address }
        if (result == null || gatt != null) {
            onConnected(false)
            return
        }
        connectedDevice = result.toBleDevice()
        connectionCallback = onConnected
        deviceCallback = onDeviceUpdated
        valueCallback = onValueReceived
        gatt = result.device.connectGatt(appContext, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnect(onDisconnected: () -> Unit) {
        val currentGatt = gatt ?: return onDisconnected()
        disconnectCallback = onDisconnected
        currentGatt.disconnect()
    }

    @SuppressLint("MissingPermission")
    fun write(value: String): Boolean {
        val currentGatt = gatt ?: return false
        val service = currentGatt.services.firstOrNull { service ->
            service.characteristics.any { it.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0 }
        } ?: return false
        val characteristic = service.characteristics.firstOrNull {
            it.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0 ||
                it.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0
        } ?: return false
        characteristic.value = value.toByteArray()
        return currentGatt.writeCharacteristic(characteristic)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(currentGatt: BluetoothGatt, status: Int, newState: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                currentGatt.close()
                if (gatt == currentGatt) gatt = null
                connectionCallback?.invoke(false)
                disconnectCallback?.invoke()
                disconnectCallback = null
                return
            }
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    gatt = currentGatt
                    connectionCallback?.invoke(true)
                    currentGatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    currentGatt.close()
                    if (gatt == currentGatt) gatt = null
                    connectionCallback?.invoke(false)
                    disconnectCallback?.invoke()
                    disconnectCallback = null
                }
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(currentGatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) return
            currentGatt.services.forEach { service ->
                service.characteristics.forEach { characteristic ->
                    if (characteristic.properties == 18) subscribe(currentGatt, characteristic)
                }
            }
            val services = currentGatt.services.map { service ->
                BleGattService(
                    service.uuid.toString(),
                    service.characteristics.map { characteristic ->
                        BleGattCharacteristic(characteristic.uuid.toString(), characteristic.properties)
                    }
                )
            }
            connectedDevice = connectedDevice?.copy(services = services)
            connectedDevice?.let { deviceCallback?.invoke(it) }
        }

        override fun onCharacteristicChanged(currentGatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val value = characteristic.value.decodeToString()
            Handler(Looper.getMainLooper()).post {
                _receivedValue.value = value
                valueCallback?.invoke(value)
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun subscribe(currentGatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        currentGatt.setCharacteristicNotification(characteristic, true)
        characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))?.let {
            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            currentGatt.writeDescriptor(it)
        }
    }

    @SuppressLint("MissingPermission")
    private fun ScanResult.toBleDevice() = BleDevice(
        name = device.name ?: "Unknown",
        address = device.address,
        rssi = rssi,
        device = device
    )
}

class BleViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private var controller: BluetoothController? = null
        private fun controller(application: Application) = controller
            ?: BluetoothController(application).also { controller = it }
    }

    private val ble = controller(application)
    private val _devices = mutableStateListOf<BleDevice>()
    private val _services = mutableStateOf<List<BleGattService>>(emptyList())
    private val _connectedDevice = mutableStateOf<BleDevice?>(null)
    private val _isScanning = mutableStateOf(false)
    private val _isConnected = mutableStateOf(false)
    private val _isDiscoveringServices = mutableStateOf(false)
    private val _receivedValue = mutableStateOf("test")

    init {
        viewModelScope.launch {
            ble.receivedValue.collect { _receivedValue.value = it }
        }
    }

    val devices: List<BleDevice> = _devices
    val services: State<List<BleGattService>> = _services
    val connectedDevice: State<BleDevice?> = _connectedDevice
    val isScanning: State<Boolean> = _isScanning
    val isConnected: State<Boolean> = _isConnected
    val isDiscoveringServices: State<Boolean> = _isDiscoveringServices
    val receivedValue: State<String> = _receivedValue

    fun startScanning() {
        if (_isScanning.value) return
        _isScanning.value = true
        ble.startScan { device ->
            val index = _devices.indexOfFirst { it.address == device.address }
            if (index < 0) _devices.add(device) else _devices[index] = device
        }
    }

    fun refreshScan() = _devices.clear()

    fun stopScanning() {
        ble.stopScan()
        _isScanning.value = false
    }

    fun connectToDevice(device: BleDevice) {
        _services.value = emptyList()
        _connectedDevice.value = device
        _isDiscoveringServices.value = true
        ble.connect(device.address, { connected ->
            _isConnected.value = connected
            if (!connected) resetConnection()
        }, { updated ->
            _connectedDevice.value = updated
            _services.value = updated.services
            _isDiscoveringServices.value = false
        }, { value -> _receivedValue.value = value })
    }

    fun disconnectDevice(onDisconnected: () -> Unit = {}) {
        ble.disconnect {
            resetConnection()
            onDisconnected()
        }
    }

    fun writeValue(value: String) = ble.write(value)

    private fun resetConnection() {
        _isConnected.value = false
        _connectedDevice.value = null
        _services.value = emptyList()
        _receivedValue.value = "test"
        _isDiscoveringServices.value = false
    }
}
