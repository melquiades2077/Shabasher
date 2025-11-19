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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shabasher.Model.Routes
import com.example.shabasher.Model.ScreenConfig
import com.example.shabasher.Screens.LoginPage
import com.example.shabasher.Screens.MainPage
import com.example.shabasher.Screens.NamePage
import com.example.shabasher.Screens.RegisterPage
import com.example.shabasher.Screens.WelcomePage
import com.example.shabasher.ui.theme.ShabasherTheme
import com.example.shabasher.ViewModels.LoginViewModel
import com.example.shabasher.ViewModels.NameViewModel
import com.example.shabasher.ViewModels.RegisterViewModel
import com.example.shabasher.ViewModels.ThemeViewModel


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val navController = rememberNavController()
            val screenConfig = remember { mutableStateOf(ScreenConfig()) }

            ShabasherTheme(darkTheme = themeViewModel.isDarkTheme.value) {

                Scaffold(
                    modifier = Modifier.Companion.fillMaxSize(),
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

                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                    },
                    floatingActionButton = {
                        if (screenConfig.value.showFab) {
                            FloatingActionButton(
                                onClick = { screenConfig.value.fabAction?.invoke() },
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "FAB")
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.WELCOME,
                        modifier = Modifier.Companion.padding(innerPadding)
                    ) {

                        composable(Routes.WELCOME) {
                            screenConfig.value = ScreenConfig(
                                title = "",
                                showTopBar = false,
                                showBottomBar = false,
                                showFab = false
                            )
                            WelcomePage(navController)
                        }

                        composable(Routes.REGISTER) {
                            val vm: RegisterViewModel = viewModel()
                            screenConfig.value = ScreenConfig(
                                title = "Регистрация",
                                showTopBar = true,
                                showFab = true,
                                fabAction = {
                                    if (vm.validate()) navController.navigate(Routes.NAME)
                                }
                            )

                            RegisterPage(
                                navController = navController,
                                onRegisterSuccess = { navController.navigate(Routes.NAME) },
                                viewModel = vm
                            )
                        }

                        composable(Routes.LOGIN) {
                            val vm: LoginViewModel = viewModel()
                            screenConfig.value = ScreenConfig(
                                title = "Вход",
                                showTopBar = true,
                                showFab = true,
                                fabAction = {
                                    if (vm.validate()) {
                                        navController.navigate(Routes.MAIN) {
                                            popUpTo(0) { inclusive = true }
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
                                viewModel = vm
                            )
                        }

                        composable(Routes.MAIN) {

                            screenConfig.value = ScreenConfig(
                                title = "Главная",
                                showTopBar = false,
                                showFab = false
                            )
                            MainPage(navController, themeViewModel)
                        }

                        composable(Routes.NAME) {
                            val vm: NameViewModel = viewModel()
                            screenConfig.value = ScreenConfig(
                                title = "Создать профиль",
                                showTopBar = true,
                                showFab = true,
                                fabAction = {
                                    if (vm.validate()) {
                                        navController.navigate(Routes.MAIN) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                }
                            )

                            NamePage(
                                navController = navController,
                                onNameSuccess = {
                                    navController.navigate(Routes.MAIN) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                viewModel = vm
                            )
                        }
                    }
                }
            }
        }
    }
}