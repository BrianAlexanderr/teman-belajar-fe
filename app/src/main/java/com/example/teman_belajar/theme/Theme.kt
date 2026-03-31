package com.example.teman_belajar.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppColors {
    val Purple        = Color(0xFF7C3AED)
    val PurpleLight   = Color(0xFFEDE9FE)
    val PurpleDark    = Color(0xFF5B21B6)
    val PurpleDot     = Color(0xFFD8B4FE)
    
    val TextPrimary   = Color(0xFF1A1A2E)
    val TextSecondary = Color(0xFF6B7280)
    val TextSub       = Color(0xFF6B7280)
    
    val Background    = Color(0xFFF9FAFB)
    val BgColor       = Color(0xFFF8F8F8)
    val Surface       = Color.White
    
    val InputBorder   = Color(0xFFE5E7EB)
    val GrayDot       = Color(0xFFE5E7EB)
    
    val Error         = Color(0xFFEF4444)
    val ErrorSurface  = Color(0xFFFFF5F5)
    val Success       = Color(0xFF10B981)
    
    val DecorationTop = Color(0xFFEDE9FE)
    val DecorationBot = Color(0xFFF3E8FF)
    
    val White         = Color.White
}

object RegistrationColors {
    val Purple         = AppColors.Purple
    val PurpleLight    = AppColors.PurpleLight
    val PurpleDark     = AppColors.PurpleDark
    val TextPrimary    = AppColors.TextPrimary
    val TextSecondary  = AppColors.TextSecondary
    val InputBorder    = AppColors.InputBorder
    val InputFocused   = AppColors.Purple
    val Error          = AppColors.Error
    val ErrorSurface   = AppColors.ErrorSurface
    val Success        = AppColors.Success
    val Background     = AppColors.Background
    val Surface        = AppColors.Surface
    val DecorationTop  = AppColors.DecorationTop
    val DecorationBot  = AppColors.DecorationBot
    val CheckRingOuter = Color(0xFFEC4899)
    val CheckRingInner = Color(0xFF1E3A5F)
    val CheckBg        = Color(0xFFDBEAFE)
}

object RegistrationSpacing {
    val xs  = 4.dp
    val sm  = 8.dp
    val md  = 16.dp
    val lg  = 24.dp
    val xl  = 32.dp
    val xxl = 48.dp
    val screenHorizontal = 24.dp
}

object RegistrationShapes {
    val fieldRadius  = 12.dp
    val buttonRadius = 14.dp
    val stepSize     = 44.dp
    val checkSize    = 160.dp
    val checkInner   = 130.dp
    val buttonHeight = 54.dp
}

private val LightColorScheme = lightColorScheme(
    primary   = AppColors.Purple,
    onPrimary = Color.White,
    secondary = AppColors.PurpleLight,
    onSecondary = AppColors.PurpleDark,
    background = AppColors.Background,
    surface   = AppColors.Surface,
    error     = AppColors.Error,
    onSurface = AppColors.TextPrimary,
    onSurfaceVariant = AppColors.TextSecondary
)

@Composable
fun TemanBelajarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
