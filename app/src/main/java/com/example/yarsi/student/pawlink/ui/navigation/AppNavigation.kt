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
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yarsi.student.pawlink.viewmodel.AuthViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.yarsi.student.pawlink.ui.detail.DetailHewanScreen
import com.example.yarsi.student.pawlink.ui.posting.PostingHewanScreen
import androidx.compose.runtime.getValue
import com.example.yarsi.student.pawlink.ui.profil.ProfilScreen
import com.example.yarsi.student.pawlink.viewmodel.HewanViewModel

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
    val hewanViewModel: HewanViewModel = viewModel()
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
                authViewModel.fetchCurrentUser()
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

        composable(Routes.DETAIL_HEWAN + "/{hewanId}") { backStackEntry ->
            val hewanId = backStackEntry.arguments?.getString("hewanId")
            DetailHewanScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.POSTING_HEWAN) {
            val userRole by authViewModel.userRole.collectAsState()

            PostingHewanScreen(
                userRole = userRole,
                hewanViewModel = hewanViewModel,
                onBack = { navController.popBackStack() },
                onPostingSuccess = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFIL) {
            val userName by authViewModel.userName.collectAsState()
            val userRole by authViewModel.userRole.collectAsState()
            val userEmail by authViewModel.userEmail.collectAsState()

            ProfilScreen(
                nama = userName,
                email = userEmail,
                role = userRole,
                onBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onSaveProfile = { nama, noHp, kota ->
                    // TODO: update ke Appwrite nanti
                }
            )
        }
    }
}