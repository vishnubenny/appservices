package com.vishnu.app

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vishnu.app.ui.theme.AppServicesTheme
import java.util.Calendar

@Composable
fun ServiceListScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val hasPermission = remember {
        hasUsageStatsPermission(context)
    }

    if (hasPermission) {
        UsageStatsScreen(modifier = modifier)
    } else {
        PermissionRequestScreen(modifier = modifier)
    }
}

@Composable
fun UsageStatsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.HOUR, -1)
    val startTime = calendar.timeInMillis
    val endTime = System.currentTimeMillis()

    val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        .filter { it.lastTimeUsed > 0 }
        .sortedByDescending { it.lastTimeUsed }

    if (usageStats.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "No recently used apps found.")
        }
    } else {
        LazyColumn(modifier = modifier.padding(16.dp)) {
            items(usageStats) { stat ->
                UsageStatItem(stat = stat)
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Permission Required",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "This app needs usage stats access to show recently used apps. Please grant the permission in the settings.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = { 
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            context.startActivity(intent)
        }) {
            Text(text = "Open Settings")
        }
    }
}

@Composable
fun UsageStatItem(stat: UsageStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Package: ${stat.packageName}")
            Text(text = "Last time used: ${java.text.SimpleDateFormat.getDateTimeInstance().format(stat.lastTimeUsed)}")
        }
    }
}

private fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

@Preview(showBackground = true)
@Composable
fun ServiceListScreenPreview() {
    AppServicesTheme {
        ServiceListScreen()
    }
}
