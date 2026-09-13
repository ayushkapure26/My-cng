package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Pumps : Screen("pumps", "Pumps", Icons.Default.LocalGasStation)
    object Refills : Screen("refills", "My Refills", Icons.Default.ReceiptLong)
    object More : Screen("more", "More", Icons.Default.MoreHoriz)
    object Cars : Screen("cars", "Vehicles", Icons.Default.DirectionsCar)
    object Reports : Screen("reports", "Analytics", Icons.Default.Analytics)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Auth : Screen("auth", "Sign In", Icons.Default.Person)
}

val bottomNavScreens = listOf(
    Screen.Pumps,
    Screen.Refills,
    Screen.More
)
