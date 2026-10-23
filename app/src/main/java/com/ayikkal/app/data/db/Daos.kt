package com.ayikkal.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkTypeDao {
    @Query("SELECT * FROM drink_types ORDER BY category, custom DESC, name, volumeMl")
    fun observeAll(): Flow<List<DrinkType>>

    @Query("SELECT COUNT(*) FROM drink_types")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(items: List<DrinkType>)

    @Insert
    suspend fun insert(item: DrinkType): Long

    @Query("SELECT * FROM drink_types WHERE id = :id")
    suspend fun get(id: Long): DrinkType?
}

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: Session): Long

    @Update
    suspend fun update(session: Session)

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun get(id: Long): Session?

    @Query("SELECT * FROM sessions WHERE id = :id")
    fun observe(id: Long): Flow<Session?>

    @Query("SELECT * FROM sessions WHERE endedAt IS NOT NULL ORDER BY startedAt DESC LIMIT 30")
    fun observeEnded(): Flow<List<Session>>
}

@Dao
interface DrinkEntryDao {
    @Insert
    suspend fun insert(entry: DrinkEntry): Long

    @Query("SELECT * FROM drink_entries WHERE sessionId = :sessionId ORDER BY consumedAt")
    fun observeForSession(sessionId: Long): Flow<List<DrinkEntry>>

    @Query("SELECT * FROM drink_entries WHERE sessionId = :sessionId ORDER BY consumedAt")
    suspend fun forSession(sessionId: Long): List<DrinkEntry>

    @Query(
        "DELETE FROM drink_entries WHERE id = (SELECT id FROM drink_entries " +
            "WHERE sessionId = :sessionId AND drinkTypeId = :typeId ORDER BY consumedAt DESC LIMIT 1)"
    )
    suspend fun deleteLatestOfType(sessionId: Long, typeId: Long): Int
}

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: GuardEvent): Long

    @Query("SELECT * FROM events WHERE sessionId = :sessionId ORDER BY at")
    fun observeForSession(sessionId: Long): Flow<List<GuardEvent>>

    @Query("SELECT * FROM events WHERE type IN (:types) ORDER BY at DESC LIMIT :limit")
    fun observeRecent(types: List<EventType>, limit: Int): Flow<List<GuardEvent>>
}

@Dao
interface BlockedContactDao {
    @Query("SELECT * FROM blocked_contacts ORDER BY name")
    fun observeAll(): Flow<List<BlockedContact>>

    @Insert
    suspend fun insert(contact: BlockedContact): Long

    @Delete
    suspend fun delete(contact: BlockedContact)
}

@Dao
interface LockedAppDao {
    @Query("SELECT * FROM locked_apps ORDER BY label")
    fun observeAll(): Flow<List<LockedApp>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(app: LockedApp): Long

    @Upsert
    suspend fun upsert(app: LockedApp)

    @Delete
    suspend fun delete(app: LockedApp)
}
