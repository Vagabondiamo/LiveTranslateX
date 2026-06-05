package com.livetranslatex.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var selectedEngine by remember { mutableStateOf("ML Kit (offline)") }
    var sourceLang by remember { mutableStateOf("Giapponese") }
    var targetLang by remember { mutableStateOf("Italiano") }
    var deeplKey by remember { mutableStateOf("") }
    var openaiKey by remember { mutableStateOf("") }
    var showDeepL by remember { mutableStateOf(false) }
    var showOpenAI by remember { mutableStateOf(false) }

    val engines = listOf("ML Kit (offline)", "DeepL", "OpenAI GPT-4o")
    val langs = listOf("Italiano", "Inglese", "Giapponese", "Cinese", "Coreano", "Francese", "Spagnolo", "Tedesco")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impostazioni") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Indietro") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Motore traduzione
            SettingsSection("🔧 Motore di traduzione") {
                engines.forEach { engine ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedEngine == engine,
                            onClick = { selectedEngine = engine }
                        )
                        Text(engine, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            // API Keys (visibili solo se non ML Kit)
            if (selectedEngine == "DeepL") {
                SettingsSection("🔑 DeepL API Key") {
                    OutlinedTextField(
                        value = deeplKey,
                        onValueChange = { deeplKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Inserisci chiave DeepL") },
                        visualTransformation = if (showDeepL) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(onClick = { showDeepL = !showDeepL }) {
                                Text(if (showDeepL) "Nascondi" else "Mostra", fontSize = 11.sp)
                            }
                        },
                        singleLine = true
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Ottieni chiave su deepl.com/pro-api (piano Free disponibile)",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            if (selectedEngine == "OpenAI GPT-4o") {
                SettingsSection("🔑 OpenAI API Key") {
                    OutlinedTextField(
                        value = openaiKey,
                        onValueChange = { openaiKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("sk-...") },
                        visualTransformation = if (showOpenAI) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(onClick = { showOpenAI = !showOpenAI }) {
                                Text(if (showOpenAI) "Nascondi" else "Mostra", fontSize = 11.sp)
                            }
                        },
                        singleLine = true
                    )
                }
            }

            // Lingue
            SettingsSection("🌐 Lingue") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Da:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            OutlinedTextField(
                                value = sourceLang,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                langs.forEach { lang ->
                                    DropdownMenuItem(text = { Text(lang) }, onClick = {
                                        sourceLang = lang; expanded = false
                                    })
                                }
                            }
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text("A:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            OutlinedTextField(
                                value = targetLang,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                langs.forEach { lang ->
                                    DropdownMenuItem(text = { Text(lang) }, onClick = {
                                        targetLang = lang; expanded = false
                                    })
                                }
                            }
                        }
                    }
                }
            }

            // Salva
            Button(modifier = Modifier.fillMaxWidth(), onClick = { /* TODO: persist */ }) {
                Text("💾 Salva impostazioni")
            }

            // Info versione
            Spacer(Modifier.height(8.dp))
            Text("LiveTranslateX v1.0 • Powered by ML Kit, DeepL, OpenAI",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            content()
        }
    }
}
