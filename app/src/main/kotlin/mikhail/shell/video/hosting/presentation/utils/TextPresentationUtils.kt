package mikhail.shell.video.hosting.presentation.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.validation.mb
import kotlin.math.floor
import kotlin.math.round

fun Long.toCorrectSuffix(): String {
    return when {
        this < 1_000 -> ""
        this < 1_000_000 -> "K"
        this < 1_000_000_000 -> "M"
        else -> "B"
    }
}

fun Long.toSubscribers(): String {
    return this.toRoundString() + this.toCorrectSuffix()
}

fun Long.toFullSubscribers(context: Context): String {
    val quantityForWordForm = (if (this < 1000) this else this / 1000 * 1000).toInt()
    return context.resources.getQuantityString(
        R.plurals.subscribers_number,
        quantityForWordForm,
        this.toSubscribers()
    )
}

fun Long.toViews(): String {
    return this.toRoundString() + this.toCorrectSuffix()
}

fun Long.toRoundString(): String {
    val roundedNumber = when {
        this < 1_000 -> toDouble()
        this < 1_000_000 -> toDouble() / 1_000
        this < 1_000_000_000 -> toDouble() / 1_000_000
        else -> toDouble() / 1_000_000_000
    }.round(2)
    return if (roundedNumber == toDouble()){
        roundedNumber.toLong().toString()
    } else {
        if (roundedNumber.hasPortion()) {
            String.format("%.2f", roundedNumber)
        }
        else {
            roundedNumber.toLong().toString()
        }
    } + " " + toCorrectSuffix()
}

fun Double.round(n: Int) = round(this * n) / n

fun Double.hasPortion():Boolean {
    return floor(this) < this
}

@Composable
fun getFileErrorMessage(error: FileError?, maxSize: Int = MAX_IMAGE_SIZE): String? {
    return when(error) {
        FileError.NAME_NOT_VALID -> stringResource(R.string.file_name_not_valid)
        FileError.NOT_FOUND -> stringResource(R.string.file_not_found_error)
        FileError.EMPTY -> stringResource(R.string.file_empty)
        FileError.LARGE -> stringResource(R.string.file_too_large_error, "${maxSize.mb} MB")
        FileError.NOT_SUPPORTED -> stringResource(R.string.type_not_valid_error)
        else -> null
    }
}