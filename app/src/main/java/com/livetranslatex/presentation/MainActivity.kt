package com.livetranslatex.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.livetranslatex.presentation.camera.CameraScreen
import com.livetranslatex.presentation.history.HistoryScreen
import com.livetranslatex.presentation.home.HomeScreen
import com.livetranslatex.presentation.image.ImageTranslateScreen
import com.livetranslatex.presentation.manga.MangaScreen
import com.livetranslatex.presentation.screen.ScreenTranslateScreen
import com.livetranslatex.presentation.settings.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToScreen   = { navController.navigate("screen") },
                onNavigateToCamera   = { navController.navigate("camera") },
                onNavigateToImage    = { navController.navigate("image") },
                onNavigateToManga    = { navController.navigate("manga") },
                onNavigateToHistory  = { navController.navigate("history") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("screen")   { ScreenTranslateScreen(onBack = { navController.popBackStack() }) }
        composable("camera")   { CameraScreen(onBack = { navController.popBackStack() }) }
        composable("image")    { ImageTranslateScreen(onBack = { navController.popBackStack() }) }
        composable("manga")    { MangaScreen(onBack = { navController.popBackStack() }) }
        composable("history")  { HistoryScreen(onBack = { navController.popBackStack() }) }
        composable("settings") { SettingsScreen(onBack = { navController.popBackStack() }) }
    }
}
