package com.example.yarsi.student.pawlink.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.yarsi.student.pawlink.ui.dashboard.DashboardScreen
import com.example.yarsi.student.pawlink.ui.login.ForgotPasswordScreen
import com.example.yarsi.student.pawlink.ui.login.LoginScreen
import com.example.yarsi.student.pawlink.ui.register.RegisterScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yarsi.student.pawlink.viewmodel.AuthViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument

// Route constants

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val DASHBOARD = "dashboard"
    const val DETAIL_HEWAN    = "detail_hewan"
    const val POSTING_HEWAN   = "posting_hewan"
    const val NOTIFIKASI      = "notifikasi"
    const val PROFIL          = "profil"
}

// Nav graph

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN

) {
    val authViewModel: AuthViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onNavigateToForgetPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.DASHBOARD) {
                        // Hapus Login dari back stack agar tombol Back tidak kembali ke Login
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        // Hapus seluruh auth flow dari back stack
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = {
                    navController.popBackStack()
                },
                onOtpSent = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.DASHBOARD) {
            LaunchedEffect(Unit) {
                authViewModel.refreshUserData()
            }
            DashboardScreen(
                authViewModel = authViewModel,
                onHewanClick = { hewan_Id ->
                    navController.navigate(Routes.DETAIL_HEWAN + "/$hewan_Id")
                },
                onPostingClick = {
                    navController.navigate(Routes.POSTING_HEWAN)
                },
                onNotifikasiClick = {
                    navController.navigate(Routes.NOTIFIKASI)
                },
                onProfilClick = {
                    navController.navigate(Routes.PROFIL)
                }
            )
        }
    }
}