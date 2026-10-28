package ma.bam.inventaire.data.local

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Garde-fou JVM : changer [AppDatabase.VERSION] sans ajouter la migration ni exporter le schéma
 * fait échouer ce test (et donc la CI), au lieu de planter à l'ouverture de la base sur le téléphone.
 */
class DatabaseMigrationsTest {

    // Les tests unitaires Gradle s'exécutent depuis le dossier du module (app/).
    private val schemaDir = File("schemas/${AppDatabase::class.java.name}")

    @Test
    fun exported_schema_exists_for_every_version_since_oldest_with_schema() {
        (OLDEST_SCHEMA_VERSION..AppDatabase.VERSION).forEach { version ->
            val schema = File(schemaDir, "$version.json")
            assertTrue("Schéma exporté manquant : ${schema.path}", schema.isFile)
            assertTrue(
                "${schema.name} ne déclare pas la version $version",
                schema.readText().contains("\"version\": $version,")
            )
        }
    }

    @Test
    fun migrations_cover_every_step_up_to_current_version() {
        val steps = AppDatabase.ALL_MIGRATIONS.map { it.startVersion to it.endVersion }

        assertEquals("Migration en double", steps.size, steps.toSet().size)
        (OLDEST_MIGRATABLE_VERSION until AppDatabase.VERSION).forEach { from ->
            assertTrue(
                "Migration manquante $from -> ${from + 1} dans AppDatabase.ALL_MIGRATIONS",
                (from to from + 1) in steps
            )
        }
        steps.forEach { (from, to) ->
            assertTrue("Migration $from -> $to au-delà de la version courante", to <= AppDatabase.VERSION)
        }
    }

    private companion object {
        /** Plus ancienne version installée qu'on sait migrer (aucun téléphone n'est antérieur). */
        const val OLDEST_MIGRATABLE_VERSION = 5

        /** Premier schéma exporté (5.json n'a jamais été généré). */
        const val OLDEST_SCHEMA_VERSION = 6
    }
}
