package com.example.ble_connect.ui.screen

import android.content.ContentValues
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    captureRequest: Int = 0
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val imageCapture = remember { ImageCapture.Builder().build() }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)

        val executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener(
            {
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                    // 후면 카메라 선택
                    val cameraSelector =
                        CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider.unbindAll()

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (exception: Exception) {
                    Log.e(
                        "CameraScreen",
                        "카메라 미리보기를 시작할 수 없습니다.",
                        exception
                    )
                }
            },
            executor
        )

        onDispose {
            if (cameraProviderFuture.isDone) {
                runCatching {
                    cameraProviderFuture.get().unbindAll()
                }
            }
        }
    }

    LaunchedEffect(captureRequest) {
        if (captureRequest == 0) return@LaunchedEffect

        val fileName = SimpleDateFormat(
            "yyyyMMdd_HHmmss_SSS",
            Locale.US
        ).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BleConnect")
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d("CameraScreen", "사진 저장 완료: ${output.savedUri}")
                    Toast.makeText(
                        context,
                        "사진이 저장되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraScreen", "사진 저장 실패", exception)
                }
            }
        )
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
