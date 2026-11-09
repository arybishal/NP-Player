package com.npplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.npplayer.core.data.preferences.AppTheme
import com.npplayer.core.data.preferences.SettingsRepository
import com.npplayer.core.ui.theme.NPPlayerTheme
import com.npplayer.core.ui.theme.ThemeMode
import com.npplayer.navigation.NPPlayerNavHost
import com.npplayer.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private var hasStoragePermission by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        hasStoragePermission = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkPermission()

        setContent {
            val appTheme by settingsRepository.themeFlow.collectAsState(initial = AppTheme.FOLLOW_DEVICE)
            val themeMode = when (appTheme) {
                AppTheme.LIGHT -> ThemeMode.LIGHT
                AppTheme.DARK -> ThemeMode.DARK
                AppTheme.FOLLOW_DEVICE -> ThemeMode.FOLLOW_DEVICE
            }

            NPPlayerTheme(themeMode = themeMode) {
                val navController = rememberNavController()

                NPPlayerNavHost(
                    navController = navController,
                    hasStoragePermission = hasStoragePermission,
                    onRequestPermission = { requestStoragePermission() }
                )
            }
        }
    }

    private fun checkPermission() {
        hasStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        permissionLauncher.launch(permissions)
    }
}
