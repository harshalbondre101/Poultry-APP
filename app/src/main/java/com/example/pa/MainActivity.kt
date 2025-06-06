package com.example.pa

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Warning

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashboardUI(this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardUI(context: ComponentActivity) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(Color(0xFFF5F5F5)) // Light gray system bars

    val dbHelper = remember { DatabaseHelper(context) }
    val alerts = remember { dbHelper.getAlerts() }

    // Define color palette
    val Purple = MaterialTheme.colorScheme.primary    // Primary purple
    val LightGray = Color(0xFFF5F5F5)  // Background
    val DarkGray = Color(0xFF212121)   // Text
    val Red = Color(0xFFD32F2F)        // Critical alerts, mortality chart
    val Amber = Color(0xFFFFCA28)      // Warning alerts
    val Orange = Color(0xFFFF9800)     // Egg production chart
    val InfoPurple = Color(0xFF917CDE) // Info alerts

    Scaffold(
        topBar = {
            val context = LocalContext.current
            TopAppBar(
                title = {
                    Text(
                        "Poultry APP",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    actionIconContentColor = Purple
                ),
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, ProfileActivity::class.java))
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Purple)
                    }
                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Purple)
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        context.startActivity(Intent(context, HistoryActivity::class.java))
                    },
                    containerColor = Purple,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.AccessTime, contentDescription = "View History")
                }
                FloatingActionButton(
                    onClick = {
                        context.startActivity(Intent(context, FormActivity::class.java))
                    },
                    containerColor = Purple,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Data")
                }
            }
        },
        containerColor = LightGray,
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                StatusSection(alerts, Purple, DarkGray, InfoPurple, Red, Amber)
                ChartsSection(Purple, Red, Orange, DarkGray)
            }
        }
    )
}

@Composable
fun StatusSection(
    alerts: List<DatabaseHelper.Alert>,
    purple: Color,
    darkGray: Color,
    infoPurple: Color,
    red: Color,
    amber: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .shadow(6.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Status / Alerts",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = darkGray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            if (alerts.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "No alerts",
                        tint = purple,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "All systems normal",
                        fontSize = 14.sp,
                        color = darkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(alerts) { alert ->
                        AlertItem(alert, infoPurple, red, amber)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertItem(alert: DatabaseHelper.Alert, infoPurple: Color, red: Color, amber: Color) {
    val icon = when (alert.severity) {
        "Critical" -> Icons.Default.Error
        "Warning" -> Icons.Default.Warning
        else -> Icons.Default.Info
    }
    val backgroundColor = when (alert.severity) {
        "Critical" -> red
        "Warning" -> amber
        else -> infoPurple
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = alert.severity,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = alert.message,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ChartsSection(purple: Color, red: Color, orange: Color, darkGray: Color) {
    val context = LocalContext.current
    val dbHelper = DatabaseHelper(context)
    val flockId = "default_flock"

    val chartList = listOf(
        Triple("Feed Trend", purple, dbHelper.getFeedTrend(flockId)),
        Triple("Mortality Rate", red, dbHelper.getMortalityTrend(flockId)),
        Triple("Egg Production", orange, dbHelper.getEggProductionTrend(flockId))
    )

    val groupedCharts = chartList.chunked(2)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Trends",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = darkGray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(groupedCharts) { rowCharts ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowCharts.forEach { (title, color, data) ->
                            ChartCard(
                                title = title,
                                data = data,
                                color = color,
                                onClick = {
                                    context.startActivity(Intent(context, ChartsActivity::class.java).apply {
                                        putExtra("CHART_TYPE", title.uppercase().split(" ")[0])
                                        putExtra("FLOCK_ID", flockId)
                                    })
                                },
                                modifier = Modifier
                                    .weight(1f)
                            )
                        }
                        if (rowCharts.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChartCard(
    title: String,
    data: List<DatabaseHelper.ChartData>,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(220.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                val icon = when (title) {
                    "Feed Trend" -> Icons.Default.Restaurant
                    "Mortality Rate" -> Icons.Default.Warning
                    "Egg Production" -> Icons.Default.Egg
                    else -> Icons.Default.Info
                }
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                val chartPoints = if (data.isEmpty()) {
                    listOf(
                        DatabaseHelper.ChartData("Day 1", 10f),
                        DatabaseHelper.ChartData("Day 2", 20f),
                        DatabaseHelper.ChartData("Day 3", 15f),
                        DatabaseHelper.ChartData("Day 4", 30f),
                        DatabaseHelper.ChartData("Day 5", 25f),
                        DatabaseHelper.ChartData("Day 6", 35f),
                        DatabaseHelper.ChartData("Day 7", 28f)
                    )
                } else {
                    data
                }

                if (chartPoints.size >= 2) {
                    val maxValue = chartPoints.maxOf { it.value }
                    val minValue = chartPoints.minOf { it.value }
                    val valueRange = if (maxValue == minValue) 1f else maxValue - minValue

                    val points = chartPoints.mapIndexed { index, chartData ->
                        val x = (index * size.width) / (chartPoints.size - 1)
                        val y = size.height - ((chartData.value - minValue) / valueRange) * size.height
                        Offset(x, y)
                    }

                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }

                    drawPath(path = path, color = color, style = Stroke(width = 4f))
                } else {
                    drawLine(
                        color = color,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 4f
                    )
                }
            }


            Text(
                text = "Last 7 Days",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
