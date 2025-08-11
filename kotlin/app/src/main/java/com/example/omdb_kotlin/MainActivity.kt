package com.example.omdb_kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.omdb_kotlin.ui.theme.Omdb_kotlinTheme
import com.example.omdb_kotlin.feature.search.SearchScreen
import com.example.omdb_kotlin.feature.details.DetailsScreen
import com.example.omdb_kotlin.feature.favorites.FavoritesScreen
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Chat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Omdb_kotlinTheme {
                App()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OMDb Lookup") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White),
                actions = {
                    IconButton(onClick = { navController.navigate("favorites") }) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Favorites", tint = Color.White)
                    }
                    IconButton(onClick = { navController.navigate("chat") }) {
                        Icon(Icons.Filled.Chat, contentDescription = "Chat", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "search",
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable("search") {
                SearchScreen(onMovieClick = { id -> navController.navigate("details/$id") })
            }
            composable("favorites") {
                FavoritesScreen(onMovieClick = { id -> navController.navigate("details/$id") })
            }
            composable("chat") {
                com.example.omdb_kotlin.feature.chat.ChatScreen(onMovieClick = { id -> navController.navigate("details/$id") })
            }
            composable(
                route = "details/{imdbId}",
                arguments = listOf(navArgument("imdbId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("imdbId") ?: return@composable
                DetailsScreen(imdbId = id)
            }
        }
    }
}