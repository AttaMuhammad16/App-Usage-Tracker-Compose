package com.atta.appusagetracker

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.atta.appusagetracker.model.UsageModel
import com.atta.appusagetracker.ui.sampleRows.UsageSampleRow
import com.atta.appusagetracker.ui.theme.AppUsageTrackerTheme
import com.atta.appusagetracker.utils.Utils.getCurrentDate
import com.atta.appusagetracker.utils.Utils.getStatics
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
        val map:MutableMap<String,List<UsageModel>> = hashMapOf()
        setContent {
            AppUsageTrackerTheme {

                val list = remember { mutableStateListOf<UsageModel>() }
                var currentDate by remember {
                    mutableStateOf(getCurrentDate("yyyy-MM-dd"))
                }
                var loading by remember { mutableStateOf(true) }

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
                       val listOfUsage = map[currentDate] ?: withContext(Dispatchers.IO) { getStatics(currentDate) }
                       list.addAll(listOfUsage)
                       loading = false
                       map[currentDate] = listOfUsage
                   }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                        val offset = Offset(5.0f, 6.0f)

                        Text(text = "Check Your App Statistics On Daily Bases", textAlign = TextAlign.Center, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(
                            fontSize = 16.sp,
                            shadow = Shadow(color = Color.Red, offset = offset, blurRadius = 1f))
                        )

                        Row (modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(), verticalAlignment = Alignment.Top){

                            Image(painter = painterResource(id =R.drawable.baseline_arrow_back_24 ), contentDescription ="a" , modifier = Modifier
                                .size(25.dp)
                                .weight(1f)
                                .clickable {
                                    val newDate = increaseAndDecreaseDay(currentDate, -1) {
                                    }
                                    currentDate = newDate
                                    loading = true
                                }, alignment = Alignment.CenterStart)
                            Text(text = currentDate, color = Color.Black, fontSize = 20.sp, textAlign = TextAlign.Center)
                            Image(painter = painterResource(id =R.drawable.baseline_arrow_forward_24 ), contentDescription ="a" , modifier = Modifier
                                .size(25.dp)
                                .weight(1f)
                                .clickable {

                                    var time: Long = 0
                                    val newDate = increaseAndDecreaseDay(currentDate, 1) {
                                        time = it
                                    }
                                    if (System.currentTimeMillis() > time) {
                                        currentDate = newDate
                                        loading = true
                                    }
                                }, alignment = Alignment.CenterEnd)
                        }

                        if(loading){
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ){
                                CircularProgressIndicator(
                                    modifier = Modifier.width(40.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    strokeCap = StrokeCap.Round,
                                )
                            }
                        }

                        UsageSampleRow(list)

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


}
