package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.UnexpectedError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.constructNetworkErrorMessage

@Composable
fun ErrorDisplay(
    error: Error?,
    snackBarHostState: SnackbarHostState,
    errorMessages: Map<Error, String> = emptyMap()
) {
    val context = LocalContext.current
    LaunchedEffect(error) {
        if (error != null) {
            val message = errorMessages[error]
                ?: error.let {
                    when (it) {
                        is NetworkError -> context.constructNetworkErrorMessage(it)
                        is UnexpectedError -> context.getString(R.string.unexpected_error)
                        else -> null
                    }
                }
            message?.let {
                snackBarHostState.showSnackbar(
                    message = it,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }
}

@Composable
fun StandardErrorDisplay(
    error: Error?,
    snackBarHostState: SnackbarHostState,
    notFoundMessage: String? = null,
    errorMessages: Map<Error, String> = emptyMap()
) {
    ErrorDisplay(
        error = error,
        snackBarHostState = snackBarHostState,
        errorMessages = errorMessages
            .toMutableMap()
            .apply {
                if (notFoundMessage != null) {
                    this[NetworkError.NOT_FOUND] = notFoundMessage
                }
                this[NetworkError.AUTHENTICATION] = stringResource(R.string.authentication_required)
            }
            .toMap()
    )
}

@Composable
fun ErrorHandler(
    error: Error?,
    handlers: Map<Error, () -> Unit> = emptyMap()
) {
    LaunchedEffect(error) {
        handlers[error]?.invoke()
    }
}

@Composable
fun StandardErrorHandler(
    error: Error?,
    notFoundHandler: (() -> Unit)? = null,
    authenticationRequiredHandler: (() -> Unit)? = null
) {
    ErrorHandler(
        error = error,
        handlers = mutableMapOf<Error, () -> Unit>()
            .apply {
                if (notFoundHandler != null) {
                    this[NetworkError.NOT_FOUND] = notFoundHandler
                }
                if (authenticationRequiredHandler != null) {
                    this[NetworkError.AUTHENTICATION] = authenticationRequiredHandler
                }
            }
            .toMap()
    )
}

@Composable
fun StandardComplexErrorHandler(
    error: Error?,
    snackBarHostState: SnackbarHostState,
    notFoundMessage: String? = null,
    notFoundHandler: (() -> Unit)? = null,
    authenticationRequiredHandler: (() -> Unit)? = null
) {
    StandardErrorDisplay(
        error = error,
        snackBarHostState = snackBarHostState,
        notFoundMessage = notFoundMessage
    )
    StandardErrorHandler(
        error = error,
        notFoundHandler = notFoundHandler,
        authenticationRequiredHandler = authenticationRequiredHandler
    )
}