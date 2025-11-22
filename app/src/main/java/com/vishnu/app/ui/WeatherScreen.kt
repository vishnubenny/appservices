package com.vishnu.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.vishnu.app.MyTestService
import com.vishnu.app.ui.theme.AppServicesTheme
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun WeatherScreen(modifier: Modifier = Modifier, viewModel: WeatherViewModel = koinViewModel()) {
    val weather by viewModel.weatherState
    val error by viewModel.errorState
    val isLoading by viewModel.loadingState
    val appServices by viewModel.runningServicesState

    val context = LocalContext.current

    // Live updates for the App Status card
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateAppStatus()
            delay(1000) // Refresh every 1 seconds
        }
    }

    val permissionsToRequest = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                viewModel.startService()
            } else {
                viewModel.errorState.value = "Location permission is required to fetch weather."
            }
        }
    )

    // Automatically trigger the service and location request when the screen first opens.
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startService()
        } else {
            locationPermissionRequest.launch(permissionsToRequest)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val isServiceRunning = appServices?.any { it.contains(MyTestService::class.java.name) } == true

        Button(onClick = {
            if (!isServiceRunning) {
                // This button provides a manual way to re-trigger the process.
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    viewModel.startService()
                } else {
                    locationPermissionRequest.launch(permissionsToRequest)
                }
            }
        }) {
            Text(if (isServiceRunning) "Service Running..." else "Start Service")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Create stable local copies of the state values
        val currentError = error
        val currentWeather = weather

        if (isLoading) {
            CircularProgressIndicator()
        } else if (currentError != null) {
            Text(text = currentError)
        } else if (currentWeather != null) {
            WeatherInfo(weather = currentWeather)
        }

        Spacer(modifier = Modifier.weight(1f)) // Pushes content to the bottom

        Button(onClick = { 
            // This will terminate the app's process, effectively killing it from RAM.
            android.os.Process.killProcess(android.os.Process.myPid())
        }) {
            Text("Free RAM")
        }
        Spacer(modifier = Modifier.height(8.dp))

        AppStatusCard(viewModel = viewModel)
    }
}

@Composable
fun WeatherInfo(weather: com.vishnu.app.domain.Weather) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Location: ${weather.locality ?: "Unknown"}, ${weather.country ?: "Unknown"}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Temperature: ${weather.temperature}°C")
        Text("Wind Speed: ${weather.windSpeed} km/h")
        Text("Wind Direction: ${weather.windDirection}°")
    }
}

@Composable
fun AppStatusCard(modifier: Modifier = Modifier, viewModel: WeatherViewModel) {
    val ramUsage by viewModel.ramUsageState
    val appServices by viewModel.runningServicesState
    val lastApiUrl by viewModel.lastApiUrlState

    // Create a stable local copy of the appServices state
    val currentAppServices = appServices

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("App Status", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (ramUsage != null) {
                Text("Current RAM Usage: $ramUsage MB")
            }
            
            Text("API Call: ${lastApiUrl ?: "None"}")
            
            if (currentAppServices != null) {
                Text("Running Services:")
                if (currentAppServices.isEmpty()) {
                    Text("- None")
                } else {
                    currentAppServices.forEach { serviceName ->
                        Text("- ${serviceName.substringAfterLast('.')}")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    AppServicesTheme {
        // Can't preview this screen easily as it depends on ViewModel and Koin
    }
}
