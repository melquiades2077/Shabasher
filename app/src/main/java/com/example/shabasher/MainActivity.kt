package com.example.shabasher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.BottomAppBar
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.shabasher.ui.theme.ShabasherTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.shabasher.Model.Routes
import com.example.shabasher.Model.ScreenConfig
import com.example.shabasher.Pages.LoginPage
import com.example.shabasher.Pages.RegisterPage
import com.example.shabasher.Pages.WelcomePage

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
                                    IconButton(onClick = { navController.navigate(Routes.WELCOME) }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                                    }
                                }
                            )
                        }
                    },
                    floatingActionButton = {
                        if (screenConfig.value.showFab) {
                            FloatingActionButton(onClick = { screenConfig.value.fabAction?.invoke() }) {
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
                        composable(route = Routes.WELCOME) {
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
                            screenConfig.value = ScreenConfig(
                                title = "Регистрация",
                                showTopBar = true,
                                showBottomBar = false,
                                showFab = true,
                                fabAction = { navController.navigate(Routes.WELCOME) }
                            )
                            RegisterPage(navController)
                        }

                        composable(Routes.LOGIN) {
                            screenConfig.value = ScreenConfig(
                                title = "Вход",
                                showTopBar = true,
                                showBottomBar = false,
                                showFab = true,
                                fabAction = { navController.navigate(Routes.WELCOME) }
                            )
                            RegisterPage(navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ShabasherTheme {
        Greeting("Android")
    }
}