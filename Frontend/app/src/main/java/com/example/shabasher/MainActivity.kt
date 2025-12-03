package com.example.shabasher

import android.content.Context
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.shabasher.ViewModels.LoginViewModel
import com.example.shabasher.ViewModels.NameViewModel
import com.example.shabasher.ViewModels.ProfileViewModel
import com.example.shabasher.ViewModels.RegisterViewModel
import com.example.shabasher.ViewModels.ShareEventViewModel
import com.example.shabasher.ViewModels.ThemeViewModel
import com.example.shabasher.ui.theme.ShabasherTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(context) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(context) as T
            }
            modelClass.isAssignableFrom(NameViewModel::class.java) -> {
                NameViewModel(context) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(context) as T
            }
            modelClass.isAssignableFrom(CreateEventViewModel::class.java) -> {
                CreateEventViewModel() as T
            }
            modelClass.isAssignableFrom(EventViewModel::class.java) -> {
                EventViewModel() as T
            }
            modelClass.isAssignableFrom(ShareEventViewModel::class.java) -> {
                ShareEventViewModel() as T
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}

@Composable
fun rememberViewModelFactory(context: Context): ViewModelFactory {
    return remember { ViewModelFactory(context) }
}

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeViewModel: ThemeViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(application))
            val navController = rememberNavController()
            val context = LocalContext.current
            val viewModelFactory = rememberViewModelFactory(context)

            ShabasherTheme(darkTheme = themeViewModel.isDarkTheme.value) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.WELCOME
                ) {
                    composable(Routes.WELCOME) {
                        WelcomePage(navController)
                    }

                    composable(Routes.REGISTER) {
                        val vm: RegisterViewModel = viewModel(factory = viewModelFactory)
                        RegisterPage(
                            navController = navController,
                            viewModel = vm
                        )
                    }

                    composable(Routes.LOGIN) {
                        val vm: LoginViewModel = viewModel(factory = viewModelFactory)
                        LoginPage(
                            navController = navController,
                            viewModel = vm
                        )
                    }

                    composable(Routes.NAME) {
                        val vm: NameViewModel = viewModel(factory = viewModelFactory)
                        NamePage(
                            navController = navController,
                            viewModel = vm
                        )
                    }

                    composable(Routes.MAIN) {
                        MainPage(navController)
                    }

                    composable(Routes.PROFILE) {
                        val vm: ProfileViewModel = viewModel(factory = viewModelFactory)
                        ProfilePage(
                            navController = navController,
                            themeViewModel = themeViewModel,
                            viewModel = vm
                        )
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

                    composable(Routes.PARTICIPANTS) {
                        ParticipantsPage(navController, eventId = "1")
                    }
                }
            }
        }
    }
}