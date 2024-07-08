package com.atta.appusagetracker.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.atta.appusagetracker.ui.activities.ui.theme.AppUsageTrackerTheme
import com.atta.appusagetracker.ui.sampleRows.LineChartScreen

class GraphActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val time=intent.getLongExtra("time",0)
            LineChartScreen(time)
            Toast.makeText(this, "$time", Toast.LENGTH_SHORT).show()
        }
    }
}
