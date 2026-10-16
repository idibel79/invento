package ma.bam.inventaire.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.R

val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold)
)

/** Chiffres à chasse fixe, pour que les compteurs ne « sautent » pas quand ils changent. */
const val TabularNums = "tnum"

private val Base = Typography()

private fun TextStyle.jakarta() = copy(fontFamily = PlusJakartaSans)

val Typography = Typography(
    displayLarge = Base.displayLarge.jakarta(),
    displayMedium = Base.displayMedium.jakarta(),
    displaySmall = Base.displaySmall.jakarta(),
    headlineLarge = Base.headlineLarge.jakarta().copy(fontWeight = FontWeight.Bold),
    headlineMedium = Base.headlineMedium.jakarta().copy(fontWeight = FontWeight.Bold),
    headlineSmall = Base.headlineSmall.jakarta().copy(fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = Base.titleMedium.jakarta().copy(fontWeight = FontWeight.Bold),
    titleSmall = Base.titleSmall.jakarta().copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = Base.bodyMedium.jakarta(),
    bodySmall = Base.bodySmall.jakarta(),
    labelLarge = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = Base.labelMedium.jakarta(),
    labelSmall = Base.labelSmall.jakarta()
)
