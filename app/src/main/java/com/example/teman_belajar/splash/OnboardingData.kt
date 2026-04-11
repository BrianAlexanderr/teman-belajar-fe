package com.example.teman_belajar.splash

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
)

val onboardingPagesList = listOf(
    OnboardingPage(
        title = "Scan Study Material",
        description = "Capture your notes, textbooks, and handouts with your camera. Our OCR technology extracts text instantly",
        icon = Icons.Default.CameraAlt
    ),
    OnboardingPage(
        title = "AI Summarization",
        description = "Advanced AI analyzes your materials and creates clear, concise summaries highlighting key concepts.",
        icon = Icons.Default.AutoAwesome
    ),
    OnboardingPage(
        title = "Interactive Quizzes",
        description = "Test your knowledge with AI-generated quizzes designed for active recall and better retention.",
        icon = Icons.Default.Quiz
    ),
    OnboardingPage(
        title = "Organized Material",
        description = "Keep all your subjects and materials organized in folders for easy access anytime.",
        icon = Icons.Default.Folder
    )
)