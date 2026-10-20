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
        title = "Pindai Materi Belajar",
        description = "Foto catatan, buku pelajaran, dan modul dengan kamera. Teknologi OCR kami mengekstrak teks secara instan",
        icon = Icons.Default.CameraAlt
    ),
    OnboardingPage(
        title = "Ringkasan AI",
        description = "AI canggih menganalisis materimu dan membuat ringkasan yang jelas dan padat dengan menyoroti konsep-konsep penting.",
        icon = Icons.Default.AutoAwesome
    ),
    OnboardingPage(
        title = "Kuis Interaktif",
        description = "Uji pemahamanmu dengan kuis buatan AI yang dirancang untuk melatih daya ingat secara aktif.",
        icon = Icons.Default.Quiz
    ),
    OnboardingPage(
        title = "Materi Terorganisir",
        description = "Simpan semua mata pelajaran dan materimu dalam folder agar mudah diakses kapan saja.",
        icon = Icons.Default.Folder
    )
)