package com.atta.appusagetracker.utils

import android.os.Build
import androidx.annotation.RequiresApi
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

    fun increaseAndDecreaseDay(currentDateStr: String, daysToAdd: Int): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.parse(currentDateStr)
        val calendar = Calendar.getInstance().apply {
            time = currentDate ?: Date()
            add(Calendar.DAY_OF_YEAR, daysToAdd)
        }
        return dateFormat.format(calendar.time)
    }

}