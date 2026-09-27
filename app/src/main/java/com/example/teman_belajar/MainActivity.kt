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
import com.example.teman_belajar.folderdetail.SummaryDetailScreen
import com.example.teman_belajar.summarylist.SummaryListScreen
import com.example.teman_belajar.summarylist.SummaryListViewModel
import com.example.teman_belajar.quiz.QuizListScreen
import com.example.teman_belajar.quiz.QuizListViewModel
import com.example.teman_belajar.profile.ProfileScreen
import com.example.teman_belajar.profile.ProfileViewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val registrationViewModel: RegistrationViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val forgotPasswordViewModel: com.example.teman_belajar.forgotpassword.ForgotPasswordViewModel by viewModels()
    private val folderDetailViewModel: com.example.teman_belajar.folderdetail.FolderDetailViewModel by viewModels()
    private val smartSummaryDetailViewModel: com.example.teman_belajar.folderdetail.SmartSummaryDetailViewModel by viewModels()
    private val quizHistoryViewModel: com.example.teman_belajar.quizhistory.QuizHistoryViewModel by viewModels()
    private val quizViewModel: com.example.teman_belajar.quiz.QuizViewModel by viewModels()
    private val summaryListViewModel: SummaryListViewModel by viewModels()
    private val quizListViewModel: QuizListViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appDarkMode by mainViewModel.isDarkMode.collectAsState(initial = false)

            TemanBelajarTheme(darkTheme = appDarkMode) {
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

                            homeViewModel.onNavigateToQuizHistory = {
                                navController.navigate("quiz_history")
                            }

                            homeViewModel.onNavigateToProfile = {
                                navController.navigate("profile") {
                                    launchSingleTop = true
                                }
                            }

                            homeViewModel.onNavigateToSummaryList = {
                                navController.navigate("summary_list")
                            }

                            homeViewModel.onNavigateToQuizList = {
                                navController.navigate("quiz_list")
                            }

                            HomeScreen(
                                uiState = uiState,
                                onEvent = homeViewModel::onEvent
                            )
                        }

                        composable("summary_list") {
                            val uiState by summaryListViewModel.uiState.collectAsState()

                            summaryListViewModel.onNavigateBack = {
                                navController.popBackStack()
                            }

                            summaryListViewModel.onNavigateToSummaryDetail = { summaryId ->
                                navController.navigate("summary_detail/$summaryId")
                            }

                            SummaryListScreen(
                                uiState = uiState,
                                onEvent = summaryListViewModel::onEvent
                            )
                        }

                        composable("quiz_list") {
                            val uiState by quizListViewModel.uiState.collectAsState()

                            quizListViewModel.onNavigateBack = {
                                navController.popBackStack()
                            }

                            quizListViewModel.onNavigateToQuizSession = { quizId ->
                                quizViewModel.fetchQuiz(quizId)
                                navController.navigate("quiz_session")
                            }

                            QuizListScreen(
                                uiState = uiState,
                                onEvent = quizListViewModel::onEvent
                            )
                        }

                        composable("quiz_history") {
                            val uiState by quizHistoryViewModel.uiState.collectAsState()

                            quizHistoryViewModel.onNavigateBack = {
                                navController.popBackStack()
                            }

                            quizHistoryViewModel.onNavigateToResult = {
                                navController.navigate("quiz_history_result")
                            }

                            quizHistoryViewModel.onNavigateToQuizSession = { quizId, attemptedQuizId ->
                                quizViewModel.resumeQuiz(quizId, attemptedQuizId)
                                navController.navigate("quiz_session")
                            }

                            com.example.teman_belajar.quizhistory.QuizHistoryScreen(
                                uiState = uiState,
                                onEvent = quizHistoryViewModel::onEvent,
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToProfile = {
                                    navController.navigate("profile") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable("quiz_history_result") {
                            val uiState by quizHistoryViewModel.uiState.collectAsState()

                            quizHistoryViewModel.onNavigateToExplanation = { quizId, attemptedQuizId ->
                                quizViewModel.fetchAttemptDetail(quizId, attemptedQuizId)
                                navController.navigate("quiz_explanation")
                            }

                            com.example.teman_belajar.quizhistory.QuizHistoryResultScreen(
                                uiState = uiState,
                                onEvent = quizHistoryViewModel::onEvent
                            )
                        }

                        composable("profile") {
                            val uiState by profileViewModel.uiState.collectAsState()

                            profileViewModel.onNavigateBack = {
                                navController.popBackStack()
                            }

                            profileViewModel.onLogout = {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }

                            ProfileScreen(
                                uiState = uiState,
                                onEvent = profileViewModel::onEvent,
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToQuizHistory = {
                                    navController.navigate("quiz_history") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable("folder_detail/{folderId}/{folderName}") { backStackEntry ->
                            val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
                            val folderName = backStackEntry.arguments?.getString("folderName") ?: ""

                            LaunchedEffect(folderId) {
                                folderDetailViewModel.setFolderData(folderId, folderName)
                            }

                            val uiState by folderDetailViewModel.uiState.collectAsState()

                            folderDetailViewModel.onNavigateBack = {
                                navController.popBackStack()
                            }

                            folderDetailViewModel.onNavigateToSummaryDetail = { summaryId ->
                                if (summaryId != null) {
                                    navController.navigate("summary_detail/$summaryId")
                                }
                            }

                            com.example.teman_belajar.folderdetail.FolderDetailScreen(
                                viewModel = folderDetailViewModel,
                                uiState = uiState,
                                onEvent = folderDetailViewModel::onEvent
                            )
                        }

                        composable("summary_detail/{summaryId}") { backStackEntry ->

                            val summaryId = backStackEntry.arguments?.getString("summaryId") ?: ""

                            LaunchedEffect(summaryId) {
                                if (summaryId != "new") {
                                    smartSummaryDetailViewModel.fetchSummary(summaryId)
                                }
                            }

                            val uiState by smartSummaryDetailViewModel.uiState.collectAsState()

                            smartSummaryDetailViewModel.onNavigateBack = {
                                navController.popBackStack()
                            }

                            smartSummaryDetailViewModel.onNavigateToQuiz = {
                                navController.navigate("quiz_list")
                            }

                            SummaryDetailScreen(
                                uiState = uiState,
                                onEvent = smartSummaryDetailViewModel::onEvent
                            )
                        }

                        composable("quiz_session") {
                            val uiState by quizViewModel.uiState.collectAsState()

                            quizViewModel.onNavigateBack = {
                                navController.popBackStack()
                            }

                            quizViewModel.onNavigateToResult = {
                                navController.navigate("quiz_result") {
                                    popUpTo("quiz_session") { inclusive = true }
                                }
                            }

                            com.example.teman_belajar.quiz.QuizScreen(
                                uiState = uiState,
                                onEvent = quizViewModel::onEvent
                            )
                        }

                        composable("quiz_result") {
                            val uiState by quizViewModel.uiState.collectAsState()

                            quizViewModel.onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }

                            quizViewModel.onNavigateToExplanation = {
                                navController.navigate("quiz_explanation")
                            }

                            com.example.teman_belajar.quiz.QuizResultScreen(
                                uiState = uiState,
                                onEvent = quizViewModel::onEvent
                            )
                        }

                        composable("quiz_explanation") {
                            val uiState by quizViewModel.uiState.collectAsState()

                            quizViewModel.onNavigateBack = {
                                navController.popBackStack()
                            }

                            com.example.teman_belajar.quiz.QuizExplanationScreen(
                                uiState = uiState,
                                onEvent = quizViewModel::onEvent
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
