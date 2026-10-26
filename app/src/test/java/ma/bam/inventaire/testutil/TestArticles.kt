package ma.bam.inventaire.testutil

import ma.bam.inventaire.data.local.entity.InventorySessionEntity
import ma.bam.inventaire.data.local.entity.InventoryStatus
import ma.bam.inventaire.data.local.entity.StockArticleEntity

/** Article de test avec des valeurs par défaut neutres ; seuls les champs utiles au test sont précisés. */
@Suppress("LongParameterList")
fun testArticle(
    id: Long = 0,
    codeArticle: String = "PID$id",
    designation: String = "Produit $id",
    categorie: String = "",
    emplacement: String = "",
    quantiteTheorique: Double = 10.0,
    codeBarre1: String = "",
    codeBarre2: String = "",
    prixUnitaire: Double = 0.0,
    quantiteReelle: Double? = null,
    ecart: Double? = null,
    ecartValide: Boolean = false,
    corrige: Boolean = false
) = StockArticleEntity(
    id = id,
    sessionId = "s1",
    codeArticle = codeArticle,
    designation = designation,
    categorie = categorie,
    tva = "",
    emplacement = emplacement,
    quantiteTheorique = quantiteTheorique,
    datePeremption = "",
    stockMin = 0.0,
    stockMax = 0.0,
    codeBarre1 = codeBarre1,
    codeBarre2 = codeBarre2,
    prixUnitaire = prixUnitaire,
    pph = 0.0,
    dateImport = 0L,
    quantiteReelle = quantiteReelle,
    ecart = ecart,
    ecartValide = ecartValide,
    corrige = corrige
)

/** Session de test, en cours et non verrouillée. */
fun testSession(id: String = "s1", nomFichierSource: String = "inventaire.xlsx") = InventorySessionEntity(
    id = id,
    numero = "INV-TEST-$id",
    dateCreation = 0L,
    dateFinalisation = null,
    statut = InventoryStatus.EN_COURS,
    nomFichierSource = nomFichierSource
)
