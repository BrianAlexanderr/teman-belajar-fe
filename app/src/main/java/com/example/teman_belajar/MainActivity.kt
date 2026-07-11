package com.example.teman_belajar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.teman_belajar.login.LoginScreen
import com.example.teman_belajar.login.LoginViewModel
import com.example.teman_belajar.register.ui.RegistrationScreen
import com.example.teman_belajar.register.ui.RegistrationViewModel
import com.example.teman_belajar.components.SessionExpiredDialog
import com.example.teman_belajar.home.HomeScreen
import com.example.teman_belajar.home.HomeViewModel
import com.example.teman_belajar.splash.SplashScreen
import com.example.teman_belajar.theme.TemanBelajarTheme
import com.example.teman_belajar.utils.SessionManager

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val registrationViewModel: RegistrationViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val forgotPasswordViewModel: com.example.teman_belajar.forgotpassword.ForgotPasswordViewModel by viewModels()

    private val folderDetailViewModel: com.example.teman_belajar.folderdetail.FolderDetailViewModel by viewModels()

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

                    var isSessionExpiredDialogOpen by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        SessionManager.sessionExpiredEvent.collect {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                            isSessionExpiredDialogOpen = true
                        }
                    }

                    if (isSessionExpiredDialogOpen) {
                        SessionExpiredDialog(
                            onConfirm = {
                                isSessionExpiredDialogOpen = false
                            }
                        )
                    }

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
                            val uiState by homeViewModel.uiState.collectAsState()
                            homeViewModel.onNavigateToLogin = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }

                            homeViewModel.onNavigateToFolderDetail = { id, name ->
                                navController.navigate("folder_detail/$id/$name")
                            }

                            HomeScreen(
                                uiState = uiState,
                                onEvent = homeViewModel::onEvent
                            )
                        }

                        composable("folder_detail/{folderId}/{folderName}") { backStackEntry ->
                            val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
                            val folderName = backStackEntry.arguments?.getString("folderName") ?: ""

                            folderDetailViewModel.setFolderData(folderId, folderName)

                            val uiState by folderDetailViewModel.uiState.collectAsState()

                            folderDetailViewModel.onNavigateBack = {
                                navController.popBackStack()
                            }

                            com.example.teman_belajar.folderdetail.FolderDetailScreen(
                                viewModel = folderDetailViewModel,
                                uiState = uiState,
                                onEvent = folderDetailViewModel::onEvent
                            )
                        }

                        composable("login") {
                            val uiState by loginViewModel.uiState.collectAsState()
                            loginViewModel.onNavigateToRegister = {
                                navController.navigate("register")
                            }

                            loginViewModel.onNavigateToForgotPassword = {
                                navController.navigate("forgot_password")
                            }

                            loginViewModel.onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
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

                        composable("forgot_password") {
                            val uiState by forgotPasswordViewModel.uiState.collectAsState()

                            forgotPasswordViewModel.onNavigateBack = {
                                navController.popBackStack()
                            }

                            com.example.teman_belajar.forgotpassword.ForgotPasswordScreen(
                                uiState = uiState,
                                onEvent = forgotPasswordViewModel::onEvent
                            )
                        }
                    }
                }
            }
        }
    }
}
