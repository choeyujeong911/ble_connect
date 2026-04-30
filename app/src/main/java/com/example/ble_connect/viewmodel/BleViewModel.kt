package com.example.ble_connect.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble_connect.data.ble.BleManager
import com.example.ble_connect.data.repository.BleRepositoryImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ble_connect.domain.model.BleDevice

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

    private val _serviceUuids = mutableStateOf<List<String>>(emptyList())
    val serviceUuids: State<List<String>> = _serviceUuids

    private val repository = BleRepositoryImpl(BleManager(application.applicationContext))

    /*
     매개변수 1: hasPermission => 권한 여부
     매개변수 2: onCountReady(Int) => Unit 타입을 반환(void와 동일)하는, Int 타입의 매개변수를 받는 어떠한 함수
     매개변수 2는 코틀린의 후행 람다라는 문법에 의해 매개변수 집어넣는 소괄호 밖으로 튀어나와서 중괄호 형태로 나타낼 수 있음
     예를 들어 BleScreen.kt에서 호출할 때,
     ##viewModel.startScanningProcess(hasPermission) { Int 변수명 -> 실행문 }## 에서
     ##{ Int 변수명 -> 실행문 }## 표현과 ##익명함수(변수명: Int) { 실행문 }## 표현이 동일 의미!
     */
    fun startScanningProcess(hasPermission: Boolean, onCountReady: (Int) -> Unit) {
        if (!hasPermission) return

        // ViewModel 내부의 코루틴 스코프에서 실행
        viewModelScope.launch {
            // 스캔 시작 상태로 변경
            _isScanning.value = true

            // BleManager로 실제 스캔 시작 로직 호출
            repository.startScan()

            // 5초 동안 스캔할 수 있도록 대기
            delay(5000)

            // 5초 뒤 스캔 중지
            repository.stopScan()

            // 스캔된 최종 장치 개수 파악 및 알림
            val count = repository.getScannedCount()
            _foundDevicesCount.value = count
            // 매개변수 2의 람다함수에 Int형의 count 값을 전달함
            onCountReady(count)

            // 스캔된 장치 목록 받아오기
            val list = repository.getScannedDevices()
            _devices.clear()
            _devices.addAll(list)

            // 스캔 종료
            _isScanning.value = false
        }
    }

    fun connectToDevice(device: BleDevice) {
        repository.connectToDevice(
            device = device,
            onConnected = { connected ->
                _isConnected.value = connected
            },
            onServiceUuidReceived = { uuids ->
                _serviceUuids.value = uuids
            }
        )
    }

    fun disconnectDevice() {
        repository.disconnectDevice()
        _isConnected.value = false
    }
}