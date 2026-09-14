package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Car
import com.example.data.model.Pump
import com.example.ui.components.AppTourDialog
import com.example.ui.components.AutomotiveClusterCard
import com.example.ui.components.AutomotiveSavingsCard
import com.example.ui.components.CircularMileageGauge
import com.example.ui.components.CngStatusCard
import com.example.ui.components.DriverQuickActionsGrid
import com.example.ui.components.DriverRefillBottomSheet
import com.example.ui.components.GeminiCngAdvisorSheet
import com.example.ui.components.HomeRecentRefillsSection
import com.example.ui.components.HomeStatisticsGrid
import com.example.ui.components.LanguageSelectorDialog
import com.example.ui.components.NearestStationDistanceCard
import com.example.ui.components.WeeklyExpensesBarChart
import com.example.ui.theme.DarkTeal
import com.example.ui.theme.EmeraldGreen
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.PumpViewModel
import com.example.ui.viewmodel.RefillViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.util.AppLocalization
import com.example.util.CsvExportUtil
import com.example.util.LocationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    pumpViewModel: PumpViewModel,
    refillViewModel: RefillViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToPumps: () -> Unit,
    onNavigateToAddRefill: () -> Unit,
    onNavigateToCars: () -> Unit,
    onNavigateToReports: () -> Unit,
    onSelectPump: (Pump) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    val state by homeViewModel.uiState.collectAsState()
    val pumpItems by pumpViewModel.pumpsList.collectAsState()
    val carsList by refillViewModel.carsList.collectAsState()
    val refillsList by refillViewModel.refillsList.collectAsState()
    val pumpsList by refillViewModel.pumpsList.collectAsState()
    val settingsState by settingsViewModel.settingsState.collectAsState()

    val strings = remember(settingsState.selectedLanguage) {
        AppLocalization.getStrings(settingsState.selectedLanguage)
    }

    val appPreferences = remember { com.example.util.AppPreferences(context) }
    var showQuickRefillBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showGeminiAdvisorSheet by rememberSaveable { mutableStateOf(false) }
    var showAppTourDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showVehicleSelectDialog by rememberSaveable { mutableStateOf(false) }

    // FAB expands when at top, shrinks to icon on scroll
    val isFabExpanded by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }

    // Calculate today's fuel expense
    val startOfToday = remember {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    val todayRefills = remember(refillsList, startOfToday) {
        refillsList.filter { it.date >= startOfToday }
    }
    val todayExpense = todayRefills.sumOf { it.totalAmount }
    val todayKg = todayRefills.sumOf { it.quantityKg }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                val recordedMileage = refillsList.filter { it.carId == state.activeCar?.id && it.mileageKmPerKg > 0 }
                    .map { it.mileageKmPerKg }.takeIf { it.isNotEmpty() }?.average()
                com.example.ui.components.DriveDashboard(
                    driverName = if (settingsState.isLoggedIn) settingsState.userName else "driver",
                    vehicleName = state.activeCar?.name,
                    registration = state.activeCar?.regNumber,
                    mileage = recordedMileage,
                    monthSpend = state.currentMonthExpense,
                    todaySpend = todayExpense,
                    monthKg = state.currentMonthCngKg,
                    refillCount = state.currentMonthRefillsCount,
                    onAddRefill = { if (carsList.isEmpty()) onNavigateToCars() else showQuickRefillBottomSheet = true },
                    onFindPump = onNavigateToPumps,
                    onVehicle = { if (carsList.isEmpty()) onNavigateToCars() else showVehicleSelectDialog = true },
                    onReports = onNavigateToReports
                )
            }
            item { WeeklyExpensesBarChart(refills = refillsList) }
            item {
                HomeRecentRefillsSection(
                    recentRefills = refillsList,
                    onViewAllClick = onNavigateToAddRefill,
                    onRefillClick = { onNavigateToAddRefill() }
                )
            }
            item {
                Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .4f)) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Make it yours", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Your language. Your vehicle. Your drive.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { showLanguageDialog = true }) { Text("Language") }
                            TextButton(onClick = onNavigateToCars) { Text("My vehicles") }
                            TextButton(onClick = { showAppTourDialog = true }) { Text("App guide") }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }

        // Extended Floating Action Button (FAB) that says "Log CNG" and shrinks to an icon on scroll
        ExtendedFloatingActionButton(
            expanded = isFabExpanded,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showQuickRefillBottomSheet = true
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = "Log CNG",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            },
            text = {
                Text(
                    text = "Log CNG",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("quick_refill_fab")
        )

        // Swipe-Up ModalBottomSheet for Quick Refill with Custom NumPad & Haptic Feedback
        if (showQuickRefillBottomSheet) {
            DriverRefillBottomSheet(
                cars = carsList,
                pumps = pumpsList,
                existingRefill = null,
                lastOdometerReading = state.activeCar?.currentOdometer ?: 0.0,
                onDismiss = { showQuickRefillBottomSheet = false },
                onSave = { id, carId, pumpId, pumpName, odo, qty, price, total, isFull, notes ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    refillViewModel.saveRefill(id, carId, pumpId, pumpName, odo, qty, price, total, isFull, notes)
                    showQuickRefillBottomSheet = false
                }
            )
        }

        // Gemini AI CNG Advisor & Mileage Optimization Sheet
        if (showGeminiAdvisorSheet) {
            GeminiCngAdvisorSheet(
                activeCar = state.activeCar,
                refills = refillsList,
                onDismiss = { showGeminiAdvisorSheet = false }
            )
        }

        // App Tour Dialog
        if (showAppTourDialog) {
            AppTourDialog(
                onDismiss = {
                    showAppTourDialog = false
                    appPreferences.isAppTourCompleted = true
                }
            )
        }

        // Language Dialog
        if (showLanguageDialog) {
            LanguageSelectorDialog(
                currentLanguage = settingsState.selectedLanguage,
                onLanguageSelected = { lang ->
                    settingsViewModel.setAppLanguage(lang)
                },
                onDismiss = { showLanguageDialog = false }
            )
        }

        // Vehicle Switch Dialog
        if (showVehicleSelectDialog) {
            AlertDialog(
                onDismissRequest = { showVehicleSelectDialog = false },
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Select Active Vehicle",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        carsList.forEach { car ->
                            val isSelected = state.activeCar?.id == car.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        homeViewModel.switchActiveCar(car.id)
                                        showVehicleSelectDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            homeViewModel.switchActiveCar(car.id)
                                            showVehicleSelectDialog = false
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = car.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${car.regNumber} • ${car.tankCapacityKg} kg Tank",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showVehicleSelectDialog = false
                            onNavigateToCars()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Manage Garage", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showVehicleSelectDialog = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}
