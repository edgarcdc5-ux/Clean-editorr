package com.cassinopros.app.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "bankroll")
data class BankrollEntity(
    @PrimaryKey val id: Int = 1,
    val initialCents: Long = 100000,
    val currentCents: Long = 100000,
    val reserveCents: Long = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val casinoName: String = "",
    val slotName: String = "",
    val startingCents: Long = 0,
    val endingCents: Long? = null,
    val wageredCents: Long = 0,
    val resultCents: Long = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val status: String = "ACTIVE"
)

@Dao
interface BankrollDao {
    @Query("SELECT * FROM bankroll WHERE id = 1")
    fun observe(): Flow<BankrollEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(value: BankrollEntity)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<SessionEntity>>
    @Insert
    suspend fun insert(value: SessionEntity): Long
    @Query("UPDATE sessions SET endingCents=:ending, resultCents=:result, endedAt=:endedAt, status='FINISHED' WHERE id=:id")
    suspend fun finish(id: Long, ending: Long, result: Long, endedAt: Long)
}

@Database(entities = [BankrollEntity::class, SessionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bankrollDao(): BankrollDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "cassino_pros.db"
            ).build().also { INSTANCE = it }
        }
    }
}
