package ma.bam.inventaire.ui.inventorydetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ma.bam.inventaire.R
import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.ui.components.pluralLabel
import ma.bam.inventaire.ui.components.rememberTapHaptic
import ma.bam.inventaire.ui.theme.BamNavy
import ma.bam.inventaire.ui.theme.BamNavyFill
import ma.bam.inventaire.ui.theme.LightDarkPreviews
import ma.bam.inventaire.ui.theme.OnBrand
import ma.bam.inventaire.ui.theme.PreviewSurface

/** Opacité des icônes de la barre quand l'inventaire est clôturé. */
private const val DISABLED_ICON_ALPHA = 0.38f

/** Barre du haut : numéro d'inventaire, nombre d'articles et état, créer un produit, exporter. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InventoryDetailTopBar(
    session: InventorySessionEntity?,
    totalCount: Int,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onExport: () -> Unit
) {
    val tapHaptic = rememberTapHaptic()
    val locked = session?.verrouille == true
    TopAppBar(
        title = { TopBarTitle(session = session, totalCount = totalCount, locked = locked) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        ),
        navigationIcon = {
            IconButton(onClick = {
                tapHaptic()
                onBack()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
            }
        },
        actions = {
            IconButton(
                onClick = {
                    tapHaptic()
                    onCreate()
                },
                enabled = !locked
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_product),
                    contentDescription = stringResource(R.string.detail_create_product),
                    modifier = Modifier
                        .size(26.dp)
                        .alpha(if (locked) DISABLED_ICON_ALPHA else 1f),
                    tint = Color.Unspecified
                )
            }
            IconButton(onClick = {
                tapHaptic()
                onExport()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_export),
                    contentDescription = stringResource(R.string.detail_export),
                    modifier = Modifier.size(26.dp),
                    tint = Color.Unspecified
                )
            }
        }
    )
}

@Composable
private fun TopBarTitle(session: InventorySessionEntity?, totalCount: Int, locked: Boolean) {
    Column {
        Text(
            session?.numero ?: stringResource(R.string.detail_title_fallback),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BamNavy,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (session != null) {
            Text(
                stringResource(
                    R.string.detail_subtitle,
                    pluralLabel(R.plurals.articles_count, totalCount),
                    stringResource(if (locked) R.string.detail_state_locked else R.string.detail_state_open)
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/** Bouton flottant « Scanner », centré en bas de la page produits. */
@Composable
internal fun ScanButton(enabled: Boolean, onClick: () -> Unit) {
    val tapHaptic = rememberTapHaptic()
    Button(
        onClick = {
            tapHaptic()
            onClick()
        },
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BamNavyFill, contentColor = OnBrand),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 8.dp),
        contentPadding = PaddingValues(horizontal = 28.dp),
        modifier = Modifier.height(58.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_barcode_scan),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(stringResource(R.string.detail_scan), fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@LightDarkPreviews
@Composable
private fun InventoryDetailTopBarPreview() {
    PreviewSurface {
        InventoryDetailTopBar(
            session = PreviewArticles.session,
            totalCount = PreviewArticles.all.size,
            onBack = {},
            onCreate = {},
            onExport = {}
        )
    }
}

@LightDarkPreviews
@Composable
private fun ScanButtonPreview() {
    PreviewSurface {
        Column {
            ScanButton(enabled = true, onClick = {})
            ScanButton(enabled = false, onClick = {})
        }
    }
}
