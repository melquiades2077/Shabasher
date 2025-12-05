package com.example.shabasher

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import com.example.shabasher.ViewModels.MainPageViewModel
import com.example.shabasher.ViewModels.NameViewModel
import com.example.shabasher.ViewModels.ProfileViewModel
import com.example.shabasher.ViewModels.RegisterViewModel
import com.example.shabasher.ViewModels.ShareEventViewModel
import com.example.shabasher.ViewModels.ShareEventViewModelFactory
import com.example.shabasher.ViewModels.ThemeViewModel
import com.example.shabasher.ViewModels.ViewModelFactory
import com.example.shabasher.data.local.TokenManager
import com.example.shabasher.data.network.InviteRepository
import com.example.shabasher.ui.theme.ShabasherTheme
import androidx.lifecycle.ViewModelProvider
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.shabasher.ViewModels.ShareEventViewModelFactory
import com.example.shabasher.data.network.InviteRepository
import com.example.shabasher.data.network.EventsRepository

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
            val context = LocalContext.current
            val themeViewModel: ThemeViewModel = viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory(application)
            )
            val viewModelFactory = rememberViewModelFactory(context)

            val navController = rememberNavController()

            val tokenManager = remember { TokenManager(context) }
            val startDestination = if (tokenManager.getToken() != null) Routes.MAIN else Routes.WELCOME

            ShabasherTheme(darkTheme = themeViewModel.isDarkTheme.value) {
                // Handle Deep Link navigation
                DeepLinkHandler(navController)

                // Реализация NavigationGraph напрямую здесь
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Routes.WELCOME) { WelcomePage(navController) }

                    composable(Routes.REGISTER) {
                        val vm: RegisterViewModel = viewModel(factory = viewModelFactory)
                        RegisterPage(navController, vm)
                    }

                    composable(Routes.LOGIN) {
                        val vm: LoginViewModel = viewModel(factory = viewModelFactory)
                        LoginPage(navController, vm)
                    }

                    composable(
                        route = "namePage?email={email}&password={password}",
                        arguments = listOf(
                            navArgument("email") { defaultValue = "" },
                            navArgument("password") { defaultValue = "" }
                        )
                    ) { backStackEntry ->
                        NamePage(
                            navController,
                            email = backStackEntry.arguments?.getString("email") ?: "",
                            password = backStackEntry.arguments?.getString("password") ?: ""
                        )
                    }

                    composable(Routes.MAIN) {
                        val vm: MainPageViewModel = viewModel(factory = viewModelFactory)
                        MainPage(navController, vm)
                    }

                    composable(Routes.PROFILE) {
                        val vm: ProfileViewModel = viewModel(factory = viewModelFactory)
                        ProfilePage(navController, themeViewModel, vm)
                    }

                    composable(
                        route = "${Routes.EVENT}/{eventId}",
                        arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val vm: EventViewModel = viewModel(factory = viewModelFactory)
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                        EventPage(navController, eventId, vm)
                    }

                    composable(Routes.CREATEEVENT) {
                        val vm: CreateEventViewModel = viewModel(factory = viewModelFactory)
                        CreateEventPage(navController = navController, viewModel = vm)
                    }

                    composable(
                        route = "${Routes.SHAREEVENT}/{eventId}",
                        arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                        val inviteRepository = InviteRepository(context = LocalContext.current)
                        val vm: ShareEventViewModel = viewModel(
                            factory = ShareEventViewModelFactory(inviteRepository)
                        )
                        ShareEventPage(navController = navController, viewModel = vm, eventId = eventId)
                    }

                    composable(
                        route = "${Routes.PARTICIPANTS}/{eventId}",
                        arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                        ParticipantsPage(navController, eventId = eventId)
                    }
                }
            }
        }
    }
}

@Composable
fun DeepLinkHandler(navController: NavController) {
    val context = LocalContext.current

    LaunchedEffect(context) {
        val intent = (context as? ComponentActivity)?.intent
        val uri = intent?.data
        uri?.let {
            if (it.scheme == "shabasher" && it.host == "event") {
                val eventId = it.getQueryParameter("eventId")
                eventId?.let { id ->
                    addParticipantViaDeepLink(context, eventId)

                    navController.navigate("${Routes.EVENT}/$id") {
                        popUpTo(Routes.MAIN) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

