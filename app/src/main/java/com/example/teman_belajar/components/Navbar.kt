package com.example.teman_belajar.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teman_belajar.theme.AppColors

@Composable
fun Navbar(
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    NavigationBar(
        // Tinggi disesuaikan ke 64dp (pendek tapi tetap proporsional)
        modifier = Modifier.height(64.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        // Menghilangkan insets bawaan agar konten tidak terdorong ke atas/terpotong
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        val navItems = listOf(
            Triple("home", "Home", Icons.Default.Home),
            Triple("quiz", "Quiz", Icons.Outlined.Quiz),
            Triple("profile", "Profile", Icons.Default.Person)
        )

        navItems.forEach { (route, label, icon) ->
            val isSelected = currentRoute == route
            
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(route) },
                icon = { 
                    Icon(
                        imageVector = icon, 
                        contentDescription = label,
                        // Menurunkan posisi ikon dengan padding top dan ukuran yang pas
                        modifier = Modifier
                            .padding(top = 4.dp) 
                            .size(22.dp)
                    ) 
                },
                label = { 
                    Text(
                        text = label, 
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        // Memberikan sedikit jarak agar label tidak terlalu mepet bawah
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.Purple,
                    selectedTextColor = AppColors.Purple,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = AppColors.Purple.copy(alpha = 0.1f)
                )
            )
        }
    }
}
