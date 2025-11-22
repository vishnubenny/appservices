package com.vishnu.app.common

import android.location.Location
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * A simple singleton object to act as an event bus for location updates.
 * This allows the Service to communicate with the ViewModel.
 */
object LocationProvider {
    private val _locationFlow = MutableSharedFlow<Location>(extraBufferCapacity = 1)
    val locationFlow = _locationFlow.asSharedFlow()

    fun postLocation(location: Location) {
        _locationFlow.tryEmit(location)
    }
}
