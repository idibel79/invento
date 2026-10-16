package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ma.bam.inventaire.ui.components.PopupHaptic
import ma.bam.inventaire.data.repository.ImportedArticle
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamNavyFill
import ma.bam.inventaire.ui.theme.BamPrimaryContainer
import ma.bam.inventaire.ui.theme.CardSurface
import ma.bam.inventaire.ui.theme.ControlBorder
import ma.bam.inventaire.ui.theme.Hairline
import ma.bam.inventaire.ui.theme.OnBrand
import ma.bam.inventaire.ui.theme.SoftBand
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.StatusSuccess
import ma.bam.inventaire.ui.theme.StatusSuccessBg
import ma.bam.inventaire.ui.theme.TextSecondary
import ma.bam.inventaire.util.formatQuantity

/**
 * Feuille du bas affichée juste après la création manuelle d'un produit, même style que la fiche
 * détail (fiche C) avec un bandeau vert « Produit créé ». « Créer un autre » ferme la feuille et
 * rouvre la création ; « Terminé », glisser vers le bas ou toucher en dehors la ferment.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleCreatedSheet(
    article: ImportedArticle,
    onCreateAnother: () -> Unit,
    onDismiss: () -> Unit
) {
    PopupHaptic()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    fun hideThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }
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
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SuccessBanner()
            Text(
                article.designation.ifBlank { article.codeArticle },
                fontSize = 16.sp,
                lineHeight = 21.6.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BamNavy
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                CreatedField("PID") { FieldValue(article.codeArticle) }
                CreatedField("Catégorie") { FieldValue(article.categorie) }
                val codes = listOf(article.codeBarre1, article.codeBarre2).filter { it.isNotBlank() }
                if (codes.isNotEmpty()) {
                    CreatedField("Code-barres") { codes.forEach { CreatedBarcodeLine(it) } }
                }
                CreatedField("Zone") { FieldValue(article.emplacement) }
            }
            Band(color = BamPrimaryContainer) {
                BandRow("PPV", "${formatQuantity(article.prixUnitaire)} DH")
                BandRow("PPH", "${formatQuantity(article.pph)} DH")
            }
            Band(color = SoftBand) {
                BandRow("Quantité système", formatQuantity(article.quantiteTheorique))
                article.quantiteReelle?.let {
                    BandRow("Quantité inventoriée", formatQuantity(it), valueColor = BamPrimary)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { hideThen(onCreateAnother) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ControlBorder),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text("Créer un autre", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BamNavy)
                }
                Button(
                    onClick = { hideThen(onDismiss) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BamNavyFill, contentColor = OnBrand),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Text("Terminé", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SuccessBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StatusSuccessBg)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(30.dp), contentAlignment = Alignment.Center) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = StatusSuccess,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text("Produit créé", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
    }
}

/** Champ pleine largeur : libellé en petites majuscules, valeur en dessous, trait fin sous le champ. */
@Composable
private fun CreatedField(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
        Text(
            label.uppercase(),
            fontSize = 10.5.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = TextSecondary
        )
        content()
    }
    HorizontalDivider(thickness = 1.dp, color = Hairline)
}

@Composable
private fun FieldValue(value: String) {
    Text(
        value.ifBlank { "-" },
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.Bold,
        color = BamNavy,
        modifier = Modifier.padding(top = 1.dp)
    )
}

@Composable
private fun CreatedBarcodeLine(code: String) {
    Row(modifier = Modifier.padding(top = 1.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(BarcodeIcon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            code,
            fontSize = 13.5.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Bold,
            color = BamNavy,
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}

@Composable
private fun Band(color: Color, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        content()
    }
}

/** Ligne de bandeau : libellé à gauche, valeur à droite (passe à la ligne alignée à droite). */
@Composable
private fun BandRow(label: String, value: String, valueColor: Color = BamNavy) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(end = 10.dp))
        Text(
            value,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor,
            textAlign = TextAlign.End,
            style = TextStyle(fontFeatureSettings = "tnum"),
            modifier = Modifier.weight(1f)
        )
    }
}
