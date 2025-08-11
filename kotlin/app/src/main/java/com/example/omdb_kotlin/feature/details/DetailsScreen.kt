package com.example.omdb_kotlin.feature.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.omdb_kotlin.domain.MovieDetail
import com.example.omdb_kotlin.domain.OmdbRepository
import kotlinx.coroutines.launch

class DetailsViewModel(private val repo: OmdbRepository = OmdbRepository()) : ViewModel() {
    var state = androidx.compose.runtime.mutableStateOf<MovieDetail?>(null)
        private set
    var error = androidx.compose.runtime.mutableStateOf<String?>(null)
        private set

    fun load(imdbId: String) {
        viewModelScope.launch {
            val res = repo.details(imdbId)
            res.onSuccess { state.value = it }.onFailure { error.value = it.message }
        }
    }
}

@Composable
fun DetailsScreen(imdbId: String, vm: DetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val detail = vm.state.value
    val err = vm.error.value
    LaunchedEffect(imdbId) { vm.load(imdbId) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        detail?.let { d ->
            AsyncImage(model = d.poster, contentDescription = d.title, modifier = Modifier.fillMaxWidth().height(300.dp))
            Spacer(Modifier.height(12.dp))
            Text(d.title, style = MaterialTheme.typography.headlineSmall)
            Text(d.year, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text("Genre: ${d.genre}")
            Text("IMDB: ${d.rating}")
            Spacer(Modifier.height(12.dp))
            Text(d.plot)
        }
    }
}
