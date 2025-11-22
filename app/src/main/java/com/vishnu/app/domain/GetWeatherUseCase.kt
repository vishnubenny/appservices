package com.vishnu.app.domain

class GetWeatherUseCase(private val weatherRepository: WeatherRepository) {
    suspend operator fun invoke(latitude: Double, longitude: Double): Weather {
        return weatherRepository.getWeather(latitude, longitude)
    }
}
