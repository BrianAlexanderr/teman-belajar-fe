package com.example.teman_belajar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teman_belajar.utils.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _startDestination = MutableStateFlow("loading")
    val startDestination: StateFlow<String> = _startDestination.asStateFlow()

    init {
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            try {
                val result = withTimeoutOrNull(2000) {
                    val isFirstTime = userPreferences.isFirstTimeFlow.first()
                    val isLoggedIn = userPreferences.isLoggedInFlow.first()
                    
                    if (isFirstTime) "splash"
                    else if (!isLoggedIn) "login"
                    else "home"
                }
                _startDestination.value = result ?: "login"

            } catch (e: Exception) {
                _startDestination.value = "login"
            }
        }
    }
}
