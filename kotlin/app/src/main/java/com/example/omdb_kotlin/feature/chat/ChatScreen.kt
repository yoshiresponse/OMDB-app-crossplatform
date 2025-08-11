package com.example.omdb_kotlin.feature.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omdb_kotlin.domain.MovieShort
import com.example.omdb_kotlin.domain.OmdbRepository
import kotlinx.coroutines.launch
import com.example.omdb_kotlin.data.llm.GeminiClient

class ChatViewModel(private val repo: OmdbRepository = OmdbRepository()) : ViewModel() {
    var prompt by mutableStateOf("")
        private set
    var results by mutableStateOf<List<MovieShort>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun onPromptChange(s: String) { prompt = s }

    fun ask() {
        val q = prompt.trim()
        if (q.isEmpty()) return
        viewModelScope.launch {
            loading = true
            error = null
            try {
                val suggestions = GeminiClient.suggestQueries(q)
                val queries = if (suggestions.isNotEmpty()) suggestions else listOf(q)
                val aggregated = mutableMapOf<String, MovieShort>()
                for (query in queries) {
                    val res = repo.search(query)
                    res.onSuccess { list -> list.forEach { aggregated[it.id] = it } }
                        .onFailure { e -> error = e.message }
                }
                results = aggregated.values.toList()
            } catch (t: Throwable) {
                error = t.message
            } finally {
                loading = false
            }
        }
    }
}

@Composable
fun ChatScreen(
    onMovieClick: (String) -> Unit,
    vm: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val prompt = vm.prompt
    val results = vm.results
    val loading = vm.loading
    val error = vm.error

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row {
            OutlinedTextField(
                value = prompt,
                onValueChange = vm::onPromptChange,
                label = { Text("Ask for movies...") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = vm::ask) { Text("Send") }
        }
        Spacer(Modifier.height(8.dp))
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(results) { movie ->
                ListItem(
                    headlineContent = { Text(movie.title) },
                    supportingContent = { Text(movie.year) },
                    trailingContent = { Text(movie.type) },
                    modifier = Modifier.fillMaxWidth().clickable { onMovieClick(movie.id) }
                )
                Divider()
            }
        }
    }
}
