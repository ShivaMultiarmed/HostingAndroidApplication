package mikhail.shell.video.hosting.presentation.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.validation.mb
import java.util.Locale
import kotlin.contracts.ExperimentalContracts
import kotlin.math.floor
import kotlin.math.round
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun Long.toCorrectSuffix(): String {
    return when {
        this < 1_000 -> ""
        this < 1_000_000 -> "K"
        this < 1_000_000_000 -> "M"
        else -> "B"
    }
}

fun Long.toSubscribers() = toRoundString() + toCorrectSuffix()


fun Long.toFullSubscribers(context: Context): String {
    val quantityForWordForm = (if (this < 1000) this else this / 1000 * 1000).toInt()
    return context.resources.getQuantityString(
        R.plurals.subscribers_number,
        quantityForWordForm,
        toSubscribers()
    )
}

fun Long.toViews() = toRoundString() + toCorrectSuffix()

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
            String.format(Locale.getAvailableLocales().firstOrNull(),"%.2f", roundedNumber)
        } else {
            roundedNumber.toLong().toString()
        }
    } + " " + toCorrectSuffix()
}

fun Double.round(n: Int) = round(this * n) / n

fun Double.hasPortion() = floor(this) < this

@OptIn(ExperimentalContracts::class)
@Composable
fun getFileErrorMessage(error: FileError?, maxSize: Int = MAX_IMAGE_SIZE): String? {
    return when(error) {
        FileError.NOT_FOUND -> stringResource(R.string.file_not_found_error)
        FileError.EMPTY -> stringResource(R.string.file_empty)
        FileError.LARGE -> stringResource(R.string.file_too_large_error, "${maxSize.mb} MB")
        FileError.NOT_SUPPORTED -> stringResource(R.string.type_not_supported)
        FileError.NOT_VALID -> stringResource(R.string.file_not_valid)
        null -> null
    }
}

fun LocalDateTime.format(
    context: Context,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val now = Clock.System.now()
    val currentInstant = toInstant(timeZone)
    val stringBuilder = StringBuilder()
    if (now - 5.minutes < currentInstant) {
        stringBuilder.append(context.getString(R.string.date_time_just_now_message))
    } else if (now - 60.minutes < currentInstant) {
        val diff = (now - currentInstant).inWholeMinutes.toInt()
        stringBuilder.append(
            context.resources.getQuantityString(
                R.plurals.minutes_presentation,
                diff,
                diff
            )
        )
    } else if (now - 24.hours < currentInstant) {
        val diff = (now - currentInstant).inWholeHours.toInt()
        stringBuilder.append(
            context.resources.getQuantityString(
                R.plurals.hours_presentation,
                diff,
                diff
            )
        )
    } else if (now - 30.days < currentInstant) {
        val diff = (now - currentInstant).inWholeDays.toInt()
        stringBuilder.append(
            context.resources.getQuantityString(
                R.plurals.days_presentation,
                diff,
                diff
            )
        )
    } else if (now - 30.days * 12 < currentInstant) {
        val diff = ((now - currentInstant).inWholeDays / 30).toInt()
        stringBuilder.append(
            context.resources.getQuantityString(
                R.plurals.months_presentation,
                diff,
                diff
            )
        )
    } else {
        val diff = ((now - currentInstant).inWholeDays / (30 * 12)).toInt()
        stringBuilder.append(
            context.resources.getQuantityString(
                R.plurals.years_presentation,
                diff,
                diff
            )
        )
    }
    if (now - 5.minutes >= currentInstant) {
        stringBuilder.append(" ").append(context.getString(R.string.date_time_ago_message))
    }
    return stringBuilder.toString()
}