package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.ui.components.SecondaryActionButton
import ma.bam.inventaire.data.local.entity.StockArticleEntity
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamPrimaryContainer
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.ControlBorder
import ma.bam.inventaire.ui.theme.Hairline
import ma.bam.inventaire.ui.theme.SoftBand
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.TextSecondary
import ma.bam.inventaire.util.formatQuantity

/**
 * Détail d'un article en lecture seule (ouvert par appui long sur sa carte), mise en page
 * « fiche + bandeau prix » : PID, catégorie et zone en pleine largeur, puis PPV, PPH et TVA dans
 * un bandeau. Aucun texte n'est coupé. Glisser vers le bas ou « Fermer » referme la feuille.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailDialog(
    article: StockArticleEntity,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SheetShape,
        containerColor = CardSurface,
        dragHandle = { SheetHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Text(
                article.designation.ifBlank { article.codeArticle },
                fontSize = 16.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BamNavy,
                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
            )
            FieldRow("PID", article.codeArticle)
            HorizontalDivider(thickness = 1.dp, color = FieldDivider)
            FieldRow("Catégorie", article.categorie)
            HorizontalDivider(thickness = 1.dp, color = FieldDivider)
            BarcodeField(article.codeBarre1, article.codeBarre2)
            HorizontalDivider(thickness = 1.dp, color = FieldDivider)
            FieldRow("Zone", article.emplacement)
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PriceBand)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AmountRow("PPV", "${formatQuantity(article.prixUnitaire)} DH")
                AmountRow("PPH", "${formatQuantity(article.pph)} DH")
                HorizontalDivider(thickness = 1.dp, color = BandDivider)
                AmountRow("TVA", article.tva)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(QuantityBand)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                AmountRow("Quantité système", formatQuantity(article.quantiteTheorique))
                article.quantiteReelle?.let {
                    AmountRow("Quantité inventoriée", formatQuantity(it), valueColor = BamPrimary)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            SecondaryActionButton("Fermer", onDismiss, Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private val FieldDivider: Color @Composable get() = Hairline
private val PriceBand: Color @Composable get() = BamPrimaryContainer
private val BandDivider: Color @Composable get() = ControlBorder
private val QuantityBand: Color @Composable get() = SoftBand

/** Icône code-barres de la maquette : cinq traits verticaux (viewBox 24, trait 2, bouts arrondis). */
internal val BarcodeIcon: ImageVector = ImageVector.Builder(
    name = "Barcode",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        listOf(4f, 8f, 12f, 15f, 19f).forEach { x ->
            moveTo(x, 5f)
            verticalLineTo(19f)
        }
    }
}.build()

/** Champ « Code-barres » : code 1, puis code 2 en dessous s'il existe, chacun précédé de l'icône. */
@Composable
private fun BarcodeField(code1: String, code2: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp)) {
        Text(
            "CODE-BARRES",
            fontSize = 10.5.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            color = TextSecondary
        )
        BarcodeLine(code1)
        if (code2.isNotBlank()) BarcodeLine(code2)
    }
}

@Composable
private fun BarcodeLine(code: String) {
    Row(
        modifier = Modifier.padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            BarcodeIcon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            code.ifBlank { "-" },
            fontSize = 13.5.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            color = BamNavy,
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}

/** Champ pleine largeur : libellé en petites majuscules, valeur en entier sur plusieurs lignes. */
@Composable
private fun FieldRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp)) {
        Text(
            label.uppercase(),
            fontSize = 10.5.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            color = TextSecondary
        )
        Text(
            value.ifBlank { "-" },
            fontSize = 13.5.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            color = BamNavy,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/** Ligne du bandeau prix : libellé à gauche, montant à droite (passe à la ligne aligné à droite). */
@Composable
private fun AmountRow(label: String, value: String, valueColor: Color = BamNavy) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            label,
            fontSize = 12.sp,
            lineHeight = 20.sp,
            color = TextSecondary,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            value.ifBlank { "-" },
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor,
            textAlign = TextAlign.End,
            style = TextStyle(fontFeatureSettings = "tnum"),
            modifier = Modifier.weight(1f)
        )
    }
}
