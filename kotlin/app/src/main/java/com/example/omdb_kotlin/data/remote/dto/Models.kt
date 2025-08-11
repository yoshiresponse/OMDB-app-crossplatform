package com.example.omdb_kotlin.data.remote.dto

data class SearchResponseDto(
    val Search: List<MovieShortDto>?,
    val totalResults: String?,
    val Response: String?,
    val Error: String?
)

data class MovieShortDto(
    val Title: String,
    val Year: String,
    val imdbID: String,
    val Type: String,
    val Poster: String
)

data class MovieDetailDto(
    val Title: String?,
    val Year: String?,
    val Rated: String?,
    val Released: String?,
    val Runtime: String?,
    val Genre: String?,
    val Director: String?,
    val Writer: String?,
    val Actors: String?,
    val Plot: String?,
    val Language: String?,
    val Country: String?,
    val Awards: String?,
    val Poster: String?,
    val Ratings: List<RatingDto>?,
    val Metascore: String?,
    val imdbRating: String?,
    val imdbVotes: String?,
    val imdbID: String?,
    val Type: String?,
    val DVD: String?,
    val BoxOffice: String?,
    val Production: String?,
    val Website: String?,
    val Response: String?,
    val Error: String?
)

data class RatingDto(
    val Source: String?,
    val Value: String?
)
