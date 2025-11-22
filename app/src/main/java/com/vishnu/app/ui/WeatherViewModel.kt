package com.vishnu.app.ui

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.app.MyTestService
import com.vishnu.app.common.LocationProvider
import com.vishnu.app.domain.GetWeatherUseCase
import com.vishnu.app.domain.Weather
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    init {
        // Listen for location updates from the service
        LocationProvider.locationFlow.onEach { location ->
            latitude.value = location.latitude.toString()
            longitude.value = location.longitude.toString()
            getWeather(location.latitude, location.longitude)
        }.launchIn(viewModelScope)
    }

    fun startService() {
        val intent = Intent(application, MyTestService::class.java)
        application.startService(intent)
        updateAppStatus()
    }

    fun stopService() {
        val intent = Intent(application, MyTestService::class.java)
        application.stopService(intent)
        updateAppStatus()
    }

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

    private fun getWeather(latitude: Double, longitude: Double) {
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
                // Clear the API URL so it disappears from the UI
                lastApiUrlState.value = null
            }
        }
    }
}
