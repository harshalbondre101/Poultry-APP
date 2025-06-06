package com.example.pa

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.Cursor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "poultry.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE poultry_data (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                poultry_type TEXT,
                date_time TEXT,
                flock_id TEXT,
                feed_given REAL,
                water_consumed TEXT,
                dead_birds INTEGER,
                vaccine_given TEXT,
                vaccine_name TEXT,
                avg_bird_weight REAL,
                bird_age INTEGER,
                temp_humidity TEXT,
                remarks TEXT,
                shed_cleaning TEXT,
                feeders_clean TEXT,
                litter_condition TEXT,
                sick_birds TEXT,
                sick_symptoms TEXT,
                bird_behavior TEXT,
                ventilation TEXT,
                bad_smell TEXT,
                biosecurity TEXT,
                footbath TEXT,
                photo_uploaded TEXT,
                fcr_value REAL,
                dead_bird_reason TEXT,
                growth_rate TEXT,
                overcrowding TEXT,
                lameness TEXT,
                eggs_collected INTEGER,
                egg_production REAL,
                egg_quality TEXT,
                nest_boxes_clean TEXT,
                cannibalism TEXT,
                fertile_eggs INTEGER,
                hatchability REAL,
                male_female_ratio TEXT,
                mating_behavior TEXT,
                eggs_handled TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS poultry_data")
        onCreate(db)
    }

    fun getFilteredData(
        poultryType: String? = null,
        flockId: String? = null,
        date: String? = null
    ): Cursor {
        val db = readableDatabase
        val selection = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        if (poultryType != null && poultryType != "All") {
            selection.add("poultry_type = ?")
            selectionArgs.add(poultryType)
        }
        if (flockId != null && flockId != "All") {
            selection.add("flock_id = ?")
            selectionArgs.add(flockId)
        }
        if (date != null) {
            selection.add("date_time LIKE ?")
            selectionArgs.add("$date%")
        }

        val selectionString = if (selection.isNotEmpty()) selection.joinToString(" AND ") else null
        return db.query(
            "poultry_data",
            null,
            selectionString,
            selectionArgs.toTypedArray(),
            null,
            null,
            "date_time DESC"
        )
    }

    // Chart Data Methods
    data class ChartData(val date: String, val value: Float)

    fun getFeedTrend(flockId: String, days: Int = 7): List<ChartData> {
        val db = readableDatabase
        val startDate = LocalDate.now().minusDays(days.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val data = mutableListOf<ChartData>()
        db.query(
            "poultry_data",
            arrayOf("date_time", "feed_given"),
            "flock_id = ? AND date_time >= ?",
            arrayOf(flockId, startDate),
            null,
            null,
            "date_time ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date_time")).substring(0, 10)
                val feed = cursor.getFloat(cursor.getColumnIndexOrThrow("feed_given"))
                data.add(ChartData(date, feed))
            }
        }
        return data
    }

    fun getMortalityTrend(flockId: String, days: Int = 7): List<ChartData> {
        val db = readableDatabase
        val startDate = LocalDate.now().minusDays(days.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val data = mutableListOf<ChartData>()
        val totalBirds = 100f // Placeholder: Replace with actual flock size
        db.query(
            "poultry_data",
            arrayOf("date_time", "dead_birds"),
            "flock_id = ? AND date_time >= ?",
            arrayOf(flockId, startDate),
            null,
            null,
            "date_time ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date_time")).substring(0, 10)
                val deadBirds = cursor.getInt(cursor.getColumnIndexOrThrow("dead_birds"))
                val mortalityRate = (deadBirds / totalBirds) * 100
                data.add(ChartData(date, mortalityRate))
            }
        }
        return data
    }

    fun getEggProductionTrend(flockId: String, days: Int = 7): List<ChartData> {
        val db = readableDatabase
        val startDate = LocalDate.now().minusDays(days.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val data = mutableListOf<ChartData>()
        db.query(
            "poultry_data",
            arrayOf("date_time", "eggs_collected"),
            "flock_id = ? AND poultry_type = ? AND date_time >= ?",
            arrayOf(flockId, "Layer", startDate),
            null,
            null,
            "date_time ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date_time")).substring(0, 10)
                val eggs = cursor.getInt(cursor.getColumnIndexOrThrow("eggs_collected")).toFloat()
                data.add(ChartData(date, eggs))
            }
        }
        return data
    }

    // Alerts
    data class Alert(val message: String, val severity: String)

    fun getAlerts(): List<Alert> {
        val alerts = mutableListOf<Alert>()
        val db = readableDatabase
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val twoDaysAgo = LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)

        // Alert 1: Missing Feed Entry
        db.query(
            "poultry_data",
            arrayOf("feed_given"),
            "date_time LIKE ?",
            arrayOf("$today%"),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                if (cursor.getFloat(cursor.getColumnIndexOrThrow("feed_given")) == 0f) {
                    alerts.add(Alert("Feed input missing. Enter or verify data.", "Alert"))
                } else {
                    //pass
                }
            } else {
                alerts.add(Alert("Feed input missing. Enter or verify data.", "Alert"))
            }
        }

        // Alert 2: Low Water Consumption
        db.query(
            "poultry_data",
            arrayOf("water_consumed", "date_time"),
            "date_time >= ? AND water_consumed = ?",
            arrayOf(twoDaysAgo, "Low"),
            null,
            null,
            null
        ).use { cursor ->
            val dates = mutableSetOf<String>()
            while (cursor.moveToNext()) {
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date_time")).substring(0, 10)
                dates.add(date)
            }
            if (dates.size >= 2) {
                alerts.add(Alert("Possible dehydration. Inspect water lines & supply.", "Alert"))
            }
        }

        // Alert 3: High Mortality Rate
        db.query(
            "poultry_data",
            arrayOf("dead_birds"),
            "date_time LIKE ?",
            arrayOf("$today%"),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val deadBirds = cursor.getInt(cursor.getColumnIndexOrThrow("dead_birds"))
                val totalBirds = 100 // Placeholder: Replace with actual flock size
                if (deadBirds > 0.05 * totalBirds) {
                    alerts.add(Alert("Unusual bird death. Immediate vet check.", "Critical"))
                }
            }
        }

        // Alert 4: Skipped Vaccine
        db.query(
            "poultry_data",
            arrayOf("bird_age", "vaccine_given"),
            "date_time LIKE ? AND bird_age % 7 = 0 AND vaccine_given = ?",
            arrayOf("$today%", "No"),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                alerts.add(Alert("Vaccine due but not recorded.", "Warning"))
            }
        }

        // Alert 5: Unusual Temperature
        db.query(
            "poultry_data",
            arrayOf("temp_humidity"),
            "date_time LIKE ? AND (temp_humidity = ? OR temp_humidity = ?)",
            arrayOf("$today%", "High", "Low"),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                alerts.add(Alert("Environmental stress risk. Adjust ventilation.", "Alert"))
            }
        }

        // Alert 6: Litter Wet + Smell Bad
        db.query(
            "poultry_data",
            arrayOf("litter_condition", "bad_smell"),
            "date_time LIKE ? AND litter_condition = ? AND bad_smell = ?",
            arrayOf("$today%", "Very Wet", "Yes"),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                alerts.add(Alert("Ammonia hazard. Clean urgently.", "Alert"))
            }
        }

        // Alert 7: Bird Behavior = Lethargic
        db.query(
            "poultry_data",
            arrayOf("bird_behavior"),
            "date_time LIKE ? AND bird_behavior = ?",
            arrayOf("$today%", "Lethargic"),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                alerts.add(Alert("Health problem suspected. Observe closely.", "Alert"))
            }
        }

        // Alert 8: Sick Birds Present
        db.query(
            "poultry_data",
            arrayOf("sick_birds", "sick_symptoms"),
            "date_time LIKE ? AND sick_birds = ?",
            arrayOf("$today%", "Yes"),
            null,
            null,
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val symptoms = cursor.getString(cursor.getColumnIndexOrThrow("sick_symptoms")) ?: ""
                if (symptoms.contains("diarrhea", true) || symptoms.contains("ruffled feathers", true)) {
                    alerts.add(Alert("Possible disease onset.", "Alert"))
                }
            }
        }

        // Alert 9: Cleaning Skipped 2 Days
        db.query(
            "poultry_data",
            arrayOf("shed_cleaning", "date_time"),
            "date_time >= ? AND shed_cleaning = ?",
            arrayOf(twoDaysAgo, "No"),
            null,
            null,
            null
        ).use { cursor ->
            val dates = mutableSetOf<String>()
            while (cursor.moveToNext()) {
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date_time")).substring(0, 10)
                dates.add(date)
            }
            if (dates.size >= 2) {
                alerts.add(Alert("Hygiene routine skipped. Risk of contamination.", "Alert"))
            }
        }

        // Alert 10: Ventilation Off
        db.query(
            "poultry_data",
            arrayOf("ventilation"),
            "date_time LIKE ? AND ventilation = ?",
            arrayOf("$today%", "Off"),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                alerts.add(Alert("Ventilation inactive. Risk of heatstroke/ammonia buildup.", "Warning"))
            }
        }
        return alerts
    }
}