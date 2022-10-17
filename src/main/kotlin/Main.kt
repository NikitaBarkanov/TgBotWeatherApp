import bot.WeatherBot
import data.remote.RetrofitClient
import data.remote.RetrofitType
import data.remote.repository.WeatherRepository

fun main() {
//5649745843:AAE8x59F-cCLn5-PsAeW8RQNecQfCWohh2s

    val weatherRetrofit = RetrofitClient.getRetrofit(RetrofitType.WEATHER)
    val reverseRetrofit = RetrofitClient.getRetrofit(RetrofitType.REVERSE_GEOCODER)
    val weatherApi = RetrofitClient.getWeatherApi(weatherRetrofit)
    val reverseApi = RetrofitClient.getReversedGeocodingApi(reverseRetrofit)
    val weatherRepository = WeatherRepository(weatherApi,reverseApi)
    val weatherBot = WeatherBot(weatherRepository).createBot()
    weatherBot.startPolling()
}