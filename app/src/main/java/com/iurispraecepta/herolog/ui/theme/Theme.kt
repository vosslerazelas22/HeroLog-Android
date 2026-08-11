package com.iurispraecepta.herolog.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Amber400,
    onPrimary = Stone950,
    primaryContainer = Stone900,
    onPrimaryContainer = Amber400,
    secondary = Amber500,
    onSecondary = Stone950,
    background = Stone950,
    onBackground = Amber100,
    surface = Stone900,
    onSurface = Amber100,
    surfaceVariant = Stone800,
    onSurfaceVariant = Stone400
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Amber500,
    onPrimary = Stone950,
    primaryContainer = Stone900,
    onPrimaryContainer = Amber500,
    secondary = Amber400,
    onSecondary = Stone950,
    background = Stone950,
    onBackground = Amber100,
    surface = Stone900,
    onSurface = Amber100,
    surfaceVariant = Stone800,
    onSurfaceVariant = Stone400
  )

@Composable
fun HeroLogTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
