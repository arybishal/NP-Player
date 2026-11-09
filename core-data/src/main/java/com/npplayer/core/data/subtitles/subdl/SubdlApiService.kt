package com.npplayer.core.data.subtitles.subdl

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SubdlApiService {

    @GET("api/v1/subtitles")
    suspend fun searchSubtitles(
        @Query("api_key") apiKey: String,
        @Query("film_name") filmName: String? = null,
        @Query("file_name") fileName: String? = null,
        @Query("season_number") seasonNumber: Int? = null,
        @Query("episode_number") episodeNumber: Int? = null,
        @Query("type") type: String? = null,
        @Query("year") year: Int? = null,
        @Query("languages") languages: String? = null,
        @Query("subs_per_page") subsPerPage: Int = 30
    ): Response<SubdlApiResponse>
}
