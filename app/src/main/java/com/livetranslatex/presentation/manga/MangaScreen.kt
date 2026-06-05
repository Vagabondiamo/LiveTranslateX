package com.livetranslatex.presentation.manga

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaScreen(
    onBack: () -> Unit,
    viewModel: MangaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val pickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val stream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(stream)
            stream?.close()
            bitmap?.let { viewModel.addPage(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manga / Webtoon") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Indietro") }
                },
                actions = {
                    if (uiState.pages.isNotEmpty()) {
                        TextButton(onClick = viewModel::translateAll) {
                            Text("Traduci tutto")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { pickerLauncher.launch("image/*") }) {
                Text("➕", fontSize = 20.sp)
            }
        }
    ) { padding ->
        if (uiState.pages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📖", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Aggiungi pagine manga o webtoon", fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Text("Tocca ➕ per selezionare le immagini",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.pages.size) { index ->
                    val page = uiState.pages[index]
                    MangaPageCard(
                        page = page,
                        pageNum = index + 1,
                        onTranslate = { viewModel.translatePage(index) },
                        onRemove = { viewModel.removePage(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MangaPageCard(
    page: MangaPage,
    pageNum: Int,
    onTranslate: () -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pagina $pageNum", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!page.isTranslated && !page.isLoading) {
                        TextButton(onClick = onTranslate) { Text("Traduci") }
                    }
                    TextButton(onClick = onRemove) {
                        Text("Rimuovi", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Immagine
            Box {
                Image(
                    bitmap = page.bitmap.asImageBitmap(),
                    contentDescription = "Pagina $pageNum",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )

                // Overlay testo tradotto sui balloon
                page.translations.forEach { block ->
                    if (block.translated.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .offset(x = (block.x / 2).dp, y = (block.y / 2).dp)
                                .background(Color(0xEEFFFFFF), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .widthIn(max = 140.dp)
                        ) {
                            Text(
                                block.translated,
                                fontSize = 10.sp,
                                color = Color.Black,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }

                // Loading overlay
                if (page.isLoading) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color(0x88000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(Modifier.height(8.dp))
                            Text("Analisi balloon...", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Testo estratto
            if (page.isTranslated && page.translations.isNotEmpty()) {
                Divider(modifier = Modifier.padding(horizontal = 12.dp))
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Testo tradotto:", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    page.translations.forEach { block ->
                        if (block.translated.isNotBlank()) {
                            Text("• ${block.translated}", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
