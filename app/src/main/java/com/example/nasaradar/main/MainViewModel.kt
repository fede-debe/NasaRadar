package com.example.nasaradar.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.nasaradar.api.AsteroidApiService
import com.example.nasaradar.database.getDatabase
import com.example.nasaradar.models.Asteroid
import com.example.nasaradar.models.PictureOfDay
import com.example.nasaradar.repository.AsteroidRepository
import com.example.nasaradar.util.Constants.API_KEY
import com.example.nasaradar.util.FilterAsteroid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val asteroidRepository = AsteroidRepository(database)

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()
    val pictureOfDay: LiveData<PictureOfDay>
        get() = _pictureOfDay

    private val _navigateToAsteroidDetail = MutableLiveData<Asteroid>()
    val navigateToDetailAsteroid: LiveData<Asteroid>
        get() = _navigateToAsteroidDetail

    private var _filterAsteroid = MutableLiveData(FilterAsteroid.ALL)

    val asteroidList = Transformations.switchMap(_filterAsteroid) {
        when (it!!) {
            FilterAsteroid.WEEK -> asteroidRepository.weekAsteroids
            FilterAsteroid.TODAY -> asteroidRepository.todayAsteroids
            else -> asteroidRepository.allAsteroids
        }
    }

    init {
        viewModelScope.launch {
            asteroidRepository.refreshAsteroids()
            refreshPictureOfDay()
        }
    }

    fun onAsteroidClicked(asteroid: Asteroid) {
        _navigateToAsteroidDetail.value = asteroid
    }

    fun onAsteroidNavigated() {
        _navigateToAsteroidDetail.value = null
    }

    fun onChangeFilter(filter: FilterAsteroid) {
        _filterAsteroid.postValue(filter)
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct ViewModel")
        }
    }

    private suspend fun refreshPictureOfDay() {
        withContext(Dispatchers.IO) {
            try {
                _pictureOfDay.postValue(
                    AsteroidApiService.AsteroidApi.retrofitService.getPictureDay(API_KEY)
                )
            } catch (err: Exception) {
                Log.e("refreshPictureOfDay", err.printStackTrace().toString())
            }
        }
    }
}