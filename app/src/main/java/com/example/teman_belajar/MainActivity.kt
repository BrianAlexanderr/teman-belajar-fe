package com.example.teman_belajar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.teman_belajar.Login.LoginScreen
import com.example.teman_belajar.Login.LoginViewModel
import com.example.teman_belajar.Register.ui.RegistrationScreen
import com.example.teman_belajar.Register.ui.RegistrationViewModel
import com.example.teman_belajar.theme.TemanBelajarTheme

class MainActivity : ComponentActivity() {
    
    private val loginViewModel: LoginViewModel by viewModels()
    private val registrationViewModel: RegistrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TemanBelajarTheme {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        val uiState by loginViewModel.uiState.collectAsState()
                        loginViewModel.onNavigateToRegister = {
                            navController.navigate("register")
                        }
                        
                        LoginScreen(
                            uiState = uiState,
                            onEvent = loginViewModel::onEvent
                        )
                    }
                    
                    composable("register") {
                        val uiState by registrationViewModel.uiState.collectAsState()
                        registrationViewModel.onNavigateToLogin = {
                            navController.navigate("login") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                        
                        RegistrationScreen(
                            uiState = uiState,
                            onEvent = registrationViewModel::onEvent
                        )
                    }
                }
            }
        }
    }
}
