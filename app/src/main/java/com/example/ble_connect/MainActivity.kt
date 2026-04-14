package com.example.ble_connect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ble_connect.ui.theme.Ble_connectTheme
import com.example.ble_connect.ui.screen.Greeting

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ble_connectTheme {
                Ble_connectTheme {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = { TopAppBar(
                            title = { Text("Scanner") },
                            actions = {
                                IconButton(onClick = { /* */ }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        "더보기"
                                    )
                                } }) },
                        bottomBar = { Button(
                            onClick = { /* */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0088FF), // 배경색: 0088ff
                                contentColor = Color.White          // 텍스트 색상: 파란 배경엔 흰색이 잘 보여요!
                            )
                        ) { Text(text = "SCAN", fontSize = 20.sp) } }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                                .padding(innerPadding)
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("wft")
                            Greeting(name = "Android")
                            Spacer(modifier = Modifier.height(10.dp))
                            Greeting(name = "Android")
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("wft")
                        }
                    }
                }
            }
        }
    }
}