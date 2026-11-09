package com.npplayer.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.npplayer.core.ui.component.NPRationaleScreen
import com.npplayer.feature.library.music.MusicHomeScreen
import com.npplayer.feature.library.ui.FolderDetailScreen
import com.npplayer.feature.library.ui.HomeScreen
import com.npplayer.feature.library.ui.PlaylistsScreen
import com.npplayer.feature.library.ui.SearchScreen
import com.npplayer.feature.network.ui.NetworkHubScreen
import com.npplayer.feature.privatevault.ui.PrivateVaultScreen
import com.npplayer.feature.settings.ui.AdvancedSettingsScreen
import com.npplayer.feature.settings.ui.ControlsEditorScreen
import com.npplayer.feature.settings.ui.HelpFaqScreen
import com.npplayer.feature.settings.ui.LegalAboutScreen
import com.npplayer.feature.settings.ui.MediaManagerScreen
import com.npplayer.feature.settings.ui.MenuScreen
import com.npplayer.feature.settings.ui.SeekBarStylesScreen
import com.npplayer.feature.settings.ui.SettingsScreen
import com.npplayer.player.ui.PlayerScreen

sealed class Screen(val route: String) {
    data object Rationale : Screen("rationale")
    data object Home : Screen("home")
    data object Menu : Screen("menu")
    data object Settings : Screen("settings")
    data object ControlsEditor : Screen("controls_editor")
    data object SeekBarStyles : Screen("seek_bar_styles")
    data object AdvancedSettings : Screen("advanced_settings")
    data object NetworkHub : Screen("network_hub")
    data object PrivateVault : Screen("private_vault")
    data object Playlists : Screen("playlists")
    data object MediaManager : Screen("media_manager")
    data object HelpFaq : Screen("help_faq")
    data object LegalAbout : Screen("legal_about")
    data object Search : Screen("search")
    data object Music : Screen("music")

    data object FolderDetail : Screen("folder_detail?folderPath={folderPath}&folderName={folderName}") {
        fun createRoute(folderPath: String, folderName: String): String {
            val encodedPath = Uri.encode(folderPath)
            val encodedName = Uri.encode(folderName)
            return "folder_detail?folderPath=$encodedPath&folderName=$encodedName"
        }
    }

    data object Player : Screen("player?videoPath={videoPath}&videoTitle={videoTitle}&isVault={isVault}") {
        fun createRoute(videoPath: String, videoTitle: String, isVault: Boolean = false): String {
            val encodedPath = Uri.encode(videoPath)
            val encodedTitle = Uri.encode(videoTitle)
            return "player?videoPath=$encodedPath&videoTitle=$encodedTitle&isVault=$isVault"
        }
    }
}

@Composable
fun NPPlayerNavHost(
    navController: NavHostController,
    hasStoragePermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val startDestination = if (hasStoragePermission) Screen.Home.route else Screen.Rationale.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Rationale.route) {
            NPRationaleScreen(
                onRequestPermission = onRequestPermission
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onFolderClick = { path, name ->
                    navController.navigate(Screen.FolderDetail.createRoute(path, name))
                },
                onMenuClick = {
                    navController.navigate(Screen.Menu.route)
                },
                onResumeClick = { path, title ->
                    navController.navigate(Screen.Player.createRoute(path, title))
                },
                onPrivateClick = {
                    navController.navigate(Screen.PrivateVault.route)
                },
                onNetworkClick = {
                    navController.navigate(Screen.NetworkHub.route)
                },
                onPlaylistsClick = {
                    navController.navigate(Screen.Playlists.route)
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                },
                onMusicClick = {
                    navController.navigate(Screen.Music.route)
                }
            )
        }

        composable(Screen.Music.route) {
            MusicHomeScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.FolderDetail.route,
            arguments = listOf(
                navArgument("folderPath") { type = NavType.StringType; defaultValue = "" },
                navArgument("folderName") { type = NavType.StringType; defaultValue = "Folder" }
            )
        ) {
            FolderDetailScreen(
                onBack = { navController.popBackStack() },
                onVideoClick = { path, title ->
                    navController.navigate(Screen.Player.createRoute(path, title))
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onFolderClick = { path, name ->
                    navController.navigate(Screen.FolderDetail.createRoute(path, name))
                },
                onPlayVideo = { path, title ->
                    navController.navigate(Screen.Player.createRoute(path, title))
                }
            )
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("videoPath") { type = NavType.StringType; defaultValue = "" },
                navArgument("videoTitle") { type = NavType.StringType; defaultValue = "" },
                navArgument("isVault") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("videoPath") ?: ""
            val title = backStackEntry.arguments?.getString("videoTitle") ?: ""
            val isVault = backStackEntry.arguments?.getBoolean("isVault") ?: false
            PlayerScreen(
                videoPath = path,
                videoTitle = title,
                isVault = isVault,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Menu.route) {
            MenuScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToPrivate = { navController.navigate(Screen.PrivateVault.route) },
                onNavigateToNetwork = { navController.navigate(Screen.NetworkHub.route) },
                onNavigateToPlaylists = { navController.navigate(Screen.Playlists.route) },
                onNavigateToMediaManager = { navController.navigate(Screen.MediaManager.route) },
                onNavigateToHelpFaq = { navController.navigate(Screen.HelpFaq.route) },
                onNavigateToLegalAbout = { navController.navigate(Screen.LegalAbout.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToControlsEditor = { navController.navigate(Screen.ControlsEditor.route) },
                onNavigateToSeekBarStyles = { navController.navigate(Screen.SeekBarStyles.route) },
                onNavigateToAdvancedSettings = { navController.navigate(Screen.AdvancedSettings.route) }
            )
        }

        composable(Screen.ControlsEditor.route) {
            ControlsEditorScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SeekBarStyles.route) {
            SeekBarStylesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdvancedSettings.route) {
            AdvancedSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NetworkHub.route) {
            NetworkHubScreen(
                onBack = { navController.popBackStack() },
                onPlayStream = { url, title ->
                    navController.navigate(Screen.Player.createRoute(url, title))
                }
            )
        }

        composable(Screen.PrivateVault.route) {
            PrivateVaultScreen(
                onBack = { navController.popBackStack() },
                onPlayVideo = { path, title ->
                    navController.navigate(Screen.Player.createRoute(path, title, isVault = true))
                }
            )
        }

        composable(Screen.Playlists.route) {
            PlaylistsScreen(
                onBack = { navController.popBackStack() },
                onPlayVideo = { path, title ->
                    navController.navigate(Screen.Player.createRoute(path, title))
                }
            )
        }

        composable(Screen.MediaManager.route) {
            MediaManagerScreen(
                onBack = { navController.popBackStack() },
                onPlayVideo = { path, title ->
                    navController.navigate(Screen.Player.createRoute(path, title))
                }
            )
        }

        composable(Screen.HelpFaq.route) {
            HelpFaqScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LegalAbout.route) {
            LegalAboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
