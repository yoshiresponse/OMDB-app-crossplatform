package com.example.omdb_kotlin.feature.favorites

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.omdb_kotlin.data.local.FavoritesStore
import com.example.omdb_kotlin.domain.MovieShort
import com.example.omdb_kotlin.domain.OmdbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoritesViewModel(app: Application) : AndroidViewModel(app) {
    private val store = FavoritesStore(app)
    private val repo = OmdbRepository()

    private val _items = MutableStateFlow<List<MovieShort>>(emptyList())
    val items = _items.asStateFlow()

    val favorites = store.favoritesFlow

    private val _byId = MutableStateFlow<Map<String, MovieShort>>(emptyMap())
    val byId = _byId.asStateFlow()

    fun ensureLoaded(ids: Set<String>) {
        val missing = ids - _byId.value.keys.toSet()
        if (missing.isEmpty()) return
        viewModelScope.launch {
            missing.forEach { id ->
                val res = repo.details(id)
                res.onSuccess { d ->
                    val short = MovieShort(
                        title = d.title,
                        year = d.year,
                        id = d.id,
                        poster = d.poster,
                        type = d.type
                    )
                    _byId.update { it + (id to short) }
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    onMovieClick: (String) -> Unit,
    vm: FavoritesViewModel = viewModel()
) {
    val favorites by vm.favorites.collectAsState(initial = emptySet())
    val byId by vm.byId.collectAsState()

    LaunchedEffect(favorites) { vm.ensureLoaded(favorites) }

    if (favorites.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("No favorites yet")
        }
        return
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        items(favorites.toList()) { id ->
            val movie = byId[id]
            if (movie == null) {
                ListItem(
                    headlineContent = { Text(id) },
                    modifier = Modifier.fillMaxWidth().clickable { onMovieClick(id) }
                )
            } else {
                ListItem(
                    headlineContent = { Text(movie.title) },
                    supportingContent = { Text(movie.year) },
                    modifier = Modifier.fillMaxWidth().clickable { onMovieClick(id) }
                )
            }
            Divider()
        }
    }
}
