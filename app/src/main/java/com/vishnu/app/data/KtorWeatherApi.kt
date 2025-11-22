package com.vishnu.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class KtorWeatherApi(private val client: HttpClient) : WeatherApi {
    override suspend fun getWeather(latitude: Double, longitude: Double): WeatherResponse {
        return client.get("https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current_weather=true").body()
    }
}
