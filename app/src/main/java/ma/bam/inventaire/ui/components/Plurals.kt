package ma.bam.inventaire.ui.components

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.pluralStringResource

/**
 * Libellé pluriel avec la règle française de l'app : singulier pour 0 et 1, pluriel au-delà.
 * `pluralStringResource` seul suivrait la langue du téléphone (en anglais, 0 passerait au
 * pluriel) : on lui passe donc 1 ou 2 pour choisir la forme, puis le vrai nombre à afficher.
 * Le nombre est passé en texte pour garder les chiffres latins quelle que soit la langue.
 */
@Composable
@ReadOnlyComposable
fun pluralLabel(@PluralsRes id: Int, count: Int): String = pluralStringResource(id, if (count > 1) 2 else 1, count.toString())
