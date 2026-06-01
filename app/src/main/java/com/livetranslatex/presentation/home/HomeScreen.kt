package com.livetranslatex.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.livetranslatex.presentation.Screen

data class HomeMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val menuItems = listOf(
        HomeMenuItem("Traduci Schermo", "Overlay su qualsiasi app", Icons.Default.Tv, Screen.ScreenTranslate.route),
        HomeMenuItem("Fotocamera", "Traduzione in tempo reale", Icons.Default.Camera, Screen.Camera.route),
        HomeMenuItem("Manga / Webtoon", "Lettore con traduzione integrata", Icons.Default.MenuBook, Screen.Manga.route),
        HomeMenuItem("Cronologia", "Traduzioni precedenti", Icons.Default.History, Screen.History.route),
        HomeMenuItem("Impostazioni", "OCR, traduttore, lingua", Icons.Default.Settings, Screen.Settings.route)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("LiveTranslateX", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("Traduzione in tempo reale", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(menuItems.size) { index ->
                HomeMenuCard(item = menuItems[index]) {
                    navController.navigate(menuItems[index].route)
                }
            }
        }
    }
}

@Composable
fun HomeMenuCard(item: HomeMenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(item.subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}
