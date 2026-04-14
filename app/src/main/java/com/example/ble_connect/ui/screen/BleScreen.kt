package com.example.ble_connect.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ble_connect.ui.theme.Ble_connectTheme
import com.example.ble_connect.viewmodel.BleViewModel

// 현재 앱이 블루투스 스캔 권한을 가지고 있는지 확인하는 함수
fun checkBluetoothPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12 이상: BLUETOOTH_SCAN 권한 확인
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // Android 11 이하: ACCESS_FINE_LOCATION 권한 확인
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun ScanButton(viewModel: BleViewModel = viewModel()) {
    val context = LocalContext.current  // Toast를 위한 임시 변수(권한 체크를 위한 것)
    Button(
        onClick = {
            val hasPermission = checkBluetoothPermission(context)

            if (hasPermission) {
                Toast.makeText(context, "블루투스 권한 있음!! 5초 스캔 시작", Toast.LENGTH_SHORT).show()

                // ViewModel에 스캔 프로세스 요청
                viewModel.startScanningProcess(hasPermission) { count ->
                    Toast.makeText(context, "장치 ${count}개 검색됨", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "권한 없음", Toast.LENGTH_SHORT).show()
            } },
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF0088FF),
            contentColor = Color.White
        )
    ) { Text(text = "SCAN", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

// 디자인 시 미리보기를 위한 함수(쓸 때에만 활성화!!)
//@OptIn(ExperimentalMaterial3Api::class)
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    Ble_connectTheme {
//        Scaffold(
//            modifier = Modifier.fillMaxSize(),
//            topBar = { TopAppBar(
//                title = { Text("Scanner", fontSize = 36.sp, fontWeight = FontWeight.Bold) },
//                actions = {
//                    IconButton(onClick = { /* */ }) {
//                        Icon(
//                            imageVector = Icons.Default.MoreVert,
//                            "더보기"
//                        )
//                    } }) },
//            bottomBar = { ScanButton()}
//        ) {
//            /* */
//        }
//    }
//}