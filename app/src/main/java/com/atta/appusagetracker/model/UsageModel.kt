package com.atta.appusagetracker.model

import android.graphics.drawable.Drawable

data class UsageModel(
    var appName: String="",
    var packageName:String="",
    var usageTime:String="",
    var appIcons:Drawable,
    var todayUsage:String="",
    var highestDailyUsage:String="",
    var installationDate:String=""
)
