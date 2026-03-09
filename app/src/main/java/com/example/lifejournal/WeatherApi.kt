package com.example.lifejournal

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class WeatherData(
    val description: String,
    val temperature: Double,
    val icon: String
)

object WeatherApi {

    // Open-Meteo: free, no API key required
    private const val LATITUDE  = 65.0121
    private const val LONGITUDE = 25.4651

    suspend fun getCurrentWeather(city: String = "Oulu", countryCode: String = "FI"): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.open-meteo.com/v1/forecast" +
                        "?latitude=$LATITUDE&longitude=$LONGITUDE" +
                        "&current_weather=true&temperature_unit=celsius"

                val response = URL(url).readText()
                val currentWeather = JSONObject(response).getJSONObject("current_weather")

                val temperature  = currentWeather.getDouble("temperature")
                val weatherCode  = currentWeather.getInt("weathercode")

                WeatherData(
                    description = getWeatherDescription(weatherCode),
                    temperature = temperature,
                    icon        = getWeatherIcon(weatherCode)
                )
            } catch (e: Exception) {
                Log.e("WeatherApi", "Error fetching weather: ${e.message}")
                null
            }
        }
    }

    private fun getWeatherDescription(code: Int): String = when (code) {
        0        -> "Clear sky"
        1, 2, 3  -> "Partly cloudy"
        45, 48   -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        77       -> "Snow grains"
        80, 81, 82 -> "Rain showers"
        85, 86   -> "Snow showers"
        95       -> "Thunderstorm"
        96, 99   -> "Thunderstorm with hail"
        else     -> "Unknown"
    }

    fun getWeatherIcon(code: Int): String = when (code) {
        0        -> "☀️"
        1, 2, 3  -> "⛅"
        45, 48   -> "🌫️"
        51, 53, 55 -> "🌦️"
        61, 63, 65 -> "🌧️"
        71, 73, 75 -> "❄️"
        77       -> "🌨️"
        80, 81, 82 -> "🌧️"
        85, 86   -> "🌨️"
        95       -> "⛈️"
        96, 99   -> "⛈️"
        else     -> "🌡️"
    }
}