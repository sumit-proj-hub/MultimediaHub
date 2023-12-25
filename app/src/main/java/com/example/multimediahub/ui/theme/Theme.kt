package com.example.multimediahub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val darkColorScheme = ColorScheme(
    primary = Color(18412354541153419264UL),
    onPrimary = Color(18374746132472397824UL),
    primaryContainer = Color(18374772641010548736UL),
    onPrimaryContainer = Color(18423923705580093440UL),
    inversePrimary = Color(18374801357161889792UL),
    secondary = Color(18425013132624658432UL),
    onSecondary = Color(18382906677708980224UL),
    secondaryContainer = Color(18389404890213384192UL),
    onSecondaryContainer = Color(18432925338557218816UL),
    tertiary = Color(18428664713819717632UL),
    onTertiary = Color(18386277861964120064UL),
    tertiaryContainer = Color(18392495703298408448UL),
    onTertiaryContainer = Color(18437422409834299392UL),
    background = Color(18377805892943872000UL),
    onBackground = Color(18436581180359835648UL),
    surface = Color(18377805892943872000UL),
    onSurface = Color(18436581180359835648UL),
    surfaceVariant = Color(18392498890164142080UL),
    onSurfaceVariant = Color(18428668974427275264UL),
    surfaceTint = Color(18412354541153419264UL),
    inverseSurface = Color(18436581180359835648UL),
    inverseOnSurface = Color(18386282156931416064UL),
    error = Color(18443006511564193792UL),
    onError = Color(18401730136387878912UL),
    errorContainer = Color(18414124965327536128UL),
    onErrorContainer = Color(18445018785346748416UL),
    outline = Color(18413409720128765952UL),
    outlineVariant = Color(18392498890164142080UL),
    scrim = Color(18374686479671623680UL),
    surfaceBright = Color(18388825361686200320UL),
    surfaceDim = Color(18377805892943872000UL),
    surfaceContainer = Color(18381759788296962048UL),
    surfaceContainerHigh = Color(18384585580424986624UL),
    surfaceContainerHighest = Color(18387693947041349632UL),
    surfaceContainerLow = Color(18380347998187028480UL),
    surfaceContainerLowest = Color(18375830053368889344UL)
)

@Composable
fun MultimediaHubTheme(
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }

        else -> darkColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            window.navigationBarColor = Color.Black.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}