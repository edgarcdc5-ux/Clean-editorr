package com.cassinopros.app.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName="bankroll")
data class BankrollEntity(@PrimaryKey val id:Int=1,val initialCents:Long=100000,val currentCents:Long=100000,val reserveCents:Long=0,val updatedAt:Long=System.currentTimeMillis())

@Entity(tableName="sessions")
data class SessionEntity(
 @PrimaryKey(autoGenerate=true) val id:Long=0,val casinoName:String="",val casinoPackage:String="",val slotName:String="",
 val startingCents:Long=0,val endingCents:Long?=null,val wageredCents:Long=0,val receivedCents:Long=0,val resultCents:Long=0,
 val stopLossCents:Long=5000,val targetCents:Long=10000,val stakeCents:Long=100,val startedAt:Long=System.currentTimeMillis(),val endedAt:Long?=null,val status:String="ACTIVE")

@Entity(tableName="slots")
data class SlotEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val provider:String="",val casinoName:String="",val defaultStakeCents:Long=100,val notes:String="",val imageUri:String?=null)

@Dao interface BankrollDao{@Query("SELECT * FROM bankroll WHERE id=1") fun observe():Flow<BankrollEntity?>;@Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun save(value:BankrollEntity)}
@Dao interface SessionDao{
 @Query("SELECT * FROM sessions ORDER BY startedAt DESC") fun observeAll():Flow<List<SessionEntity>>
 @Insert suspend fun insert(value:SessionEntity):Long
 @Query("UPDATE sessions SET endingCents=:ending,resultCents=:result,endedAt=:endedAt,status='FINISHED' WHERE id=:id") suspend fun finish(id:Long,ending:Long,result:Long,endedAt:Long)
 @Query("UPDATE sessions SET wageredCents=wageredCents+:wagered,receivedCents=receivedCents+:received,resultCents=:result WHERE id=:id") suspend fun register(id:Long,wagered:Long,received:Long,result:Long)
}
@Dao interface SlotDao{@Query("SELECT * FROM slots ORDER BY name COLLATE NOCASE") fun observeAll():Flow<List<SlotEntity>>;@Insert suspend fun insert(slot:SlotEntity):Long;@Delete suspend fun delete(slot:SlotEntity)}

@Database(entities=[BankrollEntity::class,SessionEntity::class,SlotEntity::class],version=3,exportSchema=false)
abstract class AppDatabase:RoomDatabase(){
 abstract fun bankrollDao():BankrollDao
 abstract fun sessionDao():SessionDao
 abstract fun slotDao():SlotDao
 companion object{
  @Volatile private var INSTANCE:AppDatabase?=null
  private val M2=object:Migration(1,2){override fun migrate(db:SupportSQLiteDatabase){db.execSQL("CREATE TABLE IF NOT EXISTS slots (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,name TEXT NOT NULL,provider TEXT NOT NULL,casinoName TEXT NOT NULL,defaultStakeCents INTEGER NOT NULL,notes TEXT NOT NULL,imageUri TEXT)")}}
  private val M3=object:Migration(2,3){override fun migrate(db:SupportSQLiteDatabase){
   db.execSQL("ALTER TABLE sessions ADD COLUMN casinoPackage TEXT NOT NULL DEFAULT ''")
   db.execSQL("ALTER TABLE sessions ADD COLUMN receivedCents INTEGER NOT NULL DEFAULT 0")
   db.execSQL("ALTER TABLE sessions ADD COLUMN stopLossCents INTEGER NOT NULL DEFAULT 5000")
   db.execSQL("ALTER TABLE sessions ADD COLUMN targetCents INTEGER NOT NULL DEFAULT 10000")
   db.execSQL("ALTER TABLE sessions ADD COLUMN stakeCents INTEGER NOT NULL DEFAULT 100")
  }}
  fun get(context:Context):AppDatabase=INSTANCE?:synchronized(this){INSTANCE?:Room.databaseBuilder(context.applicationContext,AppDatabase::class.java,"cassino_pros.db").addMigrations(M2,M3).build().also{INSTANCE=it}}
 }
}