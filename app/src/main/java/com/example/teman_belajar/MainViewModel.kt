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

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)

    private val _startDestination = MutableStateFlow("loading")
    val startDestination: StateFlow<String> = _startDestination.asStateFlow()

    init {
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            val isFirstTime = userPreferences.isFirstTimeFlow.first()
            val isLoggedIn = userPreferences.isLoggedInFlow.first()

            _startDestination.value = when {
                isFirstTime -> "splash"
                !isLoggedIn -> "login"
                else -> "home"
            }
        }
    }
}