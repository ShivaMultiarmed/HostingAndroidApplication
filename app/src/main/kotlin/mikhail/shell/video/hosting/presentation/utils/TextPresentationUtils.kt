package mikhail.shell.video.hosting.presentation.utils

import android.content.Context
import mikhail.shell.video.hosting.R
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