package com.example.omdb_kotlin.domain

import com.example.omdb_kotlin.data.remote.OmdbApi
import com.example.omdb_kotlin.data.remote.dto.MovieDetailDto
import com.example.omdb_kotlin.data.remote.dto.MovieShortDto
import com.example.omdb_kotlin.di.NetworkModule

// Domain models
 data class MovieShort(
    val id: String,
    val title: String,
    val year: String,
    val type: String,
    val poster: String
)

 data class MovieDetail(
    val id: String,
    val title: String,
    val year: String,
    val type: String,
    val plot: String,
    val poster: String,
    val genre: String,
    val rating: String
)

 private fun MovieShortDto.toDomain() = MovieShort(
    id = imdbID,
    title = Title,
    year = Year,
    type = Type,
    poster = Poster
)

 private fun MovieDetailDto.toDomain() = MovieDetail(
    id = imdbID.orEmpty(),
    title = Title.orEmpty(),
    year = Year.orEmpty(),
    type = Type.orEmpty(),
    plot = Plot.orEmpty(),
    poster = Poster.orEmpty(),
    genre = Genre.orEmpty(),
    rating = imdbRating.orEmpty()
)

 class OmdbRepository(private val api: OmdbApi = NetworkModule.omdbApi) {
    suspend fun search(query: String): Result<List<MovieShort>> = runCatching {
        val res = api.searchMovies(query)
        if (res.Response == "True" && res.Search != null) res.Search.map { it.toDomain() } else emptyList()
    }

    suspend fun details(imdbId: String): Result<MovieDetail> = runCatching {
        val res = api.getMovieDetails(imdbId)
        if (res.Response == "True") res.toDomain() else throw IllegalStateException(res.Error ?: "Unknown error")
    }
 }
