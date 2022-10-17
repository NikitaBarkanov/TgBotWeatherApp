package data.remote.api

import data.remote.models.Current
import data.remote.models.CurrentWeather
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("current.json")
    fun getCurrentWeatherAsync(
        @Query("key") apiKey:String,
        @Query("q") countryName: String,
        @Query("aqi") airQualityData: String
    ):Deferred<CurrentWeather>
}