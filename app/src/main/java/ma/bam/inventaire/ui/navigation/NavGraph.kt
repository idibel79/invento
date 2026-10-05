package ma.bam.inventaire.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ma.bam.inventaire.ui.home.HomeScreen
import ma.bam.inventaire.ui.importinventory.ImportScreen
import ma.bam.inventaire.ui.inventorydetail.InventoryDetailScreen
import ma.bam.inventaire.ui.scan.ScanScreen

@Composable
fun InventaireNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onStartInventory = { navController.navigate(Routes.IMPORT) },
                onOpenSession = { sessionId ->
                    navController.navigate(Routes.inventoryDetail(sessionId))
                }
            )
        }
        composable(Routes.IMPORT) {
            ImportScreen(
                onBack = { navController.popBackStack() },
                onSessionReady = { sessionId ->
                    navController.navigate(Routes.scan(sessionId)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(
            route = Routes.SCAN,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) {
            ScanScreen(
                onBack = { navController.popBackStack() },
                onFinished = { sessionId ->
                    navController.navigate(Routes.inventoryDetail(sessionId)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(
            route = Routes.INVENTORY_DETAIL,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) {
            InventoryDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
