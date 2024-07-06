package com.atta.appusagetracker

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.atta.appusagetracker.model.UsageModel
import com.atta.appusagetracker.ui.theme.AppUsageTrackerTheme
import com.atta.appusagetracker.utils.Utils.getCurrentDate
import com.atta.appusagetracker.utils.Utils.increaseAndDecreaseDay
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppUsageTrackerTheme {

                val list = remember { mutableStateListOf<UsageModel>() }
                var currentDate by remember {
                    mutableStateOf(getCurrentDate("yyyy-MM-dd"))
                }

                val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
                val mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(), packageName
                )
                if (mode != AppOpsManager.MODE_ALLOWED) {
                    startPermissionActivity()
                } else {

                   LaunchedEffect(key1 = currentDate){
                       list.clear()
                       val listOfIcons= withContext(Dispatchers.IO){getStatics(currentDate)}
                       list.addAll(listOfIcons)
                   }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        val offset = Offset(5.0f, 8.0f)

                        Text(text = "Check Your App Statistics On Daily Bases", textAlign = TextAlign.Center, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(
                            fontSize = 16.sp,
                            shadow = Shadow(color = Color.Blue, offset = offset, blurRadius = 2f))
                        )

                        Row (modifier = Modifier.padding(8.dp).fillMaxWidth()){

                            Image(painter = painterResource(id =R.drawable.baseline_arrow_back_24 ), contentDescription ="a" , modifier = Modifier
                                .size(25.dp)
                                .weight(1f)
                                .clickable {
                                    val newDate = increaseAndDecreaseDay(currentDate, -1)
                                    currentDate = newDate
                                }, alignment = Alignment.CenterStart)
                            Text(text = currentDate, color = Color.Black, fontSize = 20.sp, textAlign = TextAlign.Center)
                            Image(painter = painterResource(id =R.drawable.baseline_arrow_forward_24 ), contentDescription ="a" , modifier = Modifier
                                .size(25.dp)
                                .weight(1f)
                                .clickable {
                                    val newDate = increaseAndDecreaseDay(currentDate, 1)
                                    currentDate = newDate
                                }, alignment = Alignment.CenterEnd)

                        }
                        IconsSampleRow(list)
                    }
                }
            }
        }
    }
    

    @Composable
    fun IconsSampleRow(list: List<UsageModel>) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(list.size) { i ->
                val data=list[i]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(1f)
                        .padding(5.dp)
                        .clickable { /* Handle card click */ },
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(5.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Image(
                            painter = rememberDrawablePainter(drawable = data.appIcons),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp)) // Add spacing between elements
                        Text(
                            text = data.appName,
                            color = Color.Black,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        Text(
                            text = data.usageTime,
                            color = Color.Black,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }


    fun startPermissionActivity() {
        startActivityForResult(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 12);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK&&requestCode==12) {
            lifecycleScope.launch {
                getStatics(getCurrentDate("yyyy-MM-dd"))
            }
        }
    }

    suspend fun getStatics(date: String): ArrayList<UsageModel> {
        val systemService = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val packageManager = packageManager

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDate = dateFormat.parse(date) ?: Date()
        val startTime = selectedDate.time
        val endTime = startTime + 24 * 60 * 60 * 1000

        val lUsageStatsMap = systemService.queryAndAggregateUsageStats(startTime, endTime)
        val listOfUsageModels = ArrayList<UsageModel>()

        for (usageStats in lUsageStatsMap.values) {

            if (usageStats.totalTimeInForeground >= 1) {
                val formattedTime = if (usageStats.totalTimeInForeground < 1000) {
                    "${usageStats.totalTimeInForeground} ms"
                } else {
                    formatTime(usageStats.totalTimeInForeground)
                }

                try {
                    val info = packageManager.getApplicationInfo(usageStats.packageName, 0)
                    val appName = packageManager.getApplicationLabel(info).toString()
                    val appIcon = packageManager.getApplicationIcon(info)
                    val installationDate = getInstallationDate(packageManager, usageStats.packageName)

                    // Fetch additional statistics
                    val todayUsage = formattedTime
                    val highestTimeInMilli = getHighestDailyUsage(systemService, usageStats.packageName)
                    val highestDailyUsage = formatTime(highestTimeInMilli)

                    val usageModel = UsageModel(
                        appName = appName,
                        packageName = usageStats.packageName,
                        usageTime = formattedTime,
                        appIcons = appIcon,
                        todayUsage = todayUsage,
                        highestDailyUsage = highestDailyUsage,
                        installationDate = installationDate
                    )
                    listOfUsageModels.add(usageModel)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
        return listOfUsageModels
    }

    fun getInstallationDate(packageManager: PackageManager, packageName: String): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.format(Date(packageInfo.firstInstallTime))
        } catch (e: PackageManager.NameNotFoundException) {
            "N/A"
        }
    }

    fun getHighestDailyUsage(usageStatsManager: UsageStatsManager, packageName: String): Long {
        return try {
            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis())
            stats.filter { it.packageName == packageName }.maxOfOrNull { it.totalTimeInForeground } ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }


    fun formatTime(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val formattedTime = StringBuilder()
        if (days > 0) {
            formattedTime.append(days).append("d ")
        }
        if (hours > 0) {
            formattedTime.append(hours % 24).append("h ")
        }
        if (minutes > 0) {
            formattedTime.append(minutes % 60).append("m ")
        }
        if (seconds > 0) {
            formattedTime.append(seconds % 60).append("s")
        }
        return formattedTime.toString()
    }

}
