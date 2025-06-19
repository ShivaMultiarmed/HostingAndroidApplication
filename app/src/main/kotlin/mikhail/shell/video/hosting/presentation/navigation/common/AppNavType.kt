package mikhail.shell.video.hosting.presentation.navigation.common

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class AppNavType<T : Parcelable>(
    private val klass: Class<T>,
    private val serializer: KSerializer<T>
) : NavType<T>(
    isNullableAllowed = false
) {
    override fun get(bundle: Bundle, key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            bundle.getParcelable(key, klass) as T
        else
            bundle.getParcelable(key)
    }

    override fun parseValue(value: String): T = Json.decodeFromString(serializer, value)

    override fun serializeAsValue(value: T): String = Json.encodeToString(serializer, value)

    override fun put(bundle: Bundle, key: String, value: T) = bundle.putParcelable(key, value)

    companion object {
        inline fun <reified T : Parcelable> getMap(serializer: KSerializer<T>): Map<KType, AppNavType<T>> {
            return mapOf(
                typeOf<T>() to AppNavType(T::class.java, serializer)
            )
        }
    }
}