package mikhail.shell.video.hosting.data.converters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

class InstantConverter @Inject constructor(): TypeAdapter<Instant>() {
    override fun write(out: JsonWriter?, value: Instant?) {
        out?.value(value?.toString())
    }

    override fun read(`in`: JsonReader?): Instant {
        return `in`?.let { Instant.parse(it.nextString()) }?: Clock.System.now()
    }
}