package com.livetranslatex.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTranslateScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Traduci Schermo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            // TODO: MediaProjection permission flow + ScreenCaptureService start
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Modalità cattura schermo")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { /* TODO: request MediaProjection */ }) {
                    Text("Avvia traduzione schermo")
                }
            }
        }
    }
}
