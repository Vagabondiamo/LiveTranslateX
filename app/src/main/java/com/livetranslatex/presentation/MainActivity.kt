package com.livetranslatex.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.livetranslatex.presentation.home.HomeScreen
import com.livetranslatex.presentation.camera.CameraScreen
import com.livetranslatex.presentation.screen.ScreenTranslateScreen
import com.livetranslatex.presentation.manga.MangaScreen
import com.livetranslatex.presentation.history.HistoryScreen
import com.livetranslatex.presentation.settings.SettingsScreen
import com.livetranslatex.presentation.theme.LiveTranslateXTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object ScreenTranslate : Screen("screen")
    object Manga : Screen("manga")
    object History : Screen("history")
    object Settings : Screen("settings")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiveTranslateXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(navController = navController)
                        }
                        composable(Screen.Camera.route) {
                            CameraScreen(navController = navController)
                        }
                        composable(Screen.ScreenTranslate.route) {
                            ScreenTranslateScreen(navController = navController)
                        }
                        composable(Screen.Manga.route) {
                            MangaScreen(navController = navController)
                        }
                        composable(Screen.History.route) {
                            HistoryScreen(navController = navController)
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
