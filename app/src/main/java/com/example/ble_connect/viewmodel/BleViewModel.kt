package com.example.ble_connect.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble_connect.data.ble.BleManager
import com.example.ble_connect.data.repository.BleRepositoryImpl
import kotlinx.coroutines.launch
import com.example.ble_connect.domain.model.BleDevice
import com.example.ble_connect.domain.model.BleGattService

class BleViewModel(application: Application) : AndroidViewModel(application) {
    // 장치 목록을 관리하는 State
    private val _foundDevicesCount = mutableStateOf(0)
    val foundDevicesCount: State<Int> = _foundDevicesCount

    // 스캔 중인지를 표현하는 변수 정의(버튼 디자인 변경을 위함)
    private val _isScanning = mutableStateOf(false)
    val isScanning: State<Boolean> = _isScanning

    // 장치 연결 상태를 표현하는 변수 정의
    private val _isConnected = mutableStateOf(false)
    val isConnected: State<Boolean> = _isConnected

    private val _devices = mutableStateListOf<BleDevice>()
    val devices: List<BleDevice> = _devices

    private val _connectedDevice = mutableStateOf<BleDevice?>(null)
    val connectedDevice: State<BleDevice?> = _connectedDevice

    private val _isDiscoveringServices = mutableStateOf(false)
    val isDiscoveringServices: State<Boolean> = _isDiscoveringServices

    private val _services= mutableStateOf<List<BleGattService>>(emptyList())
    val services: State<List<BleGattService>> = _services

    private val _receivedValue = mutableStateOf("test")
    val receivedValue: State<String> = _receivedValue

    private val repository = BleRepositoryImpl(BleManager.getInstance(application.applicationContext))

    init {
        viewModelScope.launch {
            repository.receivedValue.collect { value ->
                _receivedValue.value = value
            }
        }
    }

    /*
     매개변수 1: hasPermission => 권한 여부
     매개변수 2: onCountReady(Int) => Unit 타입을 반환(void와 동일)하는, Int 타입의 매개변수를 받는 어떠한 함수
     매개변수 2는 코틀린의 후행 람다라는 문법에 의해 매개변수 집어넣는 소괄호 밖으로 튀어나와서 중괄호 형태로 나타낼 수 있음
     예를 들어 BleScreen.kt에서 호출할 때,
     ##viewModel.startScanningProcess(hasPermission) { Int 변수명 -> 실행문 }## 에서
     ##{ Int 변수명 -> 실행문 }## 표현과 ##익명함수(변수명: Int) { 실행문 }## 표현이 동일 의미!
     */

    fun startScanning() {
        if (_isScanning.value) return
        _isScanning.value = true

        repository.startScan { device ->
            val index = _devices.indexOfFirst {
                it.address == device.address
            }

            if (index == -1) _devices.add(device)
            else _devices[index] = device

            _foundDevicesCount.value = _devices.size
        }
    }

    fun refreshScan() {
        repository.stopScan()
        _isScanning.value = false
        _devices.clear()
        _foundDevicesCount.value = 0
        startScanning()
    }

    fun connectToDevice(device: BleDevice) {
        _services.value = emptyList()
        _connectedDevice.value = device
        _isDiscoveringServices.value = true

        repository.connectToDevice(
            device = device,
            onConnected = { connected ->
                _isConnected.value = connected

                if (!connected) {
                    _connectedDevice.value = null
                    _services.value = emptyList()
                    _receivedValue.value = "test"
                    _isDiscoveringServices.value = false
                }
            },
            onDeviceUpdated = { updatedDevice ->
                _connectedDevice.value = updatedDevice
                _services.value = updatedDevice.services
                _isDiscoveringServices.value = false
            },
            onValueReceived = { value ->
                _receivedValue.value = value
            }
        )
    }

    fun stopScanning() {
        repository.stopScan()
        _isScanning.value = false
    }

    fun disconnectDevice() {
        repository.disconnectDevice()
        _isConnected.value = false
        _connectedDevice.value = null
        _services.value = emptyList()
        _isDiscoveringServices.value = false
    }

    fun writeValue(value: String): Boolean {
        return repository.writeValue(value)
    }
}