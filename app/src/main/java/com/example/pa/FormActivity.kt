package com.example.pa

import android.content.ContentValues
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

class FormActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = DatabaseHelper(this)
        setContent {
            val systemUiController = rememberSystemUiController()
            systemUiController.setSystemBarsColor(Color.White)

            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Add Poultry Data", style = MaterialTheme.typography.titleLarge) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    },
                    content = { padding ->
                        PoultryForm(dbHelper, Modifier.padding(padding))
                    }
                )
            }
        }
    }
}

@Composable
fun PoultryForm(dbHelper: DatabaseHelper, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // State variables (unchanged)
    var poultryType by remember { mutableStateOf("Broiler") }
    var dateTime by remember { mutableStateOf(getCurrentDateTime()) }
    var flockId by remember { mutableStateOf("") }
    var feedGiven by remember { mutableStateOf("") }
    var waterConsumed by remember { mutableStateOf("Normal") }
    var deadBirds by remember { mutableStateOf("") }
    var vaccineGiven by remember { mutableStateOf(false) }
    var vaccineName by remember { mutableStateOf("") }
    var avgBirdWeight by remember { mutableStateOf("") }
    var birdAge by remember { mutableStateOf(getBirdAge().toString()) }
    var tempHumidity by remember { mutableStateOf("Normal") }
    var remarks by remember { mutableStateOf("") }
    var shedCleaning by remember { mutableStateOf(false) }
    var feedersClean by remember { mutableStateOf(false) }
    var litterCondition by remember { mutableStateOf("Dry") }
    var sickBirds by remember { mutableStateOf(false) }
    var sickSymptoms by remember { mutableStateOf("") }
    var birdBehavior by remember { mutableStateOf("Normal") }
    var ventilation by remember { mutableStateOf(true) }
    var badSmell by remember { mutableStateOf(false) }
    var biosecurity by remember { mutableStateOf(true) }
    var footbath by remember { mutableStateOf(true) }
    var photoUploaded by remember { mutableStateOf(false) }
    // Broiler-specific
    var fcrValue by remember { mutableStateOf("") }
    var deadBirdReason by remember { mutableStateOf("") }
    var growthRate by remember { mutableStateOf(true) }
    var overcrowding by remember { mutableStateOf(false) }
    var lameness by remember { mutableStateOf(false) }
    // Layer-specific
    var eggsCollected by remember { mutableStateOf("") }
    var eggProduction by remember { mutableStateOf("") }
    var eggQuality by remember { mutableStateOf("Good") }
    var nestBoxesClean by remember { mutableStateOf(true) }
    var cannibalism by remember { mutableStateOf(false) }
    // Breeder-specific
    var fertileEggs by remember { mutableStateOf("") }
    var hatchability by remember { mutableStateOf("") }
    var maleFemaleRatio by remember { mutableStateOf(true) }
    var matingBehavior by remember { mutableStateOf(true) }
    var eggsHandled by remember { mutableStateOf(true) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Form Title
        item(span = { GridItemSpan(2) }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "Poultry Data Form",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // General Information Section
        item(span = { GridItemSpan(2) }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("General Information", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        item { DropdownField("Poultry Type", listOf("Broiler", "Layer", "Breeder"), poultryType) { poultryType = it } }
        item { TextField(value = dateTime, onValueChange = {}, label = { Text("Date & Time") }, enabled = false) }
        item { DropdownField("Flock ID / Shed No.", listOf("Shed1", "Shed2", "Shed3"), flockId) { flockId = it } }
        item { NumberField("Feed Given Today (kg)", feedGiven) { feedGiven = it } }
        item { DropdownField("Water Consumed", listOf("Low", "Normal", "High"), waterConsumed) { waterConsumed = it } }
        item { NumberField("Dead Birds Today", deadBirds) { deadBirds = it } }

        // Health and Management Section
        item(span = { GridItemSpan(2) }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Health and Management", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        item { CheckboxField("Vaccine/Medicine Given?", vaccineGiven) { vaccineGiven = it } }
        if (vaccineGiven) {
            item { DropdownField("Vaccine/Medicine Name", listOf("Vaccine A", "Vaccine B", "Medicine X"), vaccineName) { vaccineName = it } }
        }
        item { NumberField("Average Bird Weight (opt)", avgBirdWeight) { avgBirdWeight = it } }
        item { TextField(value = birdAge, onValueChange = {}, label = { Text("Bird Age (days)") }, enabled = false) }
        item { DropdownField("Temp & Humidity", listOf("Low", "Normal", "High"), tempHumidity) { tempHumidity = it } }
        item { CheckboxField("Shed Cleaning Done?", shedCleaning) { shedCleaning = it } }
        item { CheckboxField("Feeders & Drinkers Clean?", feedersClean) { feedersClean = it } }
        item { DropdownField("Litter Condition", listOf("Dry", "Slightly Wet", "Very Wet"), litterCondition) { litterCondition = it } }
        item { CheckboxField("Any Sick Birds?", sickBirds) { sickBirds = it } }
        if (sickBirds) {
            item { DropdownField("Symptoms", listOf("Coughing", "Lethargy", "Other"), sickSymptoms) { sickSymptoms = it } }
        }
        item { DropdownField("Bird Behavior", listOf("Normal", "Aggressive", "Lethargic"), birdBehavior) { birdBehavior = it } }
        item { CheckboxField("Ventilation Working?", ventilation) { ventilation = it } }
        item { CheckboxField("Any Bad Smell?", badSmell) { badSmell = it } }
        item { CheckboxField("Biosecurity Followed?", biosecurity) { biosecurity = it } }
        item { CheckboxField("Footbath Used?", footbath) { footbath = it } }
        item { CheckboxField("Photo Uploaded?", photoUploaded) { photoUploaded = it } }
        item(span = { GridItemSpan(2) }) {
            TextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text("Remarks / Notes") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Broiler-specific Section
        if (poultryType == "Broiler") {
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Broiler Specific", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            item { NumberField("FCR Value", fcrValue) { fcrValue = it } }
            item { DropdownField("Dead Birds Reason", listOf("Disease", "Heat Stress", "Other"), deadBirdReason) { deadBirdReason = it } }
            item { CheckboxField("Growth Rate Normal?", growthRate) { growthRate = it } }
            item { CheckboxField("Check for Overcrowding", overcrowding) { overcrowding = it } }
            item { CheckboxField("Signs of Lameness", lameness) { lameness = it } }
        }

        // Layer-specific Section
        if (poultryType == "Layer") {
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Layer Specific", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            item { NumberField("Eggs Collected Today", eggsCollected) { eggsCollected = it } }
            item { NumberField("Egg Production %", eggProduction) { eggProduction = it } }
            // Changed "Hen-Day Egg Production %" to "Egg Production %" for brevity
            item { DropdownField("Egg Quality", listOf("Good", "Broken", "Small"), eggQuality) { eggQuality = it } }
            item { CheckboxField("Nest Boxes Cleaned?", nestBoxesClean) { nestBoxesClean = it } }
            item { CheckboxField("Cannibalism Observed?", cannibalism) { cannibalism = it } }
        }

        // Breeder-specific Section
        if (poultryType == "Breeder") {
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Breeder Specific", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            item { NumberField("Fertile Eggs Collected", fertileEggs) { fertileEggs = it } }
            item { NumberField("Hatchability %", hatchability) { hatchability = it } }
            item { CheckboxField("Male-Female Ratio?", maleFemaleRatio) { maleFemaleRatio = it } }
            item { CheckboxField("Mating Behavior Seen?", matingBehavior) { matingBehavior = it } }
            item { CheckboxField("Eggs Handled Properly?", eggsHandled) { eggsHandled = it } }
        }

        // Save Button
        item(span = { GridItemSpan(2) }) {
            Button(
                onClick = {
                    if (flockId.isEmpty() || feedGiven.isEmpty() || deadBirds.isEmpty()) {
                        Log.w("PoultryForm", "Validation failed: flockId=$flockId, feedGiven=$feedGiven, deadBirds=$deadBirds")
                        Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (feedGiven.toFloatOrNull() == null || deadBirds.toIntOrNull() == null) {
                        Log.w("PoultryForm", "Invalid number input: feedGiven=$feedGiven, deadBirds=$deadBirds")
                        Toast.makeText(context, "Invalid number input", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val values = ContentValues().apply {
                        put("poultry_type", poultryType)
                        put("date_time", dateTime)
                        put("flock_id", flockId)
                        put("feed_given", feedGiven.toFloatOrNull())
                        put("water_consumed", waterConsumed)
                        put("dead_birds", deadBirds.toIntOrNull())
                        put("vaccine_given", if (vaccineGiven) "Yes" else "No")
                        put("vaccine_name", vaccineName)
                        put("avg_bird_weight", avgBirdWeight.toFloatOrNull())
                        put("bird_age", birdAge.toIntOrNull())
                        put("temp_humidity", tempHumidity)
                        put("remarks", remarks)
                        put("shed_cleaning", if (shedCleaning) "Yes" else "No")
                        put("feeders_clean", if (feedersClean) "Yes" else "No")
                        put("litter_condition", litterCondition)
                        put("sick_birds", if (sickBirds) "Yes" else "No")
                        put("sick_symptoms", sickSymptoms)
                        put("bird_behavior", birdBehavior)
                        put("ventilation", if (ventilation) "Yes" else "No")
                        put("bad_smell", if (badSmell) "Yes" else "No")
                        put("biosecurity", if (biosecurity) "Yes" else "No")
                        put("footbath", if (footbath) "Yes" else "No")
                        put("photo_uploaded", if (photoUploaded) "Yes" else "No")
                        if (poultryType == "Broiler") {
                            put("fcr_value", fcrValue.toFloatOrNull())
                            put("dead_bird_reason", deadBirdReason)
                            put("growth_rate", if (growthRate) "Yes" else "No")
                            put("overcrowding", if (overcrowding) "Yes" else "No")
                            put("lameness", if (lameness) "Yes" else "No")
                        }
                        if (poultryType == "Layer") {
                            put("eggs_collected", eggsCollected.toIntOrNull())
                            put("egg_production", eggProduction.toFloatOrNull())
                            put("egg_quality", eggQuality)
                            put("nest_boxes_clean", if (nestBoxesClean) "Yes" else "No")
                            put("cannibalism", if (cannibalism) "Yes" else "No")
                        }
                        if (poultryType == "Breeder") {
                            put("fertile_eggs", fertileEggs.toIntOrNull())
                            put("hatchability", hatchability.toFloatOrNull())
                            put("male_female_ratio", if (maleFemaleRatio) "Yes" else "No")
                            put("mating_behavior", if (matingBehavior) "Yes" else "No")
                            put("eggs_handled", if (eggsHandled) "Yes" else "No")
                        }
                    }
                    try {
                        Log.d("PoultryForm", "Attempting insert with values: $values")
                        val result = dbHelper.writableDatabase.insert("poultry_data", null, values)
                        if (result == -1L) {
                            Log.e("PoultryForm", "Insert failed with values: $values")
                            Toast.makeText(context, "Failed to save data. Check logs.", Toast.LENGTH_LONG).show()
                        } else {
                            Log.d("PoultryForm", "Insert succeeded with ID: $result")
                            Toast.makeText(context, "Data saved successfully", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("PoultryForm", "Database error: ${e.message}", e)
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save Data", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun CheckboxField(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun DropdownField(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
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
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
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

@Composable
fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

fun getCurrentDateTime(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

fun getBirdAge(): Int = 30