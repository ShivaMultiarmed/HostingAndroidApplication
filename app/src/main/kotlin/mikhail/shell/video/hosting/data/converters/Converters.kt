package mikhail.shell.video.hosting.data.converters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject

class InstantConverter @Inject constructor(): TypeAdapter<Instant>() {
    override fun write(out: JsonWriter?, value: Instant?) {
        out?.value(value?.toString())
    }

    override fun read(`in`: JsonReader?): Instant {
        return `in`?.let { Instant.parse(it.nextString()) }?: Clock.System.now()
    }
}