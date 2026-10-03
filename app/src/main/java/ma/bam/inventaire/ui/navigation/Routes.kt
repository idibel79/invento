package ma.bam.inventaire.ui.navigation

object Routes {
    const val HOME = "home"
    const val IMPORT = "import"
    const val SCAN = "scan/{sessionId}"
    const val INVENTORY_DETAIL = "inventory_detail/{sessionId}"

    fun scan(sessionId: String) = "scan/$sessionId"
    fun inventoryDetail(sessionId: String) = "inventory_detail/$sessionId"
}
