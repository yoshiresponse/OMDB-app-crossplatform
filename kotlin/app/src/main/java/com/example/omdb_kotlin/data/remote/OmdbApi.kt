package com.example.omdb_kotlin.data.remote

import com.example.omdb_kotlin.data.remote.dto.MovieDetailDto
import com.example.omdb_kotlin.data.remote.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OmdbApi {
    @GET("/")
    suspend fun searchMovies(
        @Query("s") query: String,
        @Query("page") page: Int = 1
    ): SearchResponseDto

    @GET("/")
    suspend fun getMovieDetails(
        @Query("i") imdbId: String,
        @Query("plot") plot: String = "full"
    ): MovieDetailDto
}
