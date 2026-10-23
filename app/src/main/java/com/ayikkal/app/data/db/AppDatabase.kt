package com.ayikkal.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DrinkType::class,
        Session::class,
        DrinkEntry::class,
        GuardEvent::class,
        BlockedContact::class,
        LockedApp::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drinkTypes(): DrinkTypeDao
    abstract fun sessions(): SessionDao
    abstract fun drinkEntries(): DrinkEntryDao
    abstract fun events(): EventDao
    abstract fun blockedContacts(): BlockedContactDao
    abstract fun lockedApps(): LockedAppDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "ayikkal.db").build()
    }
}
