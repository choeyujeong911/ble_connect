package com.example.ble_connect.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble_connect.data.ble.BleManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BleViewModel(application: Application) : AndroidViewModel(application) {
    // 장치 목록을 관리하는 State
    private val _foundDevicesCount = mutableStateOf(0)
    val foundDevicesCount: State<Int> =_foundDevicesCount

    // BleManager 인스턴스 생성
    private val bleManager = BleManager(application.applicationContext)

    fun startScanningProcess(hasPermission: Boolean, onCountReady: (Int) -> Unit) {
        if (!hasPermission) return

        // ViewModel 내부의 코루틴 스코프에서 실행
        viewModelScope.launch {
            // BleManager로 실제 스캔 시작 로직 호출
            bleManager.startScan { }

            // 5초 동안 스캔할 수 있도록 대기
            delay(5000)

            // 5초 뒤 스캔 중지
            bleManager.stopScan()

            // 스캔된 최종 장치 개수 파악 및 알림
            val count = bleManager.getScannedCount()
            _foundDevicesCount.value = count
            onCountReady(count)
        }
    }
}