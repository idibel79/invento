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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Palette claire complète : chaque rôle est renseigné pour ne jamais retomber sur les teintes
 * violettes par défaut de Material 3 (conteneurs des cartes, FilterChip, menus, dialogues...).
 */
private val LightColors = lightColorScheme(
    primary = BamPrimary,
    onPrimary = Color.White,
    primaryContainer = BamPrimaryContainer,
    onPrimaryContainer = BamNavy,
    inversePrimary = BamCyan,
    secondary = BamNavy,
    onSecondary = Color.White,
    secondaryContainer = BamPrimaryContainer,
    onSecondaryContainer = BamNavy,
    tertiary = BamCyan,
    onTertiary = BamNavy,
    tertiaryContainer = Color(0xFFDDF6FD),
    onTertiaryContainer = BamNavy,
    error = StatusEcart,
    onError = Color.White,
    errorContainer = StatusEcartBg,
    onErrorContainer = StatusEcart,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = StatusPendingBg,
    onSurfaceVariant = TextSecondary,
    surfaceTint = BamPrimary,
    inverseSurface = TextPrimary,
    inverseOnSurface = Color.White,
    outline = BorderColor,
    outlineVariant = Color(0xFFE6EAF2),
    scrim = Color.Black,
    surfaceBright = CardSurface,
    surfaceDim = Color(0xFFE9EDF5),
    surfaceContainerLowest = CardSurface,
    surfaceContainerLow = CardSurface,
    surfaceContainer = CardSurface,
    surfaceContainerHigh = CardSurface,
    surfaceContainerHighest = AppBackground
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = BamNavy,
    primaryContainer = BamPrimary,
    onPrimaryContainer = Color.White,
    secondary = BamCyan,
    onSecondary = BamNavy,
    secondaryContainer = DarkSurfaceHigh,
    onSecondaryContainer = Color.White,
    tertiary = BamCyan,
    onTertiary = BamNavy,
    error = Color(0xFFFF8A80),
    onError = Color(0xFF3B0000),
    background = DarkBackground,
    onBackground = Color(0xFFE6ECF8),
    surface = DarkSurface,
    onSurface = Color(0xFFE6ECF8),
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = Color(0xFFA9B6CE),
    surfaceTint = DarkPrimary,
    outline = Color(0xFF3A4C78),
    outlineVariant = Color(0xFF243866),
    surfaceBright = DarkSurfaceHigh,
    surfaceDim = DarkBackground,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = DarkSurfaceHigh
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
