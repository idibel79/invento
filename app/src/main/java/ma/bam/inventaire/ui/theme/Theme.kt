package ma.bam.inventaire.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val L = LightInventoPalette
private val D = DarkInventoPalette

/**
 * Palette claire complète : chaque rôle est renseigné pour ne jamais retomber sur les teintes
 * violettes par défaut de Material 3 (conteneurs des cartes, FilterChip, menus, dialogues...).
 */
private val LightColors = lightColorScheme(
    primary = L.primary,
    onPrimary = Color.White,
    primaryContainer = L.primaryContainer,
    onPrimaryContainer = L.navy,
    inversePrimary = L.cyan,
    secondary = L.navy,
    onSecondary = Color.White,
    secondaryContainer = L.primaryContainer,
    onSecondaryContainer = L.navy,
    tertiary = L.cyan,
    onTertiary = L.navy,
    tertiaryContainer = Color(0xFFDDF6FD),
    onTertiaryContainer = L.navy,
    error = L.ecart,
    onError = Color.White,
    errorContainer = L.ecartBg,
    onErrorContainer = L.ecart,
    background = L.background,
    onBackground = L.textPrimary,
    surface = L.surface,
    onSurface = L.textPrimary,
    surfaceVariant = L.pendingBg,
    onSurfaceVariant = L.textSecondary,
    surfaceTint = L.primary,
    inverseSurface = L.textPrimary,
    inverseOnSurface = Color.White,
    outline = L.border,
    outlineVariant = Color(0xFFE6EAF2),
    scrim = Color.Black,
    surfaceBright = L.surface,
    surfaceDim = Color(0xFFE9EDF5),
    surfaceContainerLowest = L.surface,
    surfaceContainerLow = L.surface,
    surfaceContainer = L.surface,
    surfaceContainerHigh = L.surface,
    surfaceContainerHighest = L.background
)

/** Palette sombre complète, même logique : aucun rôle laissé aux valeurs Material par défaut. */
private val DarkColors = darkColorScheme(
    primary = D.primary,
    onPrimary = D.background,
    primaryContainer = D.primaryContainer,
    onPrimaryContainer = D.navy,
    inversePrimary = L.primary,
    secondary = D.cyan,
    onSecondary = D.background,
    secondaryContainer = D.band,
    onSecondaryContainer = D.navy,
    tertiary = D.cyan,
    onTertiary = D.background,
    tertiaryContainer = Color(0xFF0C3B4A),
    onTertiaryContainer = Color(0xFFBDEFFC),
    error = D.ecart,
    onError = Color(0xFF3B0000),
    errorContainer = D.ecartBg,
    onErrorContainer = D.ecart,
    background = D.background,
    onBackground = D.textPrimary,
    surface = D.surface,
    onSurface = D.textPrimary,
    surfaceVariant = D.band,
    onSurfaceVariant = D.textSecondary,
    surfaceTint = D.primary,
    inverseSurface = D.textPrimary,
    inverseOnSurface = D.background,
    outline = D.controlBorder,
    outlineVariant = D.hairline,
    scrim = Color.Black,
    surfaceBright = D.band,
    surfaceDim = D.background,
    surfaceContainerLowest = D.background,
    surfaceContainerLow = D.surface,
    surfaceContainer = D.surface,
    surfaceContainerHigh = D.surface,
    surfaceContainerHighest = D.band
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun InventaireTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Désactivé : la palette de marque doit être identique sur tous les téléphones.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    CompositionLocalProvider(
        LocalInventoPalette provides if (darkTheme) DarkInventoPalette else LightInventoPalette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
