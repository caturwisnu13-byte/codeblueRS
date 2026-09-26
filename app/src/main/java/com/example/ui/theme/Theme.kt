package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CodeBlueAccent,
    onPrimary = Color.Black,
    primaryContainer = CodeBluePrimary,
    onPrimaryContainer = Color.White,
    secondary = CodeBlueSecondary,
    onSecondary = Color.White,
    tertiary = EmergencyRedBright,
    onTertiary = Color.White,
    background = DarkHospitalBg,
    onBackground = Color(0xFFE2E8F0),
    surface = DarkHospitalSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkHospitalCard,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = DarkHospitalOutline,
    error = EmergencyRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CodeBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E4FF),
    onPrimaryContainer = CodeBluePrimaryDark,
    secondary = CodeBlueSecondary,
    onSecondary = Color.White,
    tertiary = EmergencyRed,
    onTertiary = Color.White,
    background = LightHospitalBg,
    onBackground = Color(0xFF0F172A),
    surface = LightHospitalSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightHospitalCard,
    onSurfaceVariant = Color(0xFF334155),
    outline = LightHospitalOutline,
    error = EmergencyRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our specialized hospital theme
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
