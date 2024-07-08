package com.atta.appusagetracker.model

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Stable


@Stable
data class UsageModel(
    var appName: String="",
    var packageName:String="",
    var usageTime:String="",
    var appIcons:Drawable,
    var todayUsage:String="",
    var installationDate:String="",
    var timeInMilliseconds:Long=0
)
