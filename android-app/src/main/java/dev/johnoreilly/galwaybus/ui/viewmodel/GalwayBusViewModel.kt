package dev.johnoreilly.galwaybus.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import co.touchlab.kermit.Kermit
import com.surrus.galwaybus.common.GalwayBusDeparture
import com.surrus.galwaybus.common.GalwayBusRepository
import com.surrus.galwaybus.common.model.BusStop
import com.surrus.galwaybus.common.model.Departure
import com.surrus.galwaybus.common.model.Location
import com.surrus.galwaybus.common.model.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.switchMap
import kotlinx.coroutines.launch


sealed class UiState<out T: Any> {
    object Loading : UiState<Nothing>()
    data class Success<out T : Any>(val data: T) : UiState<T>()
    data class Error(val exception: Exception) : UiState<Nothing>()
}


class GalwayBusViewModel(
        private val galwayBusRepository: GalwayBusRepository,
        private val logger: Kermit
) : ViewModel() {

    val uiState = MutableLiveData<UiState<List<BusStop>>>()

    //val busDepartureList = MutableLiveData<List<GalwayBusDeparture>>(emptyList())

    val stopRef = MutableLiveData<String>("")
    val busDepartureList = stopRef.switchMap { pollBusDepartures(it).asLiveData() }



    val location: MutableLiveData<Location> = MutableLiveData()
    val cameraPosition: MutableLiveData<Location> = MutableLiveData()
    private val zoomLevel: MutableLiveData<Float> = MutableLiveData()

    private var pollingJob: Job? = null


    init {
        location.value = Location(53.2743394, -9.0514163) // default if we can't get location
        getNearestStops(Location(53.2743394, -9.0514163))
    }

    fun setLocation(loc: Location) {
        location.value = loc
    }

    fun setStopRef(stopRefValue: String) {
        stopRef.value = stopRefValue
    }

    fun getNearestStops(location: Location) {
        viewModelScope.launch {
            val result = galwayBusRepository.fetchNearestStops(location.latitude, location.longitude)
            when (result) {
                is Result.Success -> {
                    uiState.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    uiState.value = UiState.Error(result.exception)
                }
            }
        }
    }

    private fun pollBusDepartures(stopRef: String): Flow<List<GalwayBusDeparture>> = flow {
        while (true) {
            val result = galwayBusRepository.fetchBusStopDepartures(stopRef)
            if (result is Result.Success) {
                logger.d("GalwayBusViewModel") { result.data.toString() }
                emit(result.data)
            }
            delay(10000)
        }
    }

/*
    fun getBusStopDepartures(stopRef: String) {
        pollingJob?.cancel()

        busDepartureList.value = emptyList()
        pollingJob = viewModelScope.launch {
            while (true) {
                val result = galwayBusRepository.fetchBusStopDepartures(stopRef)
                if (result is Result.Success) {
                    busDepartureList.value = result.data
                    logger.d { result.data.toString() }
                }
                delay(10000)
            }
        }
    }

 */
    companion object {

    }
}