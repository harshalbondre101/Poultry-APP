package com.example.pa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pa.DatabaseHelper.ChartData
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ChartsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chartType = intent.getStringExtra("CHART_TYPE") ?: "FEED"
        val flockId = intent.getStringExtra("FLOCK_ID") ?: "default_flock"

        setContent {
            MaterialTheme {
                ChartsScreen(chartType, flockId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(chartType: String, flockId: String) {
    val dbHelper = DatabaseHelper(LocalContext.current)
    var selectedPoultryType by remember { mutableStateOf("All") }
    var selectedFlockId by remember { mutableStateOf(flockId) }
    var days by remember { mutableStateOf(7) }

    val data = remember(selectedPoultryType, selectedFlockId, days) {
        when (chartType) {
            "FEED" -> dbHelper.getFeedTrend(selectedFlockId, days)
            "MORTALITY" -> dbHelper.getMortalityTrend(selectedFlockId, days)
            "EGGS" -> dbHelper.getEggProductionTrend(selectedFlockId, days)
            else -> emptyList<ChartData>()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detailed Chart: ${chartType.replace("_", " ")}") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DropdownMenuInput(
                        label = "Poultry Type",
                        options = listOf("All", "Broiler", "Layer", "Breeder"),
                        selected = selectedPoultryType,
                        onSelected = { selectedPoultryType = it }
                    )
                    DropdownMenuInput(
                        label = "Flock ID",
                        options = listOf("All", "flock1", "flock2"), // Replace with actual flock IDs
                        selected = selectedFlockId,
                        onSelected = { selectedFlockId = it }
                    )
                    DropdownMenuInput(
                        label = "Days",
                        options = listOf("7", "14", "30"),
                        selected = days.toString(),
                        onSelected = { days = it.toInt() }
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Canvas(modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)) {
                        if (data.isNotEmpty()) {
                            val maxValue = data.maxOf { it.value }
                            val minValue = data.minOf { it.value }
                            val valueRange = if (maxValue == minValue) 1f else maxValue - minValue
                            val points = data.mapIndexed { index, chartData ->
                                val x = (index * size.width) / (data.size - 1)
                                val y = size.height - ((chartData.value - minValue) / valueRange) * size.height
                                androidx.compose.ui.geometry.Offset(x, y)
                            }
                            val path = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                points.forEach { lineTo(it.x, it.y) }
                            }
                            drawPath(
                                path,
                                when (chartType) {
                                    "FEED" -> Color.Blue
                                    "MORTALITY" -> Color.Red
                                    "EGGS" -> Color.Green
                                    else -> Color.Black
                                },
                                style = Stroke(width = 6f)
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Data Points:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            items(data.size) { index ->
                val point = data[index]
                Text(
                    text = "${point.date}: ${point.value}",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun DropdownMenuInput(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier
                .width(120.dp)
                .clickable { expanded = true },
            readOnly = true
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}