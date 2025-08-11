package com.example.omdb_kotlin.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.omdb_kotlin.domain.MovieShort
import com.example.omdb_kotlin.domain.OmdbRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchViewModel(private val repo: OmdbRepository = OmdbRepository()) : ViewModel() {
    var query by mutableStateOf("")
        private set
    var results by mutableStateOf<List<MovieShort>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        query = newQuery
    }

    fun search() {
        val q = query.trim()
        if (q.isEmpty()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            loading = true
            error = null
            val res = repo.search(q)
            loading = false
            res.onSuccess { results = it }.onFailure { error = it.message }
        }
    }
}

@Composable
fun SearchScreen(
    onMovieClick: (String) -> Unit,
    vm: SearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val query = vm.query
    val results = vm.results
    val loading = vm.loading
    val error = vm.error

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.onQueryChange(it) },
                label = { Text("Search movies") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { vm.search() }) { Text("Search") }
        }

        Spacer(Modifier.height(8.dp))

        if (loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(Modifier.height(8.dp))

        LazyColumn(Modifier.fillMaxSize()) {
            items(results) { movie ->
                MovieRow(movie = movie, onClick = { onMovieClick(movie.id) })
                Divider()
            }
        }
    }
}

@Composable
private fun MovieRow(movie: MovieShort, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = movie.poster,
            contentDescription = movie.title,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(movie.title, style = MaterialTheme.typography.titleMedium)
            Text(movie.year, style = MaterialTheme.typography.bodyMedium)
        }
        AssistChip(onClick = onClick, label = { Text(movie.type) })
    }
}
