package com.ayikkal.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class DrinkCategory(val label: String) {
    BEER("Bira"),
    WINE("Şarap"),
    RAKI("Rakı"),
    VODKA("Votka"),
    WHISKY("Viski"),
    TEQUILA("Tekila"),
    GIN("Cin"),
    COCKTAIL("Kokteyl"),
    OTHER("Diğer"),
}

/** Katalogdaki içki tanımı (marka + servis + alkol oranı). */
@Entity(tableName = "drink_types")
data class DrinkType(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: DrinkCategory,
    val name: String,
    val servingLabel: String,
    val volumeMl: Int,
    val abv: Double,
    val custom: Boolean = false,
)

/** Bir içme gecesi. Bitmemiş seansın id'si Prefs içinde tutulur. */
@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long? = null,
    val peakBac: Double = 0.0,
    val peakAt: Long? = null,
    val reportSeen: Boolean = false,
)

/** İçilen tek bir kadeh. Katalog değişse bile rapor bozulmasın diye değerler kopyalanır. */
@Entity(tableName = "drink_entries", indices = [Index("sessionId")])
data class DrinkEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val drinkTypeId: Long,
    val name: String,
    val servingLabel: String,
    val category: DrinkCategory,
    val volumeMl: Int,
    val abv: Double,
    val consumedAt: Long,
)

enum class EventType {
    SESSION_START,
    SESSION_END,
    WATER,
    TEST_PASSED,
    TEST_FAILED,
    TEST_MISSED,
    DRUNK_MODE_ON,
    UNLOCKED,
    APP_BLOCKED,
    CALL_BLOCKED,
    INCOMING_BLOCKED,
    SETTINGS_BLOCKED,
    SOBER_TEST_FAILED,
    SAFE_CALL,
    LOCATION_SENT,
}

/** Gece boyunca olan her şeyin kara kutusu; sabah raporu buradan üretilir. */
@Entity(tableName = "events", indices = [Index("sessionId")])
data class GuardEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long?,
    val at: Long,
    val type: EventType,
    val detail: String = "",
)

@Entity(tableName = "blocked_contacts")
data class BlockedContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val number: String,
    val normalized: String,
)

@Entity(tableName = "locked_apps")
data class LockedApp(
    @PrimaryKey val packageName: String,
    val label: String,
    val enabled: Boolean = true,
)
