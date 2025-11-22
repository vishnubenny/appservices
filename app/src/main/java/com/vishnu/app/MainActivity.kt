package com.vishnu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vishnu.app.ui.WeatherScreen
import com.vishnu.app.ui.theme.AppServicesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppServicesTheme {
                WeatherScreen()
            }
        }
    }
}
