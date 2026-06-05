package com.livetranslatex.presentation.image

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageTranslateScreen(
    onBack: () -> Unit,
    viewModel: ImageTranslateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val stream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(stream)
            stream?.close()
            bitmap?.let { bmp -> viewModel.processImage(bmp) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Traduci Immagine") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.bitmap == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🖼️", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Nessuna immagine selezionata",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp)) {
                    Image(
                        bitmap = uiState.bitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) {
                    Text("📁 Galleria")
                }
                if (uiState.bitmap != null) {
                    OutlinedButton(onClick = viewModel::reset, modifier = Modifier.weight(1f)) {
                        Text("🗑 Rimuovi")
                    }
                }
            }

            if (uiState.isLoading) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("Analisi in corso...")
                    }
                }
            }

            if (uiState.originalText.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📝 Testo riconosciuto", fontWeight = FontWeight.Bold, fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text(uiState.originalText, fontSize = 14.sp)
                    }
                }
            }

            if (uiState.translatedText.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🌐 Traduzione", fontWeight = FontWeight.Bold, fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(Modifier.height(8.dp))
                        Text(uiState.translatedText, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                OutlinedButton(
                    onClick = { viewModel.copyToClipboard(context, uiState.translatedText) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("📋 Copia traduzione") }
            }

            if (uiState.error != null) {
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text("⚠️ ${uiState.error}", modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}
