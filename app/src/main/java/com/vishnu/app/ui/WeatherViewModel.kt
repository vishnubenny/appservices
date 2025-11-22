package com.vishnu.app.ui

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.app.domain.GetWeatherUseCase
import com.vishnu.app.domain.Weather
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val application: Application
) : ViewModel() {

    val latitude = mutableStateOf("")
    val longitude = mutableStateOf("")

    val weatherState = mutableStateOf<Weather?>(null)
    val errorState = mutableStateOf<String?>(null)
    val loadingState = mutableStateOf(false)

    // State for the App Status card
    val ramUsageState = mutableStateOf<Int?>(null)
    val runningServicesState = mutableStateOf<List<String>?>(null)
    val lastApiUrlState = mutableStateOf<String?>(null)

    fun updateAppStatus() {
        val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Get RAM Usage
        val memoryInfoArray = activityManager.getProcessMemoryInfo(intArrayOf(android.os.Process.myPid()))
        ramUsageState.value = (memoryInfoArray.firstOrNull()?.totalPss ?: 0) / 1024

        // Get this app's running services
        @Suppress("DEPRECATION")
        val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)
        runningServicesState.value = runningServices
            .filter { it.service.packageName == application.packageName }
            .map { it.service.className }
    }

    fun getWeather(latitude: Double, longitude: Double) {
        // Construct the full URL
        val fullUrl = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current_weather=true"
        
        // Extract only the endpoint (path + query) for display
        val endpoint = fullUrl.substringAfter("open-meteo.com")
        lastApiUrlState.value = endpoint

        viewModelScope.launch {
            loadingState.value = true
            try {
                weatherState.value = getWeatherUseCase(latitude, longitude)
                errorState.value = null
            } catch (e: Exception) {
                errorState.value = "Error fetching weather: ${e.message}"
                weatherState.value = null
            } finally {
                loadingState.value = false
                // Automatically update the app status after the API call is finished
                updateAppStatus()
            }
        }
    }
}
