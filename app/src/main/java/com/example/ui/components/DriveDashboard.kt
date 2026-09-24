package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val DriveInk = Color(0xFF112D29)
private val DriveLime = Color(0xFFD9F777)
private val DriveMuted = Color(0xFFB8CFC5)

/** Overview uses recorded values; it does not claim live pump status or remaining fuel. */
@Composable
fun DriveDashboard(
    driverName: String,
    vehicleName: String?,
    registration: String?,
    mileage: Double?,
    monthSpend: Double,
    todaySpend: Double,
    monthKg: Double,
    refillCount: Int,
    onAddRefill: () -> Unit,
    onFindPump: () -> Unit,
    onVehicle: () -> Unit,
    onReports: () -> Unit
) {
    val money = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column {
            Text("YOUR DRIVE, SIMPLIFIED", color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(6.dp))
            Text("Chalo, ${driverName.trim().substringBefore(' ').ifBlank { "driver" }}.",
                color = MaterialTheme.colorScheme.onSurface, fontSize = 29.sp,
                fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("Every refill. Every rupee. In one place.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(DriveInk, Color(0xFF21483C))))) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = .09f)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.DirectionsCar, null, tint = DriveLime)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(vehicleName ?: "Your garage starts here", color = Color.White,
                            fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                        Text(registration?.takeIf { it.isNotBlank() } ?: "Add your CNG vehicle",
                            color = DriveMuted, fontSize = 12.sp)
                    }
                    IconButton(onClick = onVehicle, modifier = Modifier.testTag("drive_vehicle")) {
                        Icon(Icons.Default.ArrowForward, "Open vehicles", tint = DriveLime)
                    }
                }
                Spacer(Modifier.height(26.dp))
                Text("YOUR AVERAGE MILEAGE", color = DriveMuted,
                    letterSpacing = 1.4.sp, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(mileage?.let { String.format(Locale.US, "%.1f", it) } ?: "—",
                        color = DriveLime, fontSize = 62.sp, fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-3).sp, modifier = Modifier.testTag("drive_mileage"))
                    Text("km/kg", color = DriveMuted, fontSize = 15.sp,
                        modifier = Modifier.padding(start = 10.dp, bottom = 13.dp))
                }
                Text(if (mileage == null) "Add refill readings to see your mileage" else "Calculated from your recorded refills",
                    color = DriveMuted, fontSize = 12.sp)
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = Color.White.copy(alpha = .12f))
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("TODAY'S SPEND", color = DriveMuted, fontSize = 10.sp, letterSpacing = .8.sp)
                        Text(money.format(todaySpend), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("REFILLS THIS MONTH", color = DriveMuted, fontSize = 10.sp, letterSpacing = .8.sp)
                        Text(refillCount.toString().padStart(2, '0'), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onAddRefill, modifier = Modifier.weight(1f).heightIn(min = 58.dp).testTag("drive_add_refill"),
                shape = RoundedCornerShape(18.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DriveLime, contentColor = DriveInk)) {
                Icon(Icons.Default.Add, null, Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add refill", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            OutlinedButton(onClick = onFindPump,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface), modifier = Modifier.weight(1f).heightIn(min = 58.dp).testTag("drive_find_pump"),
                shape = RoundedCornerShape(18.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp)) {
                Icon(Icons.Default.LocalGasStation, null, Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Find pump", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("This month", color = MaterialTheme.colorScheme.onSurface, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onReports, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) { Text("View report →", fontWeight = FontWeight.SemiBold) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)) {
                Column(Modifier.padding(18.dp)) {
                    Text("Fuel spend", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(money.format(monthSpend), color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    Text("From saved refills", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
            }
            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)) {
                Column(Modifier.padding(18.dp)) {
                    Text("CNG filled", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(String.format(Locale.US, "%.1f", monthKg), color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    Text("kilograms this month", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
            }
        }
    }
}
