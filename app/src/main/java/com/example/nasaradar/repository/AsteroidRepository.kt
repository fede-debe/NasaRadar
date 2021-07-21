package com.example.nasaradar.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.nasaradar.util.Constants.API_KEY
import com.example.nasaradar.api.AsteroidApiService
import com.example.nasaradar.api.parseAsteroidsJsonResult
import com.example.nasaradar.database.AsteroidDatabase
import com.example.nasaradar.database.asDatabaseModel
import com.example.nasaradar.database.asDomainModel
import com.example.nasaradar.models.Asteroid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AsteroidRepository(private val database: AsteroidDatabase) {

    @RequiresApi(Build.VERSION_CODES.O)
    private val startDate = LocalDateTime.now()

    @RequiresApi(Build.VERSION_CODES.O)
    private val endDate = LocalDateTime.now().minusDays(7)

    val allAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getAllAsteroids()) {
            it.asDomainModel()
        }

    @RequiresApi(Build.VERSION_CODES.O)
    val todayAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getAsteroidDay(startDate.format(DateTimeFormatter.ISO_DATE))) {
            it.asDomainModel()
        }

    @RequiresApi(Build.VERSION_CODES.O)
    val weekAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(
            database.asteroidDao.getAsteroidsDate(
                startDate.format(DateTimeFormatter.ISO_DATE),
                endDate.format(DateTimeFormatter.ISO_DATE)
            )
        ) {
            it.asDomainModel()
        }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            try {
                val asteroids = AsteroidApiService.AsteroidApi.retrofitService.getNearAsteroids(API_KEY)
                val result = parseAsteroidsJsonResult(JSONObject(asteroids))
                database.asteroidDao.insertAll(*result.asDatabaseModel())
                Log.d("Refresh Asteroids", "Success")
            } catch (err: Exception) {
                Log.e("Failed: AsteroidRepFile", err.message.toString())
            }
        }
    }
}