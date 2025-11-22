package com.vishnu.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.vishnu.app.ui.theme.AppServicesTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun WeatherScreen(modifier: Modifier = Modifier, viewModel: WeatherViewModel = koinViewModel()) {
    var latitude by viewModel.latitude
    var longitude by viewModel.longitude
    val weather by viewModel.weatherState
    val error by viewModel.errorState
    val isLoading by viewModel.loadingState

    val context = LocalContext.current
    val fusedLocationClient = remember(context) { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    viewModel.loadingState.value = true
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            viewModel.loadingState.value = false
                            if (location != null) {
                                latitude = location.latitude.toString()
                                longitude = location.longitude.toString()
                                viewModel.getWeather(location.latitude, location.longitude)
                            } else {
                                viewModel.errorState.value = "Could not get current location. Please check GPS and enter manually."
                            }
                        }
                        .addOnFailureListener { e ->
                            viewModel.loadingState.value = false
                            viewModel.errorState.value = "Failed to get location: ${e.message}. Please enter manually."
                        }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text("Latitude") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            TextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitude") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.getWeather(latitude.toDouble(), longitude.toDouble())
            },
            enabled = latitude.isNotBlank() && longitude.isNotBlank()
        ) {
            Text("Get Weather")
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.weight(1f)) // Pushes the new card to the bottom

        AppStatusCard(viewModel = viewModel)
    }
}

@Composable
fun WeatherInfo(weather: com.vishnu.app.domain.Weather) {
    Column {
        Text("Location: ${weather.locality ?: "Unknown"}, ${weather.country ?: "Unknown"}")
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
            if (lastApiUrl != null) {
                Text("Last API Call: $lastApiUrl")
            }
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
