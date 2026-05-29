package com.example.yarsi.student.pawlink.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.yarsi.student.pawlink.ui.dashboard.DashboardScreen
import com.example.yarsi.student.pawlink.ui.login.ForgotPasswordScreen
import com.example.yarsi.student.pawlink.ui.login.LoginScreen
import com.example.yarsi.student.pawlink.ui.register.RegisterScreen

// Route constants

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val DASHBOARD = "dashboard"
}

// Nav graph

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onNavigateToForgetPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        // Hapus Login dari back stack agar tombol Back tidak kembali ke Login
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
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
                    // TODO: navigasi ke OtpScreen saat sudah dibuat
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                userName = "Anisa",  // TODO: ganti dengan data user dari session/ViewModel
                onHewanClick = { hewan_Id ->
                    // TODO: navController.navigate(Routes.DETAIL_HEWAN + "/$hewanId")
                },
                onPostingClick = {
                    // TODO: navController.navigate(Routes.POSTING_HEWAN)
                },
                onNotifikasiClick = {
                    // TODO: navController.navigate(Routes.NOTIFIKASI)
                },
                onProfilClick = {
                    // TODO: navController.navigate(Routes.PROFIL)
                }
            )
        }
    }
}