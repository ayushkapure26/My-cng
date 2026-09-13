package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// "Neon Street" Signature Mobility Color Palette
// ==========================================

// Primary Action Accent: Signature Lime
val NeonLime = Color(0xFFC5F45A)
val NeonLimeDark = Color(0xFFAEDC43)
val NeonLimeLight = Color(0xFFD6F87E)

// Deep Forest (Contrast anchor for light mode text & dark accents)
val DeepForest = Color(0xFF102A23)
val DeepForestDark = Color(0xFF0A1C17)
val DeepForestLight = Color(0xFF1B3B32)

// Light Mode: Warm off-white #F6F7F2, deep forest text #102A23
val NeonLightBg = Color(0xFFF6F7F2)
val NeonLightSurface = Color(0xFFFFFFFF)
val NeonLightSurfaceVariant = Color(0xFFEBEFE6)
val NeonLightBorder = Color(0xFFD5DDD0)
val NeonLightText = Color(0xFF102A23)
val NeonLightTextMuted = Color(0xFF3B564E)

// Dark Mode: Charcoal-green #101B17, surfaces #1B2B24, ivory text
val NeonDarkBg = Color(0xFF101B17)
val NeonDarkSurface = Color(0xFF1B2B24)
val NeonDarkSurfaceVariant = Color(0xFF243A31)
val NeonDarkBorder = Color(0xFF2C443A)
val NeonDarkText = Color(0xFFF3F4ED)
val NeonDarkTextMuted = Color(0xFFB0C2B9)

// Status Indicators (never used alone, accompanied by labels & icons)
val StatusEmeraldLight = Color(0xFF15803D)
val StatusEmeraldDark = Color(0xFF22C55E)
val StatusEmeraldBgLight = Color(0xFFE8F5E9)
val StatusEmeraldBgDark = Color(0xFF163324)

val StatusAmberLight = Color(0xFFB45309)
val StatusAmberDark = Color(0xFFF59E0B)
val StatusAmberBgLight = Color(0xFFFEF3C7)
val StatusAmberBgDark = Color(0xFF382910)

val StatusRedLight = Color(0xFFB91C1C)
val StatusRedDark = Color(0xFFEF4444)
val StatusRedBgLight = Color(0xFFFEE2E2)
val StatusRedBgDark = Color(0xFF381A1A)

val StatusUnknownLight = Color(0xFF475569)
val StatusUnknownDark = Color(0xFF94A3B8)
val StatusUnknownBgLight = Color(0xFFF1F5F9)
val StatusUnknownBgDark = Color(0xFF1E293B)

// Road & Route Motif Line
val NeonRouteLine = Color(0xFF65A30D)
val NeonRouteMarker = Color(0xFFC5F45A)

// Backward-compatible mappings so existing components compile seamlessly
val EmeraldGreen = StatusEmeraldLight
val DarkTeal = DeepForest
val BrightMint = NeonLime
val LightEmerald = StatusEmeraldDark
val CngGreen = StatusEmeraldLight
val AmberAccent = StatusAmberLight
val ErrorRed = StatusRedLight
val SuccessGreen = StatusEmeraldLight
val ElectricCyan = Color(0xFF06B6D4)
val DeepNavy = DeepForest

val DarkBackground = NeonDarkBg
val DarkSurface = NeonDarkSurface
val DarkSurfaceVariant = NeonDarkSurfaceVariant
val DarkOnBackground = NeonDarkText
val DarkOnSurface = NeonDarkText
val DarkBorder = NeonDarkBorder

val LightBackground = NeonLightBg
val LightSurface = NeonLightSurface
val LightSurfaceVariant = NeonLightSurfaceVariant
val LightOnBackground = NeonLightText
val LightOnSurface = NeonLightText
val LightOnSurfaceVariant = NeonLightTextMuted
val LightBorder = NeonLightBorder



