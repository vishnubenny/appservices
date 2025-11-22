package com.vishnu.app.data

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.vishnu.app.domain.Weather
import com.vishnu.app.domain.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class WeatherRepositoryImpl(private val weatherApi: WeatherApi, private val context: Context) : WeatherRepository {
    override suspend fun getWeather(latitude: Double, longitude: Double): Weather {
        val response = weatherApi.getWeather(latitude, longitude)
        val address = getAddressFromLocation(latitude, longitude)
        return Weather(
            temperature = response.current_weather.temperature,
            windSpeed = response.current_weather.windspeed,
            windDirection = response.current_weather.winddirection,
            locality = address?.locality,
            country = address?.countryName
        )
    }

    private suspend fun getAddressFromLocation(latitude: Double, longitude: Double): Address? = withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        if (continuation.isActive) {
                            continuation.resume(addresses.firstOrNull())
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
            }
        } catch (e: Exception) {
            // In case of error, return null, so the app doesn't crash
            null
        }
    }
}
