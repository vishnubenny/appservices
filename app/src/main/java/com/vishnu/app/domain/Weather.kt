package com.vishnu.app.domain

data class Weather(
    val temperature: Double,
    val windSpeed: Double,
    val windDirection: Int,
    val locality: String?,
    val country: String?
)
