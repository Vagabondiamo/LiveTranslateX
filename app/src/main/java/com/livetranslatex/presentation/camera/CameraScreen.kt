package com.livetranslatex.presentation.camera

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    val uiState by viewModel.uiState.collectAsState()
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            viewModel.stopProcessing()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camera Live") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Indietro")
                    }
                },
                actions = {
                    // Toggle lingua sorgente
                    TextButton(onClick = viewModel::toggleSourceLang) {
                        Text(uiState.sourceLang, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("→", modifier = Modifier.padding(horizontal = 4.dp))
                    TextButton(onClick = viewModel::toggleTargetLang) {
                        Text(uiState.targetLang, color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->

        if (!cameraPermission.status.isGranted) {
            // Richiedi permesso
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📷", fontSize = 48.sp)
                Spacer(Modifier.height(16.dp))
                Text("Permesso fotocamera richiesto", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                    Text("Concedi permesso")
                }
            }
            return@Scaffold
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            // Camera preview
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        // ImageAnalysis per OCR a ~5fps
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setTargetResolution(android.util.Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            if (uiState.isProcessing) {
                                imageProxy.close()
                                return@setAnalyzer
                            }
                            val bitmap = imageProxy.toBitmap()
                            imageProxy.close()
                            bitmap?.let { viewModel.processFrame(it) }
                        }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay bounding boxes OCR
            Canvas(modifier = Modifier.fillMaxSize()) {
                uiState.textBlocks.forEach { block ->
                    drawRect(
                        color = Color(0x884FC3F7),
                        topLeft = Offset(block.rect.left.toFloat(), block.rect.top.toFloat()),
                        size = Size(block.rect.width().toFloat(), block.rect.height().toFloat()),
                        style = Stroke(width = 2f)
                    )
                }
            }

            // Translation overlay boxes
            uiState.textBlocks.forEach { block ->
                if (block.translated.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (block.rect.left / 3).dp,
                                y = (block.rect.top / 3).dp
                            )
                            .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = block.translated,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Bottom status bar
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xBB000000))
                    .padding(12.dp)
            ) {
                if (uiState.lastTranslation.isNotBlank()) {
                    Text(
                        text = uiState.lastTranslation,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 3
                    )
                } else {
                    Text(
                        "Punta la fotocamera al testo da tradurre",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            }

            // Processing indicator
            if (uiState.isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

// Extension function per convertire ImageProxy in Bitmap
private fun ImageProxy.toBitmap(): Bitmap? {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer
    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()
    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 80, out)
    val bytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
