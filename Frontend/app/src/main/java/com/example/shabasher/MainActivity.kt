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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.example.shabasher.Screens.EditProfileScreen
import com.example.shabasher.Screens.ProfileScreen
import com.example.shabasher.Screens.ProfileViewModelFactory
import com.example.shabasher.Screens.SuggestionsScreen

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
        window.setBackgroundDrawableResource(android.R.color.transparent)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val context = LocalContext.current
            val themeViewModel: ThemeViewModel = viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory(application)
            )
            val viewModelFactory = rememberViewModelFactory(context)

            val navController = rememberNavController()

            val tokenManager = remember { TokenManager(context) }
            val startDestination =
                if (tokenManager.getToken() != null) Routes.MAIN else Routes.WELCOME

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

                    // Свой профиль (без ID)
                    composable(Routes.PROFILE) {
                        val vm: ProfileViewModel = viewModel(
                            factory = ProfileViewModelFactory(
                                LocalContext.current,
                                targetUserId = null
                            )
                        )
                        ProfileScreen(navController, themeViewModel, null)
                    }

                    // Чужой профиль (с ID)
                    composable(Routes.PROFILE_WITH_ID) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")
                        if (userId == null) {
                            // Ошибка — вернуть назад или показать ошибку
                            navController.popBackStack()
                            return@composable
                        }

                        val vm: ProfileViewModel = viewModel(
                            key = "profile_$userId", // ← важно! иначе ViewModel будет кэшироваться
                            factory = ProfileViewModelFactory(
                                LocalContext.current,
                                targetUserId = userId
                            )
                        )
                        ProfileScreen(navController, themeViewModel, userId)
                    }

                    composable(
                        route = "${Routes.EVENT}/{eventId}?inviteId={inviteId}",
                        arguments = listOf(
                            navArgument("eventId") { type = NavType.StringType },
                            navArgument("inviteId") {
                                type = NavType.StringType
                                nullable = true
                            }
                        )
                    ) { backStackEntry ->
                        val vm: EventViewModel = viewModel(factory = viewModelFactory)
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                        // inviteId больше не используется в логике
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
                        ShareEventPage(
                            navController = navController,
                            viewModel = vm,
                            eventId = eventId
                        )
                    }

                    composable(
                        route = "${Routes.PARTICIPANTS}/{eventId}",
                        arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                        ParticipantsPage(navController, eventId = eventId)
                    }

                    composable(Routes.EDIT_PROFILE) {


                        EditProfileScreen(
                            navController = navController
                        )
                    }

                    composable(Routes.SUGGESTIONS) {


                        SuggestionsScreen(navController = navController)

                    }
                }
            }
        }
    }

}

@Composable
fun DeepLinkHandler(navController: NavController) {
    val context = LocalContext.current

    // Handle Deep Link navigation
    LaunchedEffect(context) {
        val intent = (context as? ComponentActivity)?.intent
        val uri = intent?.data
        uri?.let {
            if (it.scheme == "shabasher" && it.host == "event") {
                val eventId = it.getQueryParameter("eventId")
                eventId?.let { id ->
                    // Navigate to the Event Page
                    navController.navigate("${Routes.EVENT}/$id") {
                        popUpTo(Routes.MAIN) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}