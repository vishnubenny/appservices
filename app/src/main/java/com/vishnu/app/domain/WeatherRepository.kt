package com.vishnu.app.domain

interface WeatherRepository {
    suspend fun getWeather(latitude: Double, longitude: Double): Weather
}
