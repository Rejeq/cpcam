package com.rejeq.cpcam.ui

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.arkivanov.decompose.defaultComponentContext
import com.rejeq.cpcam.core.data.model.ThemeConfig
import com.rejeq.cpcam.core.data.repository.AppearanceRepository
import com.rejeq.cpcam.core.ui.LocalIsWindowFocused
import com.rejeq.cpcam.core.ui.theme.CpcamTheme
import com.rejeq.cpcam.core.ui.wantUseDarkMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var appearanceRepo: AppearanceRepository

    @Inject
    lateinit var rootFactory: RootComponent.Factory

    private val hasWindowFocus = MutableStateFlow(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            hasWindowFocus.value = it.getBoolean(KEY_WINDOW_FOCUS, true)
        }

        val component = rootFactory.create(
            defaultComponentContext(),
            Dispatchers.Main.immediate,
        )

        splashScreen.setKeepOnScreenCondition { !component.readyToShow() }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            fixAndroid10ActivityLeak(component)
        }

        setContent {
            // TODO: Entire app will be recomposed several times if
            //  appData == null and the user has custom theme that differ with
            //  ThemeConfig.FOLLOW_SYSTEM
            //  Maybe wait actual appData somehow?
            val useDarkMode = appearanceRepo.themeConfig
                .collectAsState(ThemeConfig.FOLLOW_SYSTEM)
            val useDynamicColor = appearanceRepo.useDynamicColor
                .collectAsState(false)

            val wantUseDarkMode = wantUseDarkMode(useDarkMode.value)

            CpcamTheme(wantUseDarkMode, useDynamicColor.value) {
                LaunchedEffect(wantUseDarkMode) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            Color.TRANSPARENT,
                            Color.TRANSPARENT,
                        ) { wantUseDarkMode },

                        // Used SystemBarStyle.dark() here to force setting
                        //   window.isNavigationBarContrastEnforced = false
                        // Thus, navigation will be completely transparent
                        navigationBarStyle = SystemBarStyle.dark(
                            Color.TRANSPARENT,
                        ),
                    )
                }

                CompositionLocalProvider(
                    LocalIsWindowFocused provides
                        hasWindowFocus.collectAsState().value,
                ) {
                    RootContent(component)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_WINDOW_FOCUS, hasWindowFocus.value)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        hasWindowFocus.value = hasFocus
        Log.d(TAG, "Window focus changed: hasFocus=$hasFocus")
    }

    // https://issuetracker.google.com/issues/139738913
    private fun fixAndroid10ActivityLeak(component: RootComponent) {
        val activityLeak = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (isTaskRoot) {
                    finishAfterTransition()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, activityLeak)

        component.stack.subscribe { stack ->
            activityLeak.isEnabled = stack.backStack.isEmpty()
        }
    }
}

private const val TAG = "MainActivity"
private const val KEY_WINDOW_FOCUS = "window_focus"
