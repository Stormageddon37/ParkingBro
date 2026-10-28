package com.bresler.parkingbro.ui

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bresler.parkingbro.ui.fullscreen.FullScreenImageScreen
import com.bresler.parkingbro.ui.main.MainScreen
import com.bresler.parkingbro.ui.settings.SettingsScreen
import com.bresler.parkingbro.ui.theme.ParkingTheme
import dagger.hilt.android.AndroidEntryPoint

private const val ROUTE_MAIN = "main"
private const val ROUTE_SETTINGS = "settings"
private const val ARG_IMAGE_URI = "imageUri"
private const val ROUTE_FULLSCREEN = "fullscreen/{$ARG_IMAGE_URI}"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ParkingTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = ROUTE_MAIN) {
                    composable(ROUTE_MAIN) {
                        MainScreen(
                            onOpenImage = { uri ->
                                navController.navigate("fullscreen/${Uri.encode(uri.toString())}")
                            },
                            onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
                        )
                    }
                    composable(ROUTE_SETTINGS) {
                        SettingsScreen(onBack = { navController.popBackStack() })
                    }
                    composable(
                        route = ROUTE_FULLSCREEN,
                        arguments = listOf(navArgument(ARG_IMAGE_URI) { type = NavType.StringType }),
                    ) { entry ->
                        val uri = Uri.parse(entry.arguments?.getString(ARG_IMAGE_URI).orEmpty())
                        FullScreenImageScreen(uri = uri, onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
