package com.example.shabasher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.shabasher.ui.theme.ShabasherTheme
import androidx.navigation.compose.composable
import com.example.shabasher.Model.Routes
import com.example.shabasher.Model.ScreenConfig
import com.example.shabasher.ViewModels.LoginViewModel
import com.example.shabasher.ViewModels.RegisterViewModel
import com.example.shabasher.Views.LoginPage
import com.example.shabasher.Views.MainPage
import com.example.shabasher.Views.RegisterPage
import com.example.shabasher.Views.WelcomePage

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShabasherTheme {
                val navController = rememberNavController()
                val screenConfig = remember { mutableStateOf(ScreenConfig()) }


                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (screenConfig.value.showTopBar) {
                            CenterAlignedTopAppBar(
                                title = { Text(screenConfig.value.title ?: "") },
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            navController.navigate(Routes.WELCOME) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                                    }
                                }
                            )
                        }
                    },
                    floatingActionButton = {
                        if (screenConfig.value.showFab) {
                            FloatingActionButton(onClick = { screenConfig.value.fabAction?.invoke() }, shape = CircleShape) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "FAB")
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.WELCOME,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Routes.WELCOME) {
                            screenConfig.value = ScreenConfig(
                                title = "",
                                showTopBar = false,
                                showBottomBar = false,
                                showFab = false,
                                fabAction = { }
                            )
                            WelcomePage(navController)

                        }

                        composable(Routes.REGISTER) {
                            val vm: RegisterViewModel = viewModel()

                            screenConfig.value = ScreenConfig(
                                title = "Регистрация",
                                showTopBar = true,
                                showBottomBar = false,
                                showFab = true,
                                fabAction = {
                                    if (vm.validate()) {
                                        navController.navigate(Routes.MAIN) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                }
                            )
                            RegisterPage(
                                navController = navController,
                                onRegisterSuccess = {
                                    navController.navigate(Routes.MAIN) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                viewModel = vm)
                        }

                        composable(Routes.LOGIN) {
                            val vm: LoginViewModel = viewModel()

                            screenConfig.value = ScreenConfig(
                                title = "Вход",
                                showTopBar = true,
                                showBottomBar = false,
                                showFab = true,
                                fabAction = {
                                    if (vm.validate()) {
                                        navController.navigate(Routes.MAIN) {
                                            popUpTo(0) {inclusive = true}
                                        }
                                    }
                                }
                            )
                            LoginPage(
                                navController = navController,
                                onLoginSuccess = {
                                    navController.navigate(Routes.MAIN) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                viewModel = vm)
                        }

                        composable(Routes.MAIN) {
                            screenConfig.value = ScreenConfig(
                                title = "Главная страница",
                                showTopBar = false,
                                showBottomBar = false,
                                showFab = false,
                                fabAction = { }
                            )
                            MainPage(navController)
                        }
                    }
                }
            }
        }
    }
}
