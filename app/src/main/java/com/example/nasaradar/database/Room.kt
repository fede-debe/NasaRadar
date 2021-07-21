package com.example.nasaradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AsteroidDao {

    @Query("SELECT * FROM asteroids ORDER BY closeApproachDate DESC")
    fun getAllAsteroids(): LiveData<List<DatabaseEntities>>

    @Query("SELECT * FROM asteroids WHERE closeApproachDate = :startDate ORDER BY closeApproachDate DESC")
    fun getAsteroidDay(startDate: String): LiveData<List<DatabaseEntities>>

    @Query("SELECT * FROM asteroids WHERE closeApproachDate BETWEEN :startDate AND :endDate ORDER BY closeApproachDate DESC")
    fun getAsteroidsDate(startDate: String, endDate: String): LiveData<List<DatabaseEntities>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroid: DatabaseEntities)
}

@Database(entities = [DatabaseEntities::class], version = 1, exportSchema = false)
abstract class AsteroidDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
}

private lateinit var INSTANCE: AsteroidDatabase

fun getDatabase(context: Context): AsteroidDatabase {
    synchronized(AsteroidDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                AsteroidDatabase::class.java,
                "asteroids").build()
        }
    }
    return INSTANCE
}