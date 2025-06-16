package com.rejeq.cpcam.ui

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.arkivanov.decompose.defaultComponentContext
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
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
            RootContent(
                component,
                hasWindowFocus = hasWindowFocus.collectAsState().value,
                onDarkModeChange = { isDarkModeEnabled ->
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            Color.TRANSPARENT,
                            Color.TRANSPARENT,
                        ) { isDarkModeEnabled },

                        // Used SystemBarStyle.dark() here to force setting
                        //   window.isNavigationBarContrastEnforced = false
                        // Thus, navigation will be completely transparent
                        navigationBarStyle = SystemBarStyle.dark(
                            Color.TRANSPARENT,
                        ),
                    )
                },
            )
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
