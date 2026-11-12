package me.razmeni

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.razmeni.data.AuthRepo
import me.razmeni.ui.auth.LoginScreen
import me.razmeni.ui.auth.RegisterScreen
import me.razmeni.ui.exchange.AddExchangeScreen
import me.razmeni.ui.exchange.ExchangeDetailsScreen
import me.razmeni.ui.main.MainScreen
import me.razmeni.ui.main.MainViewModel

@Composable
fun RazmeniApp() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()
    val startDestination = remember { if (AuthRepo.currentUid != null) "main" else "login" }

    fun goAndClearBackStack(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLoggedIn = { goAndClearBackStack("main") },
                onRegisterClick = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegistered = { goAndClearBackStack("main") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("main") {
            MainScreen(
                vm = mainViewModel,
                onAddClick = { navController.navigate("add") },
                onExchangeClick = { id -> navController.navigate("details/$id") },
                onLogout = {
                    mainViewModel.logout()
                    goAndClearBackStack("login")
                }
            )
        }
        composable("add") {
            AddExchangeScreen(
                vm = mainViewModel,
                // ako je korisnik već izašao sa ekrana dok čuvanje traje, ne vraćamo se dodatno nazad
                onBack = { navController.popBackStack("add", inclusive = true) }
            )
        }
        composable("details/{id}") { entry ->
            ExchangeDetailsScreen(
                id = entry.arguments?.getString("id").orEmpty(),
                vm = mainViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
