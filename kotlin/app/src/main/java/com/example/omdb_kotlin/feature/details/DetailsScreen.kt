package com.example.omdb_kotlin.feature.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import com.example.omdb_kotlin.data.local.FavoritesStore
import com.example.omdb_kotlin.domain.MovieDetail
import com.example.omdb_kotlin.domain.OmdbRepository
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement

class DetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = OmdbRepository()
    private val store = FavoritesStore(app)
    var state = androidx.compose.runtime.mutableStateOf<MovieDetail?>(null)
        private set
    var error = androidx.compose.runtime.mutableStateOf<String?>(null)
        private set
    val favoritesFlow = store.favoritesFlow

    fun load(imdbId: String) {
        viewModelScope.launch {
            val res = repo.details(imdbId)
            res.onSuccess { state.value = it }.onFailure { error.value = it.message }
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch { store.toggle(id) }
    }
}

@Composable
fun DetailsScreen(imdbId: String, vm: DetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val detail = vm.state.value
    val err = vm.error.value
    LaunchedEffect(imdbId) { vm.load(imdbId) }
    val favorites = vm.favoritesFlow.collectAsState(initial = emptySet()).value
    val isFav = favorites.contains(imdbId)

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        detail?.let { d ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(d.title, style = MaterialTheme.typography.headlineSmall)
                    Text(d.year, style = MaterialTheme.typography.bodyMedium)
                }
                IconButton(onClick = { vm.toggleFavorite(imdbId) }) {
                    if (isFav) Icon(Icons.Filled.Favorite, contentDescription = "Unfavorite")
                    else Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Favorite")
                }
            }
            AsyncImage(model = d.poster, contentDescription = d.title, modifier = Modifier.fillMaxWidth().height(300.dp))
            Spacer(Modifier.height(12.dp))
            Spacer(Modifier.height(8.dp))
            Text("Genre: ${d.genre}")
            Text("IMDB: ${d.rating}")
            Spacer(Modifier.height(12.dp))
            Text(d.plot)
        }
    }
}
