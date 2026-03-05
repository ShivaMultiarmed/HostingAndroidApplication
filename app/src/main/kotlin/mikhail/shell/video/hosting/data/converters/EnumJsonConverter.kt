package mikhail.shell.video.hosting.data.converters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class EnumJsonConverter : JsonSerializer<Enum<*>>, JsonDeserializer<Enum<*>> {
    override fun serialize(
        src: Enum<*>?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        return src?.name?.lowercase()?.let { JsonPrimitive(it) }
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Enum<*>? {
        val klass = when (typeOfT) {
            is Class<*> -> typeOfT
            is ParameterizedType -> typeOfT.rawType as? Class<*>
            else -> null
        }?.takeIf { it.isEnum } ?: return null
        val entry = json?.asString?: return null
        return java.lang.Enum.valueOf(klass as Class<out Enum<*>>, entry.uppercase())
    }
}