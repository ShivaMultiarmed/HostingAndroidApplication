package mikhail.shell.video.hosting.data.converters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_DATE_TIME

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDateTime {
        return LocalDateTime.parse(json?.asString, formatter)
    }

    override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.format(formatter))
    }
}

class InstantConverter: TypeAdapter<Instant>() {
    override fun write(out: JsonWriter?, value: Instant?) {
        out?.value(value.toString())
    }

    override fun read(`in`: JsonReader?): Instant {
        return `in`?.let {
            Instant.parse(it.nextString())
        }?: Clock.System.now()
    }
}