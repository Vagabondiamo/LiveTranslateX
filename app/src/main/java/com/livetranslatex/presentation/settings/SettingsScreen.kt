package com.livetranslatex.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.livetranslatex.domain.model.OcrEngineType
import com.livetranslatex.domain.model.TranslatorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var selectedOcr by remember { mutableStateOf(OcrEngineType.ML_KIT) }
    var selectedTranslator by remember { mutableStateOf(TranslatorType.OFFLINE_ML_KIT) }
    var captureInterval by remember { mutableStateOf(500L) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impostazioni") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Motore OCR", style = MaterialTheme.typography.titleMedium)
                OcrEngineType.values().forEach { engine ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(engine.name)
                        RadioButton(
                            selected = selectedOcr == engine,
                            onClick = { selectedOcr = engine }
                        )
                    }
                }
            }
            item {
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text("Traduttore", style = MaterialTheme.typography.titleMedium)
                TranslatorType.values().forEach { translator ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(translator.name)
                        RadioButton(
                            selected = selectedTranslator == translator,
                            onClick = { selectedTranslator = translator }
                        )
                    }
                }
            }
            item {
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text("Intervallo aggiornamento", style = MaterialTheme.typography.titleMedium)
                listOf(250L, 500L, 1000L).forEach { ms ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${ms}ms")
                        RadioButton(
                            selected = captureInterval == ms,
                            onClick = { captureInterval = ms }
                        )
                    }
                }
            }
        }
    }
}
