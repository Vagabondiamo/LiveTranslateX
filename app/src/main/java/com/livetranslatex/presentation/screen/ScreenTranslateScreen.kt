package com.livetranslatex.presentation.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livetranslatex.service.FloatingBubbleService
import com.livetranslatex.service.ScreenCaptureService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTranslateScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var bubbleActive by remember { mutableStateOf(false) }
    var hasOverlayPermission by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }
    var projectionData by remember { mutableStateOf<Intent?>(null) }

    // Launcher permesso overlay
    val overlayLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        hasOverlayPermission = Settings.canDrawOverlays(context)
    }

    // Launcher MediaProjection
    val projectionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            projectionData = result.data
            startServices(context, result.resultCode, result.data!!)
            bubbleActive = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Traduci Schermo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (bubbleActive) Color(0xFF1B5E20) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (bubbleActive) "🟢 Bolla attiva" else "⚪ Inattiva",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bubbleActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (bubbleActive) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tocca la bolla fluttuante per tradurre qualsiasi schermo",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Step 1: Permesso overlay
            StepCard(
                step = 1,
                title = "Permesso Overlay",
                description = "Necessario per mostrare la bolla sopra le altre app",
                done = hasOverlayPermission,
                buttonText = if (hasOverlayPermission) "✓ Concesso" else "Concedi permesso",
                enabled = !hasOverlayPermission
            ) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                overlayLauncher.launch(intent)
            }

            // Step 2: Avvia traduzione
            StepCard(
                step = 2,
                title = "Avvia Traduzione Schermo",
                description = "Cattura lo schermo e posiziona la bolla fluttuante",
                done = bubbleActive,
                buttonText = if (bubbleActive) "✓ Attivo" else "Avvia",
                enabled = hasOverlayPermission && !bubbleActive
            ) {
                val mpManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                projectionLauncher.launch(mpManager.createScreenCaptureIntent())
            }

            // Stop button
            if (bubbleActive) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        stopServices(context)
                        bubbleActive = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("⏹ Ferma tutto", fontSize = 16.sp)
                }
            }

            // Info
            Spacer(Modifier.weight(1f))
            Text(
                "Come funziona: avvia, poi esci dall'app e usa qualsiasi altra app. " +
                        "Tocca la bolla 🔵 per tradurre immediatamente ciò che vedi.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun StepCard(
    step: Int,
    title: String,
    description: String,
    done: Boolean,
    buttonText: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (done) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (done) "✓" else step.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(buttonText, fontSize = 12.sp)
            }
        }
    }
}

private fun startServices(context: Context, resultCode: Int, data: Intent) {
    // Avvia ScreenCaptureService
    val captureIntent = Intent(context, ScreenCaptureService::class.java).apply {
        action = ScreenCaptureService.ACTION_START
        putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
        putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, data)
    }
    context.startForegroundService(captureIntent)

    // Avvia FloatingBubbleService
    context.startForegroundService(Intent(context, FloatingBubbleService::class.java))
}

private fun stopServices(context: Context) {
    context.stopService(Intent(context, ScreenCaptureService::class.java))
    context.stopService(Intent(context, FloatingBubbleService::class.java))
}
