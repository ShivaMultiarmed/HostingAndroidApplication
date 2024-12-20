package mikhail.shell.video.hosting.presentation.utils

import android.util.Log
import org.junit.Test

class TextPresentationUtilsTests {
    private val TAG = "TextPresentationUtilsTests"
    @Test
    fun test_number_presentation() {
        val str = 11L.toCorrectWordForm("ключ","ключа","ключей")
        println(str)
    }
    @Test
    fun test_number_suffix() {
        val str = 11_000_000_000L.toCorrectSuffix()
        println(str)
    }
    @Test
    fun test_number_rounding() {
        val str = 23_899_947L.toRoundString()
        println(str)
    }
}