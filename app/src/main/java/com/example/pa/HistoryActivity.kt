package com.example.pa

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : ComponentActivity() {
    private val TAG = "HistoryActivity"
    private lateinit var dbHelper: DatabaseHelper
    private var exportCallback: (() -> Unit)? = null

    // Permission launcher for legacy storage (Android 9 and below)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Storage permission granted")
            exportCallback?.invoke() ?: Log.e(TAG, "Export callback not set")
        } else {
            Log.w(TAG, "Storage permission denied")
            Toast.makeText(
                this,
                "Storage permission denied. Cannot export data.",
                Toast.LENGTH_LONG
            ).show()
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(
                    this,
                    "Please enable storage permission in Settings.",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(
                    android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", packageName, null)
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)
        Log.d(TAG, "Package name: ${packageName}")
        setContent {
            val systemUiController = rememberSystemUiController()
            systemUiController.setSystemBarsColor(Color.White)

            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Poultry Data History", style = MaterialTheme.typography.titleLarge) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    },
                    content = { padding ->
                        HistoryScreen(dbHelper, Modifier.padding(padding)) {
                            exportCallback = it
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED
                            ) {
                                Log.d(TAG, "Requesting storage permission for Android 9 or lower")
                                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    Toast.makeText(
                                        this,
                                        "Storage permission is needed to save Excel files to Downloads.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            } else {
                                exportCallback?.invoke() ?: Log.e(TAG, "Export callback not set")
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(dbHelper: DatabaseHelper, modifier: Modifier = Modifier, onExport: (() -> Unit) -> Unit) {
    val TAG = "HistoryScreen"
    val context = LocalContext.current
    var poultryTypeFilter by remember { mutableStateOf("All") }
    var flockIdFilter by remember { mutableStateOf("All") }
    var dateFilter by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val dataList = remember { mutableStateListOf<Map<String, Any?>>() }

    // Load data with error handling
    LaunchedEffect(poultryTypeFilter, flockIdFilter, dateFilter) {
        isLoading = true
        try {
            val cursor = dbHelper.getFilteredData(
                poultryTypeFilter.takeIf { it != "All" },
                flockIdFilter.takeIf { it != "All" },
                dateFilter.takeIf { it.isNotEmpty() }
            )
            dataList.clear()
            if (cursor.count == 0) {
                Log.d(TAG, "No data found for filters: poultryType=$poultryTypeFilter, flockId=$flockIdFilter, date=$dateFilter")
                errorMessage = "No data found for the selected filters."
            } else {
                while (cursor.moveToNext()) {
                    val row = mutableMapOf<String, Any?>()
                    for (i in 0 until cursor.columnCount) {
                        when (cursor.getType(i)) {
                            Cursor.FIELD_TYPE_INTEGER -> row[cursor.getColumnName(i)] = cursor.getInt(i)
                            Cursor.FIELD_TYPE_FLOAT -> row[cursor.getColumnName(i)] = cursor.getFloat(i)
                            Cursor.FIELD_TYPE_STRING -> row[cursor.getColumnName(i)] = cursor.getString(i)
                            else -> row[cursor.getColumnName(i)] = cursor.getString(i)
                        }
                    }
                    dataList.add(row)
                }
                errorMessage = null
                Log.d(TAG, "Loaded ${dataList.size} data rows")
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading data: ${e.message}", e)
            errorMessage = "Failed to load data: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Date picker dialog with proper state handling
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                val datePickerState = rememberDatePickerState()
                TextButton(onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(selectedDateMillis))
                        dateFilter = date
                        Log.d(TAG, "Selected date: $date")
                    } else {
                        Log.w(TAG, "No date selected in DatePicker")
                        Toast.makeText(context, "Please select a valid date.", Toast.LENGTH_SHORT).show()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = rememberDatePickerState(
                    initialSelectedDateMillis = System.currentTimeMillis()
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filters
            item(span = { GridItemSpan(2) }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Filters", style = MaterialTheme.typography.titleMedium)
                        CreateDropdownField(
                            label = "Poultry Type",
                            options = listOf("All", "Broiler", "Layer", "Breeder"),
                            selected = poultryTypeFilter,
                            onSelected = { poultryTypeFilter = it }
                        )
                        CreateDropdownField(
                            label = "Flock ID",
                            options = listOf("All", "Shed1", "Shed2", "Shed3"),
                            selected = flockIdFilter,
                            onSelected = { flockIdFilter = it }
                        )
                        DatePickerField(
                            label = "Date",
                            value = dateFilter,
                            onValueChange = { dateFilter = it },
                            onClick = { showDatePicker = true }
                        )
                    }
                }
            }

            // Loading or Error State
            item(span = { GridItemSpan(2) }) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                    errorMessage != null -> {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                    dataList.isEmpty() -> {
                        Text(
                            text = "No data available.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }

            // Data Entries
            dataList.forEach { row ->
                item(span = { GridItemSpan(2) }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Entry #${row["id"] ?: "Unknown"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Type: ${row["poultry_type"] ?: "N/A"}")
                            Text("Date: ${row["date_time"] ?: "N/A"}")
                            Text("Flock ID: ${row["flock_id"] ?: "N/A"}")
                            Text("Feed Given: ${row["feed_given"] ?: "N/A"} kg")
                            Text("Dead Birds: ${row["dead_birds"] ?: "N/A"}")
                        }
                    }
                }
            }
        }

        // Export Button (Pinned to Bottom)
        Button(
            onClick = {
                onExport {
                    try {
                        val workbook = XSSFWorkbook()
                        val sheet = workbook.createSheet("Poultry Data")
                        val cursor = dbHelper.getFilteredData(
                            poultryTypeFilter.takeIf { it != "All" },
                            flockIdFilter.takeIf { it != "All" },
                            dateFilter.takeIf { it.isNotEmpty() }
                        )

                        if (cursor.count == 0) {
                            cursor.close()
                            Log.w(TAG, "No data to export")
                            Toast.makeText(context, "No data to export.", Toast.LENGTH_LONG).show()
                            return@onExport
                        }

                        // Header row
                        val headerRow = sheet.createRow(0)
                        cursor.columnNames.forEachIndexed { index, name ->
                            headerRow.createCell(index).setCellValue(name)
                        }

                        // Data rows
                        var rowIndex = 1
                        while (cursor.moveToNext()) {
                            val row = sheet.createRow(rowIndex++)
                            for (i in 0 until cursor.columnCount) {
                                when (cursor.getType(i)) {
                                    Cursor.FIELD_TYPE_INTEGER -> row.createCell(i).setCellValue(cursor.getInt(i).toDouble())
                                    Cursor.FIELD_TYPE_FLOAT -> row.createCell(i).setCellValue(cursor.getFloat(i).toDouble())
                                    Cursor.FIELD_TYPE_STRING -> row.createCell(i).setCellValue(cursor.getString(i))
                                    else -> row.createCell(i).setCellValue(cursor.getString(i))
                                }
                            }
                        }
                        cursor.close()

                        // Save file based on Android version
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                        val fileName = "poultry_data_$timestamp.xlsx"
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            // Use MediaStore for Android 10+
                            val contentResolver = context.contentResolver
                            val values = ContentValues().apply {
                                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                                put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                                put(MediaStore.Downloads.IS_PENDING, 1)
                            }

                            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                            if (uri != null) {
                                contentResolver.openOutputStream(uri)?.use { outputStream ->
                                    workbook.write(outputStream)
                                }
                                values.clear()
                                values.put(MediaStore.Downloads.IS_PENDING, 0)
                                contentResolver.update(uri, values, null, null)
                                Log.d(TAG, "Exported to Downloads/$fileName")
                                Toast.makeText(context, "Exported successfully.", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.e(TAG, "Failed to create file in Downloads")
                                Toast.makeText(context, "Failed to export: Unable to create file.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            // Legacy storage for Android 9 and below
                            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            val file = File(downloadsDir, fileName)
                            FileOutputStream(file).use { outputStream ->
                                workbook.write(outputStream)
                            }
                            Log.d(TAG, "Exported to ${file.absolutePath}")
                            Toast.makeText(context, "Exported successfully.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Export failed: ${e.message}", e)
                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Export to Excel", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun DatePickerField(label: String, value: String, onValueChange: (String) -> Unit, onClick: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { /* Read-only, updated via DatePicker */ },
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Select Date",
                modifier = Modifier.clickable { onClick() }
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = label }
    )
}

@Composable
fun CreateDropdownField(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Dropdown",
                    modifier = Modifier
                        .rotate(if (expanded) 90f else 0f)
                        .clickable { expanded = true }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
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