package com.atta.appusagetracker.utils

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import com.atta.appusagetracker.model.UsageModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Utils {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDate(pattern: String="dd MM yyyy"):String{
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        val currentDate = LocalDate.now().format(formatter)
        return currentDate
    }

    fun increaseAndDecreaseDay(currentDateStr: String, daysToAdd: Int,timeInMillis:(Long)->Unit): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.parse(currentDateStr)
        val calendar = Calendar.getInstance().apply {
            time = currentDate ?: Date()
            add(Calendar.DAY_OF_YEAR, daysToAdd)
        }
        timeInMillis.invoke(calendar.time.time)
        return dateFormat.format(calendar.time)
    }



    suspend fun Context.getStatics(date: String): ArrayList<UsageModel> {
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