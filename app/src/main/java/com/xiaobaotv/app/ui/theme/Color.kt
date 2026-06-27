package com.xiaobaotv.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary palette - Blue accent for interactive elements
val Primary = Color(0xFF1A73E8)
val PrimaryDark = Color(0xFF1557B0)
val PrimaryLight = Color(0xFF4A90D9)
val PrimaryContainer = Color(0xFFD3E3FD)
val OnPrimaryContainer = Color(0xFF041E49)

// Secondary palette - Warm amber accent for ratings/promotions
val Secondary = Color(0xFFFF6D00)
val SecondaryContainer = Color(0xFFFFDCC2)
val OnSecondaryContainer = Color(0xFF331200)

// Tertiary palette - Teal
val Tertiary = Color(0xFF007B83)
val TertiaryContainer = Color(0xFFCEEDEF)
val OnTertiaryContainer = Color(0xFF002022)

// Surface colors
val SurfaceLight = Color(0xFFF8F9FA)
val SurfaceDark = Color(0xFF0F172A)        // Slate-900 deep background
val SurfaceVariantLight = Color(0xFFE8EAED)
val SurfaceVariantDark = Color(0xFF1E293B) // Slate-800 card surface

// Background
val BackgroundLight = Color(0xFFFFFFFF)
val BackgroundDark = Color(0xFF0F172A)     // Same as SurfaceDark

// Navigation bar glass effect
val NavBarBackground = Color(0xFF0F172A).copy(alpha = 0.85f)

// Error
val Error = Color(0xFFD32F2F)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF410002)

// On colors
val OnPrimary = Color.White
val OnSecondary = Color.White
val OnBackgroundLight = Color(0xFF1A1C1E)
val OnBackgroundDark = Color(0xFFE3E2E6)
val OnSurfaceLight = Color(0xFF1A1C1E)
val OnSurfaceDark = Color(0xFFE3E2E6)
val OnSurfaceVariantLight = Color(0xFF44474F)
val OnSurfaceVariantDark = Color(0xFF94A3B8)  // Slate-400 for secondary text

// Score colors
val ScoreHigh = Color(0xFF4CAF50)
val ScoreMedium = Color(0xFFFF9800)
val ScoreLow = Color(0xFFF44336)

// Score star color
val ScoreStar = Color(0xFFFFB800)  // Amber for star ratings

// Episode status badge
val BadgeUpdating = Color(0xFF1A73E8)
val BadgeCompleted = Color(0xFF4CAF50)
val BadgeNew = Color(0xFFFF6D00)

// Genre/Promo badge
val PromoBadge = Color(0xFFF59E0B)     // Amber-500
val PromoBadgeText = Color.White
