package ma.bam.inventaire.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Palette Invento : chaque jeton existe en version claire et sombre. Les écrans lisent les
 * jetons ci-dessous (BamNavy, CardSurface...) qui suivent le thème actif via [LocalInventoPalette],
 * ce qui évite les textes marine sur fond marine (ou blanc sur blanc) en mode sombre.
 */
@Immutable
data class InventoPalette(
    // Marque
    val navy: Color, // titres, textes et icônes d'accent
    val navyFill: Color, // fonds pleins (boutons, puces sélectionnées) sous texte blanc
    val primary: Color, // bleu d'accent (textes, anneaux, focus)
    val primaryFill: Color, // pastilles / badges bleus pleins sous texte blanc
    val primaryContainer: Color,
    val selectedCard: Color, // carte sélectionnée / ligne mise en avant
    val cyan: Color,
    val onBrand: Color, // texte sur navyFill / primaryFill
    // Neutres
    val background: Color,
    val surface: Color,
    val band: Color, // bandeaux et cases discrets dans une carte
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val controlBorder: Color, // bordure des boutons secondaires / segments
    val hairline: Color, // séparateurs fins
    val track: Color, // rail des barres de progression
    // Statuts
    val success: Color,
    val successBg: Color,
    val ecart: Color,
    val ecartBg: Color,
    val negative: Color,
    val negativeBg: Color,
    val pending: Color,
    val pendingBg: Color
)

val LightInventoPalette = InventoPalette(
    navy = Color(0xFF0B2A6B),
    navyFill = Color(0xFF0B2A6B),
    primary = Color(0xFF1652D9),
    primaryFill = Color(0xFF1652D9),
    primaryContainer = Color(0xFFE6EEFF),
    selectedCard = Color(0xFFCEDEFF),
    cyan = Color(0xFF3FD0F5),
    onBrand = Color.White,
    background = Color(0xFFF4F6FB),
    surface = Color(0xFFFFFFFF),
    band = Color(0xFFF4F6FB),
    textPrimary = Color(0xFF0F1A33),
    textSecondary = Color(0xFF55627A),
    textMuted = Color(0xFF8A95AB),
    border = Color(0xFFD5DCEA),
    controlBorder = Color(0xFFC9D3E6),
    hairline = Color(0xFFE6E9F0),
    track = Color(0xFFE1E6F0),
    success = Color(0xFF15803D), successBg = Color(0xFFE3F5EA),
    ecart = Color(0xFFC42B2B), ecartBg = Color(0xFFFDECEC),
    negative = Color(0xFF9A5B00), negativeBg = Color(0xFFFFF3DC),
    pending = Color(0xFF55627A), pendingBg = Color(0xFFEEF0F4)
)

// Mode sombre : fonds marine foncé, textes bleu très clair, accents bleu lumineux.
val DarkInventoPalette = InventoPalette(
    navy = Color(0xFFDCE6FF),
    navyFill = Color(0xFF1652D9),
    primary = Color(0xFF8DB0FF),
    primaryFill = Color(0xFF2F66E8),
    primaryContainer = Color(0xFF1B336E),
    selectedCard = Color(0xFF213F86),
    cyan = Color(0xFF3FD0F5),
    onBrand = Color.White,
    background = Color(0xFF07152F),
    surface = Color(0xFF0E1F44),
    band = Color(0xFF15295A),
    textPrimary = Color(0xFFE6ECF8),
    textSecondary = Color(0xFFA9B6CE),
    textMuted = Color(0xFF7C89A3),
    border = Color(0xFF2E4170),
    controlBorder = Color(0xFF3A4C78),
    hairline = Color(0xFF243866),
    track = Color(0xFF2A3D6B),
    success = Color(0xFF4ADE80), successBg = Color(0xFF0F3A26),
    ecart = Color(0xFFFF8A80), ecartBg = Color(0xFF4A1B22),
    negative = Color(0xFFF5B544), negativeBg = Color(0xFF3D2C0A),
    pending = Color(0xFFA9B6CE), pendingBg = Color(0xFF1C2B4F)
)

val LocalInventoPalette = staticCompositionLocalOf { LightInventoPalette }

private inline val p: InventoPalette
    @Composable @ReadOnlyComposable
    get() = LocalInventoPalette.current

// Palette de marque
val BamNavy: Color
    @Composable @ReadOnlyComposable
    get() = p.navy
val BamNavyFill: Color
    @Composable @ReadOnlyComposable
    get() = p.navyFill
val BamPrimary: Color
    @Composable @ReadOnlyComposable
    get() = p.primary
val BamPrimaryFill: Color
    @Composable @ReadOnlyComposable
    get() = p.primaryFill
val BamPrimaryContainer: Color
    @Composable @ReadOnlyComposable
    get() = p.primaryContainer
val SelectedCardBg: Color
    @Composable @ReadOnlyComposable
    get() = p.selectedCard
val BamCyan: Color
    @Composable @ReadOnlyComposable
    get() = p.cyan
val OnBrand: Color
    @Composable @ReadOnlyComposable
    get() = p.onBrand

// Neutres
val AppBackground: Color
    @Composable @ReadOnlyComposable
    get() = p.background
val CardSurface: Color
    @Composable @ReadOnlyComposable
    get() = p.surface
val SoftBand: Color
    @Composable @ReadOnlyComposable
    get() = p.band
val TextPrimary: Color
    @Composable @ReadOnlyComposable
    get() = p.textPrimary
val TextSecondary: Color
    @Composable @ReadOnlyComposable
    get() = p.textSecondary
val TextMuted: Color
    @Composable @ReadOnlyComposable
    get() = p.textMuted
val BorderColor: Color
    @Composable @ReadOnlyComposable
    get() = p.border
val ControlBorder: Color
    @Composable @ReadOnlyComposable
    get() = p.controlBorder
val Hairline: Color
    @Composable @ReadOnlyComposable
    get() = p.hairline
val TrackColor: Color
    @Composable @ReadOnlyComposable
    get() = p.track

// Statuts : couleur du texte / de l'icône et fond associé (pastilles, cases)
val StatusSuccess: Color
    @Composable @ReadOnlyComposable
    get() = p.success
val StatusSuccessBg: Color
    @Composable @ReadOnlyComposable
    get() = p.successBg
val StatusEcart: Color
    @Composable @ReadOnlyComposable
    get() = p.ecart
val StatusEcartBg: Color
    @Composable @ReadOnlyComposable
    get() = p.ecartBg
val StatusNegative: Color
    @Composable @ReadOnlyComposable
    get() = p.negative
val StatusNegativeBg: Color
    @Composable @ReadOnlyComposable
    get() = p.negativeBg
val StatusPending: Color
    @Composable @ReadOnlyComposable
    get() = p.pending
val StatusPendingBg: Color
    @Composable @ReadOnlyComposable
    get() = p.pendingBg
