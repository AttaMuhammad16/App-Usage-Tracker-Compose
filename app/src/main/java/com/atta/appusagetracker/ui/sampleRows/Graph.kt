package com.atta.appusagetracker.ui.sampleRows

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.atta.appusagetracker.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun Context.LineChartScreen(timeInMilliseconds: Long) {
    // Convert time in milliseconds to hours and minutes
    val (hours, minutes) = millisecondsToHoursMinutes(timeInMilliseconds)

    // Create entry for the time
    val entry = Entry(minutes.toFloat(), hours.toFloat())

    // Create LineDataSet
    val dataSet = LineDataSet(mutableListOf(entry), "App Usage").apply {
        color = R.color.teal_200
        valueTextColor = android.graphics.Color.BLACK
        valueTextSize = 12f
    }

    // Create LineData with the LineDataSet
    val lineData = LineData(dataSet)

    Canvas(modifier = Modifier.fillMaxSize()) {
        LineChart(this@LineChartScreen).apply {
            data = lineData
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
            axisLeft.setDrawGridLines(false)
            setTouchEnabled(true)
            setPinchZoom(true)
            animateX(1000)
            invalidate()
        }
    }

}

// Utility function to convert milliseconds to hours and minutes
private fun millisecondsToHoursMinutes(milliseconds: Long): Pair<Int, Int> {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return Pair(hours.toInt(), remainingMinutes.toInt())
}

