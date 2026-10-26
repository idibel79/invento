package ma.bam.inventaire.data.importfile

import java.io.InputStream

/** Fichier choisi, affiché dans la feuille d'import (taille en octets, null si inconnue). */
data class ImportFileInfo(val name: String, val sizeBytes: Long?)

/**
 * Accès au fichier choisi dans le sélecteur de documents, désigné par son Uri sous forme de texte
 * (`Uri.toString()`), pour garder les ViewModels indépendants d'Android. Appels bloquants : à faire
 * hors du thread principal.
 */
interface ImportFileSource {

    /** Nom et taille du fichier ; nom par défaut et taille inconnue si les métadonnées manquent. */
    fun describe(fileUri: String): ImportFileInfo

    /** Ouvre le contenu du fichier (à fermer par l'appelant), ou null s'il est illisible. */
    fun open(fileUri: String): InputStream?
}
