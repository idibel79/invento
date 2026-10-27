# Règles R8 de la build release (isMinifyEnabled + isShrinkResources).
#
# Room, Hilt/Dagger, ML Kit (barcode-scanning), CameraX, Compose, kotlinx.coroutines et
# Accompanist fournissent leurs propres règles (consumer rules) dans leurs AAR. Seuls les cas
# propres à l'app sont listés ici.

# Garder fichiers et numéros de ligne dans les traces de plantage (noms de classes obfusqués).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room : la base est instanciée par réflexion (AppDatabase_Impl, généré). Déjà couvert par les
# règles de Room ; rappelé ici car une base introuvable ferait planter l'app au démarrage.
-keep class * extends androidx.room.RoomDatabase { <init>(); }

# Statut d'inventaire stocké en base sous son nom (Converters : name / valueOf) : les noms des
# constantes ne doivent jamais changer.
-keepclassmembers enum ma.bam.inventaire.data.local.entity.InventoryStatus { *; }

# Import / export .xlsx : implémentation maison (ma.bam.inventaire.util.xlsx) sur java.util.zip
# et le parseur SAX de la plateforme (javax.xml.parsers, org.xml.sax) : pas de réflexion, rien à
# garder. Les feuilles sont lues par nom de colonne (ExcelColumnMapper), pas par nom de champ.
