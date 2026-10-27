package ma.bam.inventaire.ui.importinventory

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private const val EXCEL_GREEN = 0xFF1D6F42
private const val EXCEL_SHEET = 0xFFE3F5EA

/** Icône fichier Excel (viewBox 40 × 40), commune à tous les états de la feuille d'import. */
internal val ExcelFileIcon: ImageVector by lazy {
    val green = SolidColor(Color(EXCEL_GREEN))
    ImageVector.Builder(
        name = "ExcelFile",
        defaultWidth = 40.dp,
        defaultHeight = 40.dp,
        viewportWidth = 40f,
        viewportHeight = 40f
    ).apply {
        addPath(
            pathData = addPathNodes("M9 3h15l9 9v23a2 2 0 0 1-2 2H9a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z"),
            fill = SolidColor(Color(EXCEL_SHEET)),
            stroke = green,
            strokeLineWidth = 2f
        )
        addPath(
            pathData = addPathNodes("M24 3v9h9"),
            stroke = green,
            strokeLineWidth = 2f
        )
        // rect x=4 y=18 w=20 h=13 rx=2
        addPath(
            pathData = addPathNodes("M6 18H22A2 2 0 0 1 24 20V29A2 2 0 0 1 22 31H6A2 2 0 0 1 4 29V20A2 2 0 0 1 6 18Z"),
            fill = green
        )
        addPath(
            pathData = addPathNodes("M9 21.5l6 6M15 21.5l-6 6"),
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        )
    }.build()
}
