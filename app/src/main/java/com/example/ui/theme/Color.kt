package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// "Neon Street" Signature Mobility Color Palette
// ==========================================

// Primary Action Accent: Signature Lime
val NeonLime = Color(0xFF7FA58D)
val NeonLimeDark = Color(0xFF6B927A)
val NeonLimeLight = Color(0xFFA8C3B1)

// Deep Forest (Contrast anchor for light mode text & dark accents)
val DeepForest = Color(0xFF21342C)
val DeepForestDark = Color(0xFF15231D)
val DeepForestLight = Color(0xFF31483E)

// Light Mode: Warm off-white #F6F7F2, deep forest text #102A23
val NeonLightBg = Color(0xFFF3F1EA)
val NeonLightSurface = Color(0xFFFBFAF6)
val NeonLightSurfaceVariant = Color(0xFFE8E5DB)
val NeonLightBorder = Color(0xFFD1CEC4)
val NeonLightText = Color(0xFF21342C)
val NeonLightTextMuted = Color(0xFF666E68)

// Dark Mode: Charcoal-green #101B17, surfaces #1B2B24, ivory text
val NeonDarkBg = Color(0xFF151A17)
val NeonDarkSurface = Color(0xFF1D2420)
val NeonDarkSurfaceVariant = Color(0xFF29322D)
val NeonDarkBorder = Color(0xFF3A4540)
val NeonDarkText = Color(0xFFF1F0EA)
val NeonDarkTextMuted = Color(0xFFAFB6B0)

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
val NeonRouteLine = Color(0xFF577565)
val NeonRouteMarker = Color(0xFF7FA58D)

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



