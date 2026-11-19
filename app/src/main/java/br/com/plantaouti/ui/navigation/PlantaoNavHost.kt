package br.com.plantaouti.ui.navigation

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.plantaouti.ui.patients.PatientsScreen
import br.com.plantaouti.ui.settings.SettingsScreen
import br.com.plantaouti.ui.shift.ShiftWorkspaceScreen

object Routes {
    const val PATIENTS = "patients"
    const val SHIFT = "shift/{shiftId}"
    const val SETTINGS = "settings"

    fun shiftRoute(shiftId: Long) = "shift/$shiftId"
}

@Composable
fun PlantaoNavHost(windowWidthSizeClass: WindowWidthSizeClass) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.PATIENTS) {
        composable(Routes.PATIENTS) {
            PatientsScreen(
                onOpenShift = { id -> navController.navigate(Routes.shiftRoute(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(
            route = Routes.SHIFT,
            arguments = listOf(navArgument("shiftId") { type = NavType.LongType })
        ) {
            ShiftWorkspaceScreen(
                windowWidthSizeClass = windowWidthSizeClass,
                onBack = { navController.popBackStack() },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
