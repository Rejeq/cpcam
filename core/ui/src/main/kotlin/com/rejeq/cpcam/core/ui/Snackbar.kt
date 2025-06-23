package com.rejeq.cpcam.core.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Represents the state and configuration for a snackbar message.
 *
 * @property messageResId The string resource ID for the snackbar message to
 *           display.
 * @property duration The duration for which the snackbar will be shown.
 *           Defaults to [SnackbarDuration.Short].
 * @property actionResId Optional string resource ID for the action button
 *           label. If null, no action button is shown.
 * @property action Optional suspend lambda to invoke if the action button is
 *           pressed.
 * @property withDismissAction Whether to show a dismiss action on the snackbar.
 *           Defaults to false.
 */
@Immutable
data class SnackbarState(
    @StringRes val messageResId: Int,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    @StringRes val actionResId: Int? = null,
    val action: (suspend () -> Unit)? = null,
    val withDismissAction: Boolean = false,
)

/**
 * Dispatcher responsible for showing snackbars in the UI using
 * [SnackbarHostState].
 *
 * This class is a singleton and should be injected using Hilt. It provides a
 * [show] method to display a snackbar with the given [SnackbarState]. If an
 * action is provided and the user performs the action, the action lambda will
 * be invoked.
 *
 * @property hostState The [SnackbarHostState] used to control the snackbar
 *           display.
 */
@Immutable
@Singleton
class SnackbarDispatcher @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    internal val hostState = SnackbarHostState()

    /**
     * Shows a snackbar with the specified [SnackbarState].
     *
     * @param state The [SnackbarState] describing the snackbar to display.
     *
     * If [SnackbarState.action] is provided and the user performs the action,
     * the action lambda is invoked.
     */
    suspend fun show(state: SnackbarState) {
        val result = hostState.showSnackbar(
            message = context.getString(state.messageResId),
            duration = state.duration,
            actionLabel = state.actionResId?.let { context.getString(it) },
            withDismissAction = state.withDismissAction,
        )

        if (state.action != null && result == SnackbarResult.ActionPerformed) {
            state.action()
        }
    }
}

/**
 * Composable that displays the [SnackbarHost] for the given
 * [SnackbarDispatcher].
 *
 * @param dispatcher The [SnackbarDispatcher] whose [SnackbarHostState] will
 *        be used. If null - nothing is shown.
 * @param modifier Optional [Modifier] for styling the [SnackbarHost].
 */
@Composable
fun SnackbarDispatcherContent(
    dispatcher: SnackbarDispatcher?,
    modifier: Modifier = Modifier,
) {
    if (dispatcher != null) {
        SnackbarHost(
            hostState = dispatcher.hostState,
            modifier = modifier,
        )
    }
}
