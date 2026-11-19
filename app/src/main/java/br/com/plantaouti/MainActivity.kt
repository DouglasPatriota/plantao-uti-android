package br.com.plantaouti

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.plantaouti.data.datastore.AppSettings
import br.com.plantaouti.data.datastore.SettingsDataStore
import br.com.plantaouti.ui.navigation.PlantaoNavHost
import br.com.plantaouti.ui.security.LockScreen
import br.com.plantaouti.ui.theme.PlantaoUtiTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var settingsDataStore: SettingsDataStore

    private var isUnlocked by mutableStateOf(false)
    private var lastInteractionMillis by mutableLongStateOf(System.currentTimeMillis())
    private var configuredTimeoutMinutes by mutableIntStateOf(5)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        setContent {
            val settings by settingsDataStore.settingsFlow.collectAsStateWithLifecycle(
                initialValue = AppSettings()
            )
            val windowSizeClass = calculateWindowSizeClass(this)

            LaunchedEffect(settings.inactivityTimeoutMinutes) {
                configuredTimeoutMinutes = settings.inactivityTimeoutMinutes
            }

            // Aplica ou remove FLAG_SECURE dinamicamente conforme configuração LGPD
            LaunchedEffect(settings.flagSecureEnabled) {
                if (settings.flagSecureEnabled) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            PlantaoUtiTheme(themeMode = settings.themeMode) {
                val mustLock = settings.appLockEnabled && !isUnlocked
                if (mustLock) {
                    LockScreen(
                        activity = this@MainActivity,
                        pinHash = settings.pinHash,
                        onUnlocked = {
                            isUnlocked = true
                            lastInteractionMillis = System.currentTimeMillis()
                        }
                    )
                } else {
                    PlantaoNavHost(windowWidthSizeClass = windowSizeClass.widthSizeClass)
                }
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        lastInteractionMillis = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        val elapsedMinutes = (System.currentTimeMillis() - lastInteractionMillis) / 60_000L
        if (elapsedMinutes >= configuredTimeoutMinutes) {
            isUnlocked = false
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
