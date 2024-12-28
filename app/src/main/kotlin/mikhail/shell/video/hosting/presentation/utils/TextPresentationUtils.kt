package mikhail.shell.video.hosting.presentation.utils

import kotlin.math.floor
import kotlin.math.round

fun Long.toCorrectWordForm(
    f1: String,
    f2: String,
    f3: String
): String {
    return when {
        this >= 1_000 -> f3
        this % 10 == 1L && this != 11L -> f1
        this in 11..14 || this % 10 in 5..9 || this % 10 == 0L -> f3
        else -> f2
    }
}

fun Long.toCorrectSuffix(): String {
    return when {
        this < 1_000 -> ""
        this < 1_000_000 -> "K"
        this < 1_000_000_000 -> "M"
        this < 1_000_000_000_000 -> "B"
        else -> ""
    }
}

fun Long.toSubscribers(): String {
    return this.toRoundString() + this.toCorrectSuffix()
}

fun Long.toFullSubscribers(): String {
    return this.toSubscribers() + " " + this.toCorrectWordForm("подписчик", "подписчика", "подписчиков")
}

fun Long.toViews(): String {
    return this.toRoundString() + this.toCorrectSuffix() + " " +  "\uD83D\uDC41\uFE0F\u200D\uD83D\uDDE8\uFE0F"// this.toCorrectWordForm("просмотр", "просмотра", "просмотров")
}

fun Long.toRoundString(): String {
    val roundedNumber: Double = when {
        this < 1_000 -> this.toDouble()
        this < 1_000_000 -> this.toDouble() / 1_000
        this < 1_000_000_000 -> this.toDouble() / 1_000_000
        this < 1_000_000_000_000 -> this.toDouble() / 1_000_000_000
        else -> this.toDouble()
    }.round(2)
    return if (roundedNumber == this.toDouble()){
        roundedNumber.toLong().toString()
    }
    else {
        if (roundedNumber.hasPortion())
            String.format("%.2f", roundedNumber)
        else
            roundedNumber.toLong().toString()
    }
}

fun Double.round(n: Int) = round(this * n) / n

fun Double.hasPortion():Boolean {
    return floor(this) < this
}