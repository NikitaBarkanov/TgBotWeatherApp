package data.remote.repository

import data.remote.api.ReverseGeocodingApi
import data.remote.api.WeatherApi
import data.remote.models.CurrentWeather
import data.remote.models.ReversedCountry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val weatherApi: WeatherApi,
    private val reverseGeocodingApi: ReverseGeocodingApi
){
    suspend fun getCurrentWeather(apiKey: String, countryName: String, airQualityData: String): CurrentWeather{
        return withContext(Dispatchers.IO){
            weatherApi.getCurrentWeatherAsync(apiKey,countryName,airQualityData)
        }.await()
    }

    suspend fun getReverseGeocodingCountryName(latitude: String, longitude: String, format: String): ReversedCountry{
        return withContext(Dispatchers.IO){
            reverseGeocodingApi.getCountryNameByCoorAsync(latitude,longitude, format).await()
        }
    }
}