package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.model.CachedSearch
import com.example.data.model.Pump
import com.example.ui.components.CngGoogleMapView
import com.example.ui.components.CngMapCanvas
import com.example.ui.components.ReportLiveStatusModal
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DarkTeal
import com.example.ui.theme.DeepForest
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LightEmerald
import com.example.ui.theme.NeonLime
import com.example.ui.theme.NeonLimeDark
import com.example.ui.theme.NeonRouteLine
import com.example.ui.viewmodel.PumpDisplayItem
import com.example.ui.viewmodel.PumpSortOption
import com.example.ui.viewmodel.PumpViewModel
import com.example.ui.viewmodel.RefillViewModel
import com.example.ui.viewmodel.SuggestionType
import com.example.util.AppLocalization
import com.example.util.LocalizedStrings
import com.example.util.LocationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PumpFinderScreen(
    pumpViewModel: PumpViewModel,
    refillViewModel: RefillViewModel? = null,
    selectedLanguage: String = "English",
    onRequestLocationPermission: () -> Unit = {},
    onSelectPump: (Pump) -> Unit,
    onNavigateToRefills: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val strings = remember(selectedLanguage) { AppLocalization.getStrings(selectedLanguage) }
    val filterState by pumpViewModel.filterState.collectAsState()
    val pumpsList by pumpViewModel.pumpsList.collectAsState()
    val suggestions by pumpViewModel.searchSuggestions.collectAsState()
    val cachedSearches by pumpViewModel.cachedSearches.collectAsState()

    var showMapToggle by rememberSaveable { mutableStateOf(false) }
    var showCityDropdown by rememberSaveable { mutableStateOf(false) }
    var showSortMenu by rememberSaveable { mutableStateOf(false) }
    var showSuggestions by rememberSaveable { mutableStateOf(true) }
    var showOfflineInfoDialog by rememberSaveable { mutableStateOf(false) }
    var pumpToReport by remember { mutableStateOf<Pump?>(null) }

    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val cacheDateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val isGpsActive = filterState.isGpsGranted && filterState.userLocation.isGpsBased
    val isLocationAvailable = isGpsActive

    val selectClosestDistance: () -> Unit = {
        val locService = com.example.util.LocationService.getInstance(context)
        if (isLocationAvailable) {
            pumpViewModel.setSortOption(PumpSortOption.DISTANCE_LOW)
        } else if (locService.hasPermission()) {
            Toast.makeText(context, "Acquiring live GPS location...", Toast.LENGTH_SHORT).show()
            LocationHelper.fetchCurrentLocation(
                context = context,
                onSuccess = { loc ->
                    pumpViewModel.setGpsLocation(loc.latitude, loc.longitude)
                    pumpViewModel.setSortOption(PumpSortOption.DISTANCE_LOW)
                    Toast.makeText(context, "Location acquired: sorted by closest distance", Toast.LENGTH_SHORT).show()
                },
                onFailure = {
                    pumpViewModel.setGpsDenied()
                    Toast.makeText(context, "GPS location unavailable. Please check device settings.", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            onRequestLocationPermission()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Compact Header: City Selector + Explicit "Use my location" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // City Picker Chip
            Box {
                Surface(
                    onClick = { showCityDropdown = true },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${filterState.selectedCity} ▾",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                DropdownMenu(
                    expanded = showCityDropdown,
                    onDismissRequest = { showCityDropdown = false }
                ) {
                    LocationHelper.INDIAN_CITIES.forEach { cityLoc ->
                        DropdownMenuItem(
                            text = { Text(cityLoc.cityName) },
                            onClick = {
                                pumpViewModel.selectCity(cityLoc)
                                showCityDropdown = false
                            }
                        )
                    }
                }
            }

            // Explicit "Use my location" button (does not trigger GPS on startup)
            Surface(
                onClick = {
                    val locService = com.example.util.LocationService.getInstance(context)
                    if (locService.hasPermission()) {
                        LocationHelper.fetchCurrentLocation(
                            context = context,
                            onSuccess = { loc ->
                                pumpViewModel.setGpsLocation(loc.latitude, loc.longitude)
                            },
                            onFailure = {
                                pumpViewModel.setGpsDenied()
                            }
                        )
                    } else {
                        onRequestLocationPermission()
                    }
                },
                shape = RoundedCornerShape(20.dp),
                color = if (isGpsActive) NeonLime.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                    1.dp,
                    if (isGpsActive) NeonLime else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.testTag("btn_gps_quick_locate")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Use my location",
                        tint = if (isGpsActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGpsActive) "GPS Active" else "Use my location",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large search field: “City ya pump khojo”
        Box(modifier = Modifier.fillMaxWidth().zIndex(10f)) {
            Column {
                OutlinedTextField(
                    value = filterState.searchQuery,
                    onValueChange = {
                        pumpViewModel.updateSearchQuery(it)
                        showSuggestions = true
                    },
                    placeholder = {
                        Text("City ya pump khojo", fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = if (filterState.searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = {
                                pumpViewModel.clearSearchQuery()
                                showSuggestions = false
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Search",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pump_search_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                // Auto-complete Suggestions Dropdown
                if (showSuggestions && suggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("search_autocomplete_card"),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            suggestions.forEachIndexed { index, suggestion ->
                                if (index > 0) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            pumpViewModel.applySuggestion(suggestion)
                                            showSuggestions = false
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val (icon, iconTint) = when (suggestion.type) {
                                        SuggestionType.PUMP_NAME -> Icons.Default.LocalGasStation to EmeraldGreen
                                        SuggestionType.AREA_LOCATION -> Icons.Default.LocationOn to DarkTeal
                                        SuggestionType.CITY_REGION -> Icons.Default.Explore to DarkTeal
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconTint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = suggestion.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = suggestion.subtitle,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.NorthWest,
                                        contentDescription = "Select suggestion",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // User Current GPS Location Status & Map Quick Launcher Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("user_location_status_bar"),
            shape = RoundedCornerShape(12.dp),
            color = if (isGpsActive) EmeraldGreen.copy(alpha = 0.12f) else DarkTeal.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, if (isGpsActive) EmeraldGreen.copy(alpha = 0.3f) else DarkTeal.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isGpsActive) Icons.Default.MyLocation else Icons.Default.LocationOn,
                        contentDescription = "Current Location",
                        tint = if (isGpsActive) EmeraldGreen else DarkTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (isGpsActive) "Live GPS Location" else "Selected Location: ${filterState.selectedCity}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.4f", filterState.userLocation.latitude)}° N, ${String.format(Locale.US, "%.4f", filterState.userLocation.longitude)}° E",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isGpsActive) {
                        OutlinedButton(
                            onClick = {
                                val locService = com.example.util.LocationService.getInstance(context)
                                if (locService.hasPermission()) {
                                    LocationHelper.fetchCurrentLocation(
                                        context = context,
                                        onSuccess = { loc ->
                                            pumpViewModel.setGpsLocation(loc.latitude, loc.longitude)
                                        },
                                        onFailure = {
                                            pumpViewModel.setGpsDenied()
                                        }
                                    )
                                } else {
                                    onRequestLocationPermission()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = EmeraldGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Locate Me", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkTeal)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Quick Map Action
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (showMapToggle) DarkTeal else EmeraldGreen,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showMapToggle = !showMapToggle }
                            .testTag("quick_toggle_map_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showMapToggle) Icons.Default.ViewList else Icons.Default.Map,
                                contentDescription = "Toggle Google Map",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showMapToggle) "List" else "Map",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Offline Map Search Caching Banner & Recent Searches
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (filterState.isOfflineMode) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (filterState.isOfflineMode) Icons.Default.WifiOff else Icons.Default.OfflinePin,
                            contentDescription = null,
                            tint = if (filterState.isOfflineMode) Color(0xFF2E7D32) else DarkTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.offlineCacheActive,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (filterState.isOfflineMode) Color(0xFF1B5E20) else DarkTeal
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = strings.offlineModeToggle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = filterState.isOfflineMode,
                            onCheckedChange = { pumpViewModel.toggleOfflineMode() },
                            modifier = Modifier.size(width = 36.dp, height = 24.dp),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldGreen
                            )
                        )
                    }
                }

                // If an active cache notification is set
                if (filterState.activeCachedSearchInfo != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "💾 ${filterState.activeCachedSearchInfo}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                }

                // Recent Cached Search Pills
                if (cachedSearches.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.cachedSearchesTitle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(cachedSearches) { cached ->
                            val isCurrent = filterState.searchQuery.equals(cached.query, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isCurrent) EmeraldGreen else Color.White,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        pumpViewModel.applyCachedSearch(cached)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.OfflinePin,
                                        contentDescription = null,
                                        tint = if (isCurrent) Color.White else EmeraldGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${cached.query} (${cached.resultCount})",
                                        fontSize = 11.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Bar (Trip Mode Corridors, Stock Status, Open Now, Nearby, Highest Rated, Lowest Price)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("filter_chips_row"),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Margdarshak Highway Corridor / Trip Mode Chip
            item {
                FilterChip(
                    selected = filterState.isTripModeActive,
                    onClick = { pumpViewModel.toggleTripMode() },
                    label = { 
                        Text(
                            text = if (filterState.isTripModeActive) "🛣️ ${filterState.selectedHighwayCorridor.split("-").first().trim()}" else "🛣️ Highway Corridors",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.testTag("chip_trip_mode"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DarkTeal,
                        selectedLabelColor = Color.White
                    )
                )
            }

            // In-Stock / Available Filter Chip (Tri-color Green & Neon Street styling)
            item {
                val isSelected = filterState.availableOnly || filterState.stockStatusFilter == "AVAILABLE" || filterState.gasAvailableOnly
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        pumpViewModel.toggleAvailableOnly()
                    },
                    label = { 
                        Text(
                            text = if (isSelected) "🟢 Available Only" else "Available Only",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ) 
                    },
                    modifier = Modifier.testTag("chip_stock_available"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonLime.copy(alpha = 0.22f),
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) NeonLime else MaterialTheme.colorScheme.outlineVariant
                    ),
                    leadingIcon = if (isSelected) {
                        { 
                            Icon(
                                imageVector = Icons.Default.Check, 
                                contentDescription = null, 
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            ) 
                        }
                    } else null
                )
            }

            item {
                FilterChip(
                    selected = filterState.openNowOnly,
                    onClick = { pumpViewModel.toggleOpenNow() },
                    label = { Text(strings.openNow, fontSize = 12.sp) },
                    modifier = Modifier.testTag("chip_open_now"),
                    leadingIcon = if (filterState.openNowOnly) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null
                )
            }

            item {
                val isClosestSelected = filterState.sortOption == PumpSortOption.DISTANCE_LOW
                FilterChip(
                    selected = isClosestSelected,
                    onClick = { selectClosestDistance() },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isLocationAvailable) strings.closestDistance else "${strings.closestDistance} (GPS)",
                                fontSize = 12.sp,
                                fontWeight = if (isClosestSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            if (isLocationAvailable && isClosestSelected) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = EmeraldGreen,
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .testTag("chip_closest_distance")
                        .testTag("chip_nearby"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonLime.copy(alpha = 0.22f),
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isClosestSelected) NeonLime else MaterialTheme.colorScheme.outlineVariant
                    ),
                    leadingIcon = if (isClosestSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary) }
                    } else {
                        { Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (isLocationAvailable) NeonLimeDark else MaterialTheme.colorScheme.outline) }
                    }
                )
            }

            item {
                FilterChip(
                    selected = filterState.sortOption == PumpSortOption.RATING_HIGH,
                    onClick = {
                        if (filterState.sortOption == PumpSortOption.RATING_HIGH) {
                            pumpViewModel.setSortOption(PumpSortOption.DISTANCE_LOW)
                        } else {
                            pumpViewModel.setSortOption(PumpSortOption.RATING_HIGH)
                        }
                    },
                    label = { Text(strings.highestRated, fontSize = 12.sp) },
                    modifier = Modifier.testTag("chip_highest_rated"),
                    leadingIcon = if (filterState.sortOption == PumpSortOption.RATING_HIGH) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null
                )
            }

            item {
                FilterChip(
                    selected = filterState.sortOption == PumpSortOption.PRICE_LOW,
                    onClick = {
                        if (filterState.sortOption == PumpSortOption.PRICE_LOW) {
                            pumpViewModel.setSortOption(PumpSortOption.DISTANCE_LOW)
                        } else {
                            pumpViewModel.setSortOption(PumpSortOption.PRICE_LOW)
                        }
                    },
                    label = { Text(strings.lowestPrice, fontSize = 12.sp) },
                    modifier = Modifier.testTag("chip_lowest_price")
                )
            }

            item {
                val isSelected = filterState.availableOnly || filterState.gasAvailableOnly
                FilterChip(
                    selected = isSelected,
                    onClick = { pumpViewModel.toggleAvailableOnly() },
                    label = { Text(strings.gasAvailable, fontSize = 12.sp) },
                    modifier = Modifier.testTag("chip_gas_available"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonLime.copy(alpha = 0.22f),
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) NeonLime else MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            item {
                FilterChip(
                    selected = filterState.pressureAvailableOnly,
                    onClick = { pumpViewModel.togglePressureAvailable() },
                    label = { Text(strings.highPressure, fontSize = 12.sp) },
                    modifier = Modifier.testTag("chip_high_pressure")
                )
            }
        }

        // Highway Corridor Selection Carousel when Trip Mode is active
        AnimatedVisibility(visible = filterState.isTripModeActive) {
            Column(modifier = Modifier.padding(top = 6.dp)) {
                Text(
                    text = "Margdarshak Trip Corridors:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTeal
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PumpViewModel.POPULAR_HIGHWAY_CORRIDORS) { corridor ->
                        val isCorridorActive = filterState.selectedHighwayCorridor.equals(corridor, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCorridorActive) DarkTeal else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { pumpViewModel.setHighwayCorridor(corridor) }
                        ) {
                            Text(
                                text = corridor,
                                fontSize = 11.sp,
                                fontWeight = if (isCorridorActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCorridorActive) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val isAvailableOnlyActive = filterState.availableOnly || filterState.stockStatusFilter == "AVAILABLE" || filterState.gasAvailableOnly

        // Neon Street "Available" Filter Toggle Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { pumpViewModel.toggleAvailableOnly() }
                .testTag("filter_toggle_available"),
            shape = RoundedCornerShape(14.dp),
            color = if (isAvailableOnlyActive) NeonLime.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = if (isAvailableOnlyActive) 1.5.dp else 1.dp,
                color = if (isAvailableOnlyActive) NeonLime else MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                color = if (isAvailableOnlyActive) NeonLime else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAvailableOnlyActive) Icons.Default.CheckCircle else Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = if (isAvailableOnlyActive) DeepForest else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Show 'Available' Only",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isAvailableOnlyActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = EmeraldGreen
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isAvailableOnlyActive)
                                "Filtered to ${pumpsList.size} stations with confirmed gas in stock"
                            else
                                "Tap to hide dry / out-of-stock pumps",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = isAvailableOnlyActive,
                    onCheckedChange = { pumpViewModel.toggleAvailableOnly() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DeepForest,
                        checkedTrackColor = NeonLime,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("switch_available_only")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // View Mode Toggle (List / Google Map) & Sort Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${strings.stationsFound}: ${pumpsList.size}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            // Sort Selector Dropdown
            Box {
                val isClosestSelected = filterState.sortOption == PumpSortOption.DISTANCE_LOW
                Surface(
                    onClick = { showSortMenu = true },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isClosestSelected && isLocationAvailable)
                        NeonLime.copy(alpha = 0.22f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isClosestSelected && isLocationAvailable)
                            NeonLime
                        else
                            MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .testTag("btn_sort_selector")
                        .testTag("btn_sort_dropdown")
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isClosestSelected && isLocationAvailable)
                                Icons.Default.NearMe
                            else
                                Icons.Default.Sort,
                            contentDescription = "Sort Options",
                            tint = if (isClosestSelected && isLocationAvailable)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (filterState.sortOption) {
                                PumpSortOption.DISTANCE_LOW -> if (isLocationAvailable) strings.closestDistance else "Distance"
                                PumpSortOption.RATING_HIGH -> strings.highestRated
                                PumpSortOption.PRICE_LOW -> strings.lowestPrice
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .testTag("menu_sort_options")
                ) {
                    // Option 1: Closest Distance
                    DropdownMenuItem(
                        text = {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = strings.closestDistance,
                                        fontWeight = if (isClosestSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (isLocationAvailable) {
                                        Surface(
                                            color = EmeraldGreen,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "GPS ACTIVE",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "TAP TO ENABLE GPS",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (isLocationAvailable)
                                        "Nearest stations first using live GPS coordinates"
                                    else
                                        "Requires device location • Tap to activate GPS",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = null,
                                tint = if (isLocationAvailable) NeonLimeDark else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = if (isClosestSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        onClick = {
                            showSortMenu = false
                            selectClosestDistance()
                        },
                        modifier = Modifier.testTag("menu_item_closest_distance")
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    // Option 2: Highest Rated
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = strings.highestRated,
                                    fontWeight = if (filterState.sortOption == PumpSortOption.RATING_HIGH) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Stations with best driver reviews and ratings",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = if (filterState.sortOption == PumpSortOption.RATING_HIGH) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        onClick = {
                            showSortMenu = false
                            pumpViewModel.setSortOption(PumpSortOption.RATING_HIGH)
                        },
                        modifier = Modifier.testTag("menu_item_highest_rated")
                    )

                    // Option 3: Lowest Price
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = strings.lowestPrice,
                                    fontWeight = if (filterState.sortOption == PumpSortOption.PRICE_LOW) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Lowest CNG price per kilogram first",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = if (filterState.sortOption == PumpSortOption.PRICE_LOW) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        onClick = {
                            showSortMenu = false
                            pumpViewModel.setSortOption(PumpSortOption.PRICE_LOW)
                        },
                        modifier = Modifier.testTag("menu_item_lowest_price")
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (showMapToggle) DarkTeal else DarkTeal.copy(alpha = 0.1f),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showMapToggle = !showMapToggle }
                    .testTag("toggle_map_view_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (showMapToggle) Icons.Default.ViewList else Icons.Default.Map,
                        contentDescription = "Toggle Google Map",
                        tint = if (showMapToggle) Color.White else DarkTeal,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showMapToggle) "List View" else "Google Map",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (showMapToggle) Color.White else DarkTeal
                    )
                }
            }
        }

        // Closest Distance Active Banner / GPS Guidance
        AnimatedVisibility(visible = filterState.sortOption == PumpSortOption.DISTANCE_LOW && isLocationAvailable) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = NeonLime.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, NeonLime.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = NeonLimeDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sorted by Closest Distance to your live location",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        shape = CircleShape,
                        color = EmeraldGreen
                    ) {
                        Box(modifier = Modifier.size(6.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GPS ACTIVE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = EmeraldGreen
                    )
                }
            }
        }

        AnimatedVisibility(visible = filterState.sortOption == PumpSortOption.DISTANCE_LOW && !isLocationAvailable) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { selectClosestDistance() },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationSearching,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Distance estimated from ${filterState.selectedCity} • Tap to enable GPS",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Enable GPS →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (showMapToggle) {
            Spacer(modifier = Modifier.height(8.dp))
            CngGoogleMapView(
                pumps = pumpsList,
                userLatitude = filterState.userLocation.latitude,
                userLongitude = filterState.userLocation.longitude,
                onPumpSelected = { pump -> onSelectPump(pump) },
                onNavigateClick = { pump ->
                    com.example.util.NavigationUtil.navigateToPump(
                        context = context,
                        latitude = pump.latitude,
                        longitude = pump.longitude,
                        pumpName = pump.name
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
            )
        } else {
            // "Pump First" Travel-Pass Stations List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
            ) {
                if (pumpsList.isNotEmpty()) {
                    // Prominent Nearest-Pump Feature Card
                    item(key = "nearest_pump") {
                        PumpStationCard(
                            displayItem = pumpsList.first(),
                            strings = strings,
                            dateFormat = dateFormat,
                            isFeaturedNearest = true,
                            onViewDetails = { onSelectPump(pumpsList.first().pump) },
                            onUpdateStatus = { pumpToReport = pumpsList.first().pump },
                            onNavigate = {
                                com.example.util.NavigationUtil.navigateToPump(
                                    context = context,
                                    latitude = pumpsList.first().pump.latitude,
                                    longitude = pumpsList.first().pump.longitude,
                                    pumpName = pumpsList.first().pump.name
                                )
                            }
                        )
                    }

                    if (pumpsList.size > 1) {
                        item(key = "section_header_other") {
                            Text(
                                text = "NEARBY CNG STATIONS • अन्य पंप (${pumpsList.size - 1})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }

                        items(pumpsList.drop(1), key = { it.pump.id }) { item ->
                            PumpStationCard(
                                displayItem = item,
                                strings = strings,
                                dateFormat = dateFormat,
                                isFeaturedNearest = false,
                                onViewDetails = { onSelectPump(item.pump) },
                                onUpdateStatus = { pumpToReport = item.pump },
                                onNavigate = {
                                    com.example.util.NavigationUtil.navigateToPump(
                                        context = context,
                                        latitude = item.pump.latitude,
                                        longitude = item.pump.longitude,
                                        pumpName = item.pump.name
                                    )
                                }
                            )
                        }
                    }
                } else {
                    item(key = "empty_pumps") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No CNG pumps found matching search",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try switching cities or clearing active filters",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (isAvailableOnlyActive) {
                                        Button(
                                            onClick = { pumpViewModel.setAvailableOnly(false) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = NeonLime,
                                                contentColor = DeepForest
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Show All Stations", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepForest)
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = { pumpViewModel.clearSearchQuery() },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Reset Search", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Small refill-expense summary below the pump results
                item(key = "refill_expense_summary") {
                    val refills = refillViewModel?.refillsList?.collectAsState()?.value ?: emptyList()
                    val monthlySpend = remember(refills) {
                        val cal = java.util.Calendar.getInstance()
                        val m = cal.get(java.util.Calendar.MONTH)
                        val y = cal.get(java.util.Calendar.YEAR)
                        refills.filter {
                            val c = java.util.Calendar.getInstance().apply { timeInMillis = it.date }
                            c.get(java.util.Calendar.MONTH) == m && c.get(java.util.Calendar.YEAR) == y
                        }.sumOf { it.totalAmount }
                    }
                    val totalKg = remember(refills) { refills.sumOf { it.quantityKg } }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToRefills() }
                            .testTag("home_refill_summary_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = NeonLime,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = DeepForest,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "REFILL & EXPENSE SUMMARY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "₹${monthlySpend.toInt()} this month",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", totalKg)} kg CNG logged",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = onNavigateToRefills,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("My Refills →", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (pumpToReport != null) {
        val target = pumpToReport!!
        ReportLiveStatusModal(
            currentStatus = target.stockStatus,
            currentPressure = target.gasPressureBar,
            currentQueue = target.queueWaitMinutes,
            onDismiss = { pumpToReport = null },
            onSubmit = { status, pressure, queue, isGasAvail, driverName ->
                pumpViewModel.reportLivePumpStatus(
                    pumpId = target.id,
                    stockStatus = status,
                    pressureBar = pressure,
                    queueMinutes = queue,
                    isGasAvailable = isGasAvail,
                    reporterName = driverName
                ) { isCloudSynced ->
                    if (isCloudSynced) {
                        Toast.makeText(context, "Submitted! Status updated for community.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Saved locally on device (offline mode).", Toast.LENGTH_LONG).show()
                    }
                }
                pumpToReport = null
            }
        )
    }
}

private data class PumpStatusStyle(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val bg: Color,
    val subtext: String
)

@Composable
fun PumpStationCard(
    displayItem: PumpDisplayItem,
    strings: LocalizedStrings,
    dateFormat: SimpleDateFormat,
    isFeaturedNearest: Boolean = false,
    onViewDetails: () -> Unit,
    onUpdateStatus: () -> Unit,
    onNavigate: () -> Unit
) {
    val pump = displayItem.pump
    val now = System.currentTimeMillis()
    val ageMinutes = ((now - pump.lastUpdatedTime) / 60000L).coerceAtLeast(0L)
    val isFresh = ageMinutes < 60L // Configurable freshness threshold: 60 minutes

    // Status style with explicit label and icon
    val statusStyle = if (isFresh) {
        if (pump.isGasAvailable && pump.stockStatus != "OUT_OF_STOCK") {
            PumpStatusStyle(
                label = "AVAILABLE",
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF15803D),
                bg = Color(0xFFE8F5E9),
                subtext = "Reported ${if (ageMinutes == 0L) "just now" else "${ageMinutes}m ago"}"
            )
        } else {
            PumpStatusStyle(
                label = "UNAVAILABLE",
                icon = Icons.Default.Cancel,
                color = Color(0xFFB91C1C),
                bg = Color(0xFFFEE2E2),
                subtext = "Reported ${if (ageMinutes == 0L) "just now" else "${ageMinutes}m ago"}"
            )
        }
    } else {
        val lastState = if (pump.isGasAvailable) "Available" else "Unavailable"
        val timeLabel = if (ageMinutes < 1440L) "${ageMinutes / 60}h ago" else "${ageMinutes / 1440}d ago"
        PumpStatusStyle(
            label = "UNKNOWN / STALE",
            icon = Icons.Default.HelpOutline,
            color = Color(0xFFB45309),
            bg = Color(0xFFFEF3C7),
            subtext = "Prev: $lastState ($timeLabel)"
        )
    }

    // Pressure text based on freshness (never fake)
    val pressureText = if (isFresh && pump.gasPressureBar > 0.0) {
        "${pump.gasPressureBar.toInt()} bar"
    } else {
        "Not reported"
    }

    // Queue text based on freshness (never fake)
    val queueText = if (isFresh && pump.queueWaitMinutes >= 0) {
        "~${pump.queueWaitMinutes}m wait"
    } else {
        "Not reported"
    }

    // Gas text based on freshness
    val gasText = if (isFresh) {
        if (pump.isGasAvailable && pump.stockStatus != "OUT_OF_STOCK") "CNG In Stock" else "No CNG"
    } else {
        "Not reported"
    }

    // Distance only if calculated from coordinates (never fake)
    val distanceText = if (displayItem.distanceKm > 0.0) {
        "📍 ${displayItem.distanceKm} km away"
    } else {
        "Distance uncalculated"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag(if (isFeaturedNearest) "nearest_pump_card" else "pump_card_${pump.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFeaturedNearest) 4.dp else 1.dp),
        border = BorderStroke(
            width = if (isFeaturedNearest) 2.dp else 1.dp,
            color = if (isFeaturedNearest) NeonLime else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Nearest badge if featured
            if (isFeaturedNearest) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonLime,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = DeepForest,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "NEAREST CNG STATION • निकटतम स्टेशन",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DeepForest
                        )
                    }
                }
            }

            // Travel Pass Row with Route Motif
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Route-Line Transit Motif along the left edge
                Column(
                    modifier = Modifier
                        .width(16.dp)
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Start Node
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (statusStyle.label == "AVAILABLE") Color(0xFF15803D) else MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                    // Connecting transit line
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    // Bottom Destination Waypoint Node
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .border(2.dp, NeonLimeDark, CircleShape)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Card details
                Column(modifier = Modifier.weight(1f)) {
                    // Station Name & Distance Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = pump.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${pump.provider} • ${pump.address}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = distanceText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (displayItem.distanceKm > 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Prominent Status Chip with Explicit Label & Icon
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusStyle.bg,
                                border = BorderStroke(1.dp, statusStyle.color.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = statusStyle.icon,
                                        contentDescription = statusStyle.label,
                                        tint = statusStyle.color,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = statusStyle.label,
                                        color = statusStyle.color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = statusStyle.subtext,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Compact "Gas • Pressure • Queue" Information Strip
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Gas Status
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⛽", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = gasText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Pressure
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💨", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = pressureText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Queue Wait
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⏱️", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = queueText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Price & Source Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${pump.pricePerKg}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = " / kg",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        Text(
                            text = if (pump.reportedByDriver.isNotBlank()) pump.reportedByDriver else "Community report",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Buttons Row: [Status] [Details] [Raasta dekho]
                    // All interactive touch targets are at least 48dp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onUpdateStatus,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Status", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onViewDetails,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text(strings.details, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Signature Primary Action: Signature Lime #C5F45A with dark forest text #102A23
                        Button(
                            onClick = onNavigate,
                            modifier = Modifier
                                .weight(1.4f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonLime,
                                contentColor = DeepForest
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = DeepForest
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Raasta dekho",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepForest
                            )
                        }
                    }
                }
            }
        }
    }
}
