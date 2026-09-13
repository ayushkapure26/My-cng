package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GeminiCngAdvisorSheet
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DarkTeal
import com.example.ui.theme.EmeraldGreen
import com.example.ui.viewmodel.SettingsViewModel
import com.example.util.AppLocalization
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MoreScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateToCars: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onShowLanguageDialog: () -> Unit
) {
    val context = LocalContext.current
    val settingsState by settingsViewModel.settingsState.collectAsState()
    val strings = remember(settingsState.selectedLanguage) {
        AppLocalization.getStrings(settingsState.selectedLanguage)
    }

    var showSavingsCalcModal by rememberSaveable { mutableStateOf(false) }
    var showHydroTestModal by rememberSaveable { mutableStateOf(false) }
    var showSafetyTipsModal by rememberSaveable { mutableStateOf(false) }
    var showGeminiAdvisorSheet by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "More Options & Services",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Vehicle management, compliance, savings & guides",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Section: Vehicles & Analytics
        item {
            MoreSectionTitle("VEHICLE & ANALYTICS")
        }

        item {
            MoreNavigationCard(
                title = "My Vehicles",
                subtitle = "Manage car models, CNG tank capacity & default vehicle",
                icon = Icons.Default.DirectionsCar,
                iconTint = DarkTeal,
                testTag = "more_btn_vehicles",
                onClick = onNavigateToCars
            )
        }

        item {
            MoreNavigationCard(
                title = "Expense & Fuel Analytics",
                subtitle = "Monthly cost breakdown, total kg filled & petrol comparison",
                icon = Icons.Default.Analytics,
                iconTint = EmeraldGreen,
                testTag = "more_btn_reports",
                onClick = onNavigateToReports
            )
        }

        // Section: Practical Calculators & Compliance
        item {
            MoreSectionTitle("CALCULATORS & COMPLIANCE")
        }

        item {
            MoreNavigationCard(
                title = "CNG vs Petrol Savings Calculator",
                subtitle = "Calculate your exact monthly and annual savings with CNG",
                icon = Icons.Default.Calculate,
                iconTint = DarkTeal,
                testTag = "more_btn_savings_calc",
                onClick = { showSavingsCalcModal = true }
            )
        }

        item {
            MoreNavigationCard(
                title = "Cylinder Hydro-Test Compliance",
                subtitle = "PESO 3-year safety reminder & test certificate tracker",
                icon = Icons.Default.VerifiedUser,
                iconTint = Color(0xFFE65100),
                testTag = "more_btn_hydro_test",
                onClick = { showHydroTestModal = true }
            )
        }

        // Section: Guides & Safety
        item {
            MoreSectionTitle("SAFETY & DRIVER ADVISOR")
        }

        item {
            MoreNavigationCard(
                title = "CNG Maintenance & Safety Guides",
                subtitle = "Pressure optimization, spark plug tuning & leak precautions",
                icon = Icons.Default.Lightbulb,
                iconTint = AmberAccent,
                testTag = "more_btn_safety_guides",
                onClick = { showSafetyTipsModal = true }
            )
        }

        item {
            MoreNavigationCard(
                title = "AI Mileage & Diagnostics Advisor",
                subtitle = "Interactive vehicle health advice powered by Gemini AI",
                icon = Icons.Default.Speed,
                iconTint = EmeraldGreen,
                testTag = "more_btn_gemini_advisor",
                onClick = { showGeminiAdvisorSheet = true }
            )
        }

        // Section: Preferences & Cloud
        item {
            MoreSectionTitle("PREFERENCES & BACKUP")
        }

        item {
            MoreNavigationCard(
                title = "App Language",
                subtitle = "Current: ${settingsState.selectedLanguage} (9 regional languages supported)",
                icon = Icons.Default.Translate,
                iconTint = DarkTeal,
                testTag = "more_btn_language",
                onClick = onShowLanguageDialog
            )
        }

        item {
            MoreNavigationCard(
                title = "Driver Profile & App Settings",
                subtitle = "Freshness threshold, monthly budget & system preferences",
                icon = Icons.Default.Settings,
                iconTint = MaterialTheme.colorScheme.primary,
                testTag = "more_btn_settings",
                onClick = onNavigateToSettings
            )
        }

        item {
            MoreNavigationCard(
                title = if (settingsState.isLoggedIn) "Cloud Sync: Connected" else "Cloud Backup & Sign In",
                subtitle = if (settingsState.isLoggedIn) "Logged in as ${settingsState.userEmail}" else "Sign in to backup refills and vehicles across devices",
                icon = Icons.Default.Person,
                iconTint = if (settingsState.isLoggedIn) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                testTag = "more_btn_auth",
                onClick = onNavigateToAuth
            )
        }

        // Policy & Disclaimers
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CNG Mitra prioritizes genuine community reports and PESO safety standards. In India, public CNG stations do not offer advance slot booking; arrival queues are managed on a first-come, first-served basis.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }

    // Modal: CNG Savings Calculator
    if (showSavingsCalcModal) {
        CngSavingsCalculatorDialog(
            onDismiss = { showSavingsCalcModal = false }
        )
    }

    // Modal: Cylinder Hydro-Test Reminder
    if (showHydroTestModal) {
        HydroTestComplianceDialog(
            onDismiss = { showHydroTestModal = false }
        )
    }

    // Modal: CNG Maintenance & Safety Guides
    if (showSafetyTipsModal) {
        CngSafetyGuidesDialog(
            onDismiss = { showSafetyTipsModal = false },
            onOpenAiAdvisor = {
                showSafetyTipsModal = false
                showGeminiAdvisorSheet = true
            }
        )
    }

    // Sheet: Gemini Advisor
    if (showGeminiAdvisorSheet) {
        GeminiCngAdvisorSheet(
            activeCar = null,
            refills = emptyList(),
            onDismiss = { showGeminiAdvisorSheet = false }
        )
    }
}

@Composable
fun MoreSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
fun MoreNavigationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Consolidated, practical CNG vs Petrol Savings Calculator.
 */
@Composable
fun CngSavingsCalculatorDialog(onDismiss: () -> Unit) {
    var monthlyKmText by remember { mutableStateOf("1200") }
    var petrolPriceText by remember { mutableStateOf("96.50") }
    var cngPriceText by remember { mutableStateOf("75.50") }
    var petrolMileageText by remember { mutableStateOf("15.0") }
    var cngMileageText by remember { mutableStateOf("24.0") }

    val monthlyKm = monthlyKmText.toDoubleOrNull() ?: 0.0
    val petrolPrice = petrolPriceText.toDoubleOrNull() ?: 0.0
    val cngPrice = cngPriceText.toDoubleOrNull() ?: 0.0
    val petrolMileage = petrolMileageText.toDoubleOrNull() ?: 1.0
    val cngMileage = cngMileageText.toDoubleOrNull() ?: 1.0

    val petrolCostMonthly = if (petrolMileage > 0) (monthlyKm / petrolMileage) * petrolPrice else 0.0
    val cngCostMonthly = if (cngMileage > 0) (monthlyKm / cngMileage) * cngPrice else 0.0
    val monthlySavings = (petrolCostMonthly - cngCostMonthly).coerceAtLeast(0.0)
    val annualSavings = monthlySavings * 12

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = DarkTeal)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CNG Savings Calculator", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Compare actual running costs between Petrol and CNG based on your commute.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = monthlyKmText,
                    onValueChange = { monthlyKmText = it },
                    label = { Text("Monthly Running (km)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = petrolPriceText,
                        onValueChange = { petrolPriceText = it },
                        label = { Text("Petrol ₹/L") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cngPriceText,
                        onValueChange = { cngPriceText = it },
                        label = { Text("CNG ₹/kg") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = petrolMileageText,
                        onValueChange = { petrolMileageText = it },
                        label = { Text("Petrol km/L") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cngMileageText,
                        onValueChange = { cngMileageText = it },
                        label = { Text("CNG km/kg") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Result Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Estimated Monthly Savings",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${monthlySavings.toInt()} / month",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Annual Savings: ₹${annualSavings.toInt()} / year",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTeal
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DarkTeal)
            ) {
                Text("Done", color = Color.White)
            }
        }
    )
}

/**
 * PESO India Cylinder Hydro-Testing Compliance Tracker.
 */
@Composable
fun HydroTestComplianceDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var lastTestDateYear by remember { mutableStateOf("2023") }
    var lastTestDateMonth by remember { mutableStateOf("6") }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val testYear = lastTestDateYear.toIntOrNull() ?: (currentYear - 2)
    val nextDueYear = testYear + 3

    val isExpired = currentYear > nextDueYear
    val isDueSoon = currentYear == nextDueYear

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = if (isExpired) Color(0xFFD32F2F) else if (isDueSoon) Color(0xFFF57C00) else EmeraldGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("CNG Cylinder Hydro-Test", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isExpired) Color(0xFFFFEBEE) else if (isDueSoon) Color(0xFFFFF8E1) else Color(0xFFE8F5E9),
                    border = BorderStroke(1.dp, if (isExpired) Color(0xFFD32F2F) else if (isDueSoon) Color(0xFFF57C00) else EmeraldGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = when {
                                isExpired -> "⚠️ CYLINDER HYDRO-TEST OVERDUE"
                                isDueSoon -> "⏳ RE-TESTING DUE THIS YEAR"
                                else -> "✅ COMPLIANT (PESO CERTIFIED)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isExpired) Color(0xFFD32F2F) else if (isDueSoon) Color(0xFFF57C00) else Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Next hydrostatic inspection due by: Dec $nextDueYear (valid 3 years per PESO mandate).",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Update Last Hydro-Test Date:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = lastTestDateMonth,
                        onValueChange = { if (it.length <= 2) lastTestDateMonth = it },
                        label = { Text("Month (1-12)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lastTestDateYear,
                        onValueChange = { if (it.length <= 4) lastTestDateYear = it },
                        label = { Text("Year (e.g. 2023)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Rule 35 of Gas Cylinder Rules 2016:\nEvery CNG cylinder mounted in an automotive vehicle must be hydro-tested at high pressure (300 Bar) once every 3 years by a PESO-authorized test agency. Always carry your plate/compliance certificate in the car.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Toast.makeText(context, "Hydro-test record saved successfully", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkTeal)
            ) {
                Text("Save Compliance Date", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Driver practical safety and mileage maintenance guides.
 */
@Composable
fun CngSafetyGuidesDialog(
    onDismiss: () -> Unit,
    onOpenAiAdvisor: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.HealthAndSafety, contentDescription = null, tint = EmeraldGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CNG Driver Safety & Tips", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    SafetyGuideItem(
                        title = "1. Best Filling Time: Early Morning",
                        description = "Ambient temperature is lowest in the morning, meaning CNG gas is denser. Filling at 200+ Bar gets you 10-15% extra gas in the cylinder."
                    )
                }
                item {
                    SafetyGuideItem(
                        title = "2. Spark Plug Maintenance (Every 15,000 km)",
                        description = "CNG requires a stronger electrical spark than petrol. Keep spark plug electrode gaps trimmed to 0.7-0.8 mm or upgrade to Iridium plugs for smooth acceleration."
                    )
                }
                item {
                    SafetyGuideItem(
                        title = "3. Mandated Refilling Safety Protocol",
                        description = "Turn off engine, switch off ignition, step out of vehicle, and remove mobile phones during high-pressure filling per PESO safety rules."
                    )
                }
                item {
                    SafetyGuideItem(
                        title = "4. Gas Odor / Leak Precautions",
                        description = "CNG is infused with ethyl mercaptan (pungent smell). If detected, switch off the manual cylinder valve, park in an open area, and do not crank the starter."
                    )
                }
                item {
                    SafetyGuideItem(
                        title = "5. Air Filter Cleaning",
                        description = "CNG engines need a precise stoichiometric air-fuel ratio (17.2:1). A dusty air filter severely degrades pickup and fuel mileage."
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenAiAdvisor,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("Ask AI Advisor", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SafetyGuideItem(title: String, description: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
        }
    }
}
