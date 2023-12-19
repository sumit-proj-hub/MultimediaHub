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
    primary = Color(18446663349799223296UL),
    onPrimary = Color(18398366683073871872UL),
    primaryContainer = Color(18406552633042010112UL),
    onPrimaryContainer = Color(18446704267952652288UL),
    inversePrimary = Color(18414740786328371200UL),
    secondary = Color(18439635374553694208UL),
    onSecondary = Color(18393873090720301056UL),
    secondaryContainer = Color(18400934244588191744UL),
    onSecondaryContainer = Color(18446704267952652288UL),
    tertiary = Color(18433735266310029312UL),
    onTertiary = Color(18389940030253563904UL),
    tertiaryContainer = Color(18396719709144743936UL),
    onTertiaryContainer = Color(18441928938629365760UL),
    background = Color(18383148346928791552UL),
    onBackground = Color(18443611380398424064UL),
    surface = Color(18383148346928791552UL),
    onSurface = Color(18443611380398424064UL),
    surfaceVariant = Color(18397842456545591296UL),
    onSurfaceVariant = Color(18435699178760830976UL),
    surfaceTint = Color(18446663349799223296UL),
    inverseSurface = Color(18443611380398424064UL),
    inverseOnSurface = Color(18391343153119494144UL),
    error = Color(18443006511564193792UL),
    onError = Color(18401730136387878912UL),
    errorContainer = Color(18414124965327536128UL),
    onErrorContainer = Color(18445018785346748416UL),
    outline = Color(18419878078315495424UL),
    outlineVariant = Color(18397842456545591296UL),
    scrim = Color(18374686479671623680UL),
    surfaceBright = Color(18393886353579311104UL),
    surfaceDim = Color(18383148346928791552UL),
    surfaceContainer = Color(18386257838826586112UL),
    surfaceContainerHigh = Color(18389366201147981824UL),
    surfaceContainerHighest = Color(18392754938934460416UL),
    surfaceContainerLow = Color(18385128627499958272UL),
    surfaceContainerLowest = Color(18382297299159089152UL)
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