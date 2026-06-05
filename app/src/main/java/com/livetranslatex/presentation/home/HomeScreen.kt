package com.livetranslatex.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HomeFeature(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val color: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToScreen: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToImage: () -> Unit,
    onNavigateToManga: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val features = listOf(
        HomeFeature("📱", "Schermo Live", "Bolla fluttuante\nTraduce qualsiasi app", Color(0xFF1565C0), onNavigateToScreen),
        HomeFeature("📷", "Camera Live", "AR in tempo reale\nPunta e traduci", Color(0xFF2E7D32), onNavigateToCamera),
        HomeFeature("🖼️", "Immagini", "Galleria e foto\nOCR + traduzione", Color(0xFF6A1B9A), onNavigateToImage),
        HomeFeature("📖", "Manga/Webtoon", "Balloon per balloon\nTraduzione contestuale", Color(0xFFAD1457), onNavigateToManga),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("LiveTranslateX", fontWeight = FontWeight.Bold)
                        Text("Traduttore universale", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Text("📋", fontSize = 18.sp)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Text("⚙️", fontSize = 18.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            // Banner principale
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🌐", fontSize = 40.sp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Pronto a tradurre", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Scegli una modalità qui sotto",
                            fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Modalità", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(features.size) { i ->
                    val feature = features[i]
                    Card(
                        onClick = feature.onClick,
                        modifier = Modifier.fillMaxWidth().height(130.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = feature.color)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(feature.emoji, fontSize = 28.sp)
                            Column {
                                Text(feature.title, color = Color.White,
                                    fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(feature.subtitle, color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp, lineHeight = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
