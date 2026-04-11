package com.example.teman_belajar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.teman_belajar.Login.LoginScreen
import com.example.teman_belajar.Login.LoginViewModel
import com.example.teman_belajar.Register.ui.RegistrationScreen
import com.example.teman_belajar.Register.ui.RegistrationViewModel
import com.example.teman_belajar.splash.SplashScreen
import com.example.teman_belajar.theme.TemanBelajarTheme

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val registrationViewModel: RegistrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TemanBelajarTheme {
                val startDestination by mainViewModel.startDestination.collectAsState()

                if (startDestination == "loading") {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("splash") {
                            SplashScreen(
                                onOnboardingFinished = {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("HOME")
                            }
                        }

                        composable("login") {
                            val uiState by loginViewModel.uiState.collectAsState()
                            loginViewModel.onNavigateToRegister = {
                                navController.navigate("register")
                            }

                            loginViewModel.onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }

                            LoginScreen(
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
}