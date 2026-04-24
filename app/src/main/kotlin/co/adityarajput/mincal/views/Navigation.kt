package co.adityarajput.mincal.views

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import co.adityarajput.mincal.views.screens.AboutScreen
import co.adityarajput.mincal.views.screens.HomeScreen
import co.adityarajput.mincal.views.screens.LicensesScreen

@Composable
fun Navigator(controller: NavHostController) {
    NavHost(controller, Routes.HOME.name) {
        composable(Routes.HOME.name) { HomeScreen({ controller.navigate(Routes.ABOUT.name) }) }
        composable(Routes.ABOUT.name) {
            AboutScreen(controller::popBackStack) { controller.navigate(Routes.LICENSES.name) }
        }
        composable(Routes.LICENSES.name) { LicensesScreen(controller::popBackStack) }
    }
}

enum class Routes { HOME, ABOUT, LICENSES }
