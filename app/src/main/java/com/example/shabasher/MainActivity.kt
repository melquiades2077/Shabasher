package com.example.shabasher

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shabasher.Model.Routes
import com.example.shabasher.Screens.CreateEventPage
import com.example.shabasher.Screens.EventPage
import com.example.shabasher.Screens.LoginPage
import com.example.shabasher.Screens.MainPage
import com.example.shabasher.Screens.NamePage
import com.example.shabasher.Screens.ParticipantsPage
import com.example.shabasher.Screens.ProfilePage
import com.example.shabasher.Screens.RegisterPage
import com.example.shabasher.Screens.ShareEventPage
import com.example.shabasher.Screens.WelcomePage
import com.example.shabasher.ViewModels.CreateEventViewModel
import com.example.shabasher.ViewModels.EventViewModel
import com.example.shabasher.ui.theme.ShabasherTheme
import com.example.shabasher.ViewModels.LoginViewModel
import com.example.shabasher.ViewModels.NameViewModel
import com.example.shabasher.ViewModels.RegisterViewModel
import com.example.shabasher.ViewModels.ShareEventViewModel
import com.example.shabasher.ViewModels.ThemeViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val navController = rememberNavController()

            ShabasherTheme(darkTheme = themeViewModel.isDarkTheme.value) {

                NavHost(
                    navController = navController,
                    startDestination = Routes.WELCOME


                ) {

                    composable(Routes.WELCOME) {
                        WelcomePage(navController)
                    }

                    composable(Routes.REGISTER) {
                        val vm: RegisterViewModel = viewModel()
                        RegisterPage(
                            navController = navController,
                            viewModel = vm
                        )
                    }

                    composable(Routes.LOGIN) {
                        val vm: LoginViewModel = viewModel()
                        LoginPage(
                            navController = navController,
                            viewModel = vm
                        )
                    }

                    composable(Routes.NAME) {
                        val vm: NameViewModel = viewModel()
                        NamePage(
                            navController = navController,
                            viewModel = vm
                        )
                    }

                    composable(Routes.MAIN) {
                        MainPage(navController)
                    }

                    composable(Routes.PROFILE) {
                        ProfilePage(navController, themeViewModel)
                    }

                    composable(Routes.EVENT) {
                        val vm: EventViewModel = viewModel()
                        EventPage(navController, "1", vm)
                    }

                    composable(Routes.CREATEEVENT) {
                        val vm: CreateEventViewModel = viewModel()
                        CreateEventPage(navController, vm)
                    }

                    composable(Routes.SHAREEVENT) {
                        val vm: ShareEventViewModel = viewModel()
                        ShareEventPage(navController, vm)
                    }

                    // "${Routes.PARTICIPANTS}/{eventId}"
                    composable(Routes.PARTICIPANTS) { //backStack ->
                        //val eventId = backStack.arguments?.getString("eventId")!!
                        ParticipantsPage(navController, eventId = "1")
                    }

                }
            }
        }
    }
}
