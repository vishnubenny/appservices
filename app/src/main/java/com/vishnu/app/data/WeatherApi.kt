package com.vishnu.app.data

interface WeatherApi {
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherResponse
}
