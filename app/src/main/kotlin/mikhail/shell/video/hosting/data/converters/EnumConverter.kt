package mikhail.shell.video.hosting.data.converters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class EnumConverter: JsonSerializer<Enum<*>>, JsonDeserializer<Enum<*>> {
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
        return json?.asJsonPrimitive?.asString?.let {
            val klass = typeOfT as? Class<out Enum<*>>?: return@let null
            return java.lang.Enum.valueOf(klass, it.uppercase())
        }
    }
}