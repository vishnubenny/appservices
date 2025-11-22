package com.vishnu.app

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vishnu.app.ui.theme.AppServicesTheme

@Composable
fun TaskManagerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var memoryInfo by remember { mutableStateOf("Tap a button to see memory usage") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = memoryInfo,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = { 
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfoArray = activityManager.getProcessMemoryInfo(intArrayOf(android.os.Process.myPid()))
            val pss = memoryInfoArray.firstOrNull()?.totalPss ?: 0
            memoryInfo = "This app is using approximately ${pss / 1024} MB of RAM."
        }) {
            Text(text = "Get This App's RAM Usage")
        }
        Button(onClick = { 
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            val totalRam = memInfo.totalMem / (1024 * 1024)
            val availableRam = memInfo.availMem / (1024 * 1024)
            memoryInfo = "Device RAM: $availableRam MB available of $totalRam MB total."
        }) {
            Text(text = "Get Total Device RAM Usage")
        }
        Button(onClick = { 
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.killBackgroundProcesses(context.packageName)
            memoryInfo = "Stopped this app\'s background processes."
        }) {
            Text(text = "Stop This App\'s Processes")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskManagerScreenPreview() {
    AppServicesTheme {
        TaskManagerScreen()
    }
}
