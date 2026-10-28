package ma.bam.inventaire.ui.quantityentry

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.R
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamPrimary
import ma.bam.inventaire.ui.theme.TextMuted
import ma.bam.inventaire.ui.theme.TextPrimary
import ma.bam.inventaire.ui.theme.TextSecondary

private const val REMARQUE_MAX_LENGTH = 200

/** Remarque facultative de la correction (200 caractères max), avec compteur sous le champ. */
@Composable
internal fun RemarqueField(value: String, onValueChange: (String) -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Column {
        RemarqueLabel()
        Spacer(modifier = Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.take(REMARQUE_MAX_LENGTH)) },
            maxLines = 3,
            interactionSource = interaction,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences
            ),
            textStyle = TextStyle(fontSize = 13.5.sp, lineHeight = 19.575.sp, color = TextPrimary),
            cursorBrush = SolidColor(BamPrimary),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner -> RemarqueBox(isEmpty = value.isEmpty(), focused = focused, inner = inner) }
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            "${value.length} / $REMARQUE_MAX_LENGTH",
            fontSize = 11.sp,
            color = TextMuted,
            textAlign = TextAlign.End,
            style = Tabular,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RemarqueLabel() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.AutoMirrored.Outlined.StickyNote2,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        FieldLabel(stringResource(R.string.ecart_remark))
    }
}

/** Cadre du champ remarque : bordure marine épaisse quand il a le focus, texte d'aide quand il est vide. */
@Composable
private fun RemarqueBox(isEmpty: Boolean, focused: Boolean, inner: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 66.dp)
            .border(
                if (focused) 2.dp else 1.dp,
                if (focused) BamNavy else StepBorder,
                ControlShape
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        if (isEmpty) {
            Text(
                stringResource(R.string.ecart_remark_placeholder),
                fontSize = 13.5.sp,
                lineHeight = 19.575.sp,
                color = TextMuted
            )
        }
        inner()
    }
}
