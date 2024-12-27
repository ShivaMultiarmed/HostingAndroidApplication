package mikhail.shell.video.hosting.data.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import mikhail.shell.video.hosting.domain.errors.ChannelCreationError

class ChannelCreationErrorAdapter : TypeAdapter<ChannelCreationError>() {
    override fun write(out: JsonWriter, value: ChannelCreationError?) {
        out.value(value?.name)
    }

    override fun read(`in`: JsonReader): ChannelCreationError {
        val value = `in`.nextString()
        return ChannelCreationError.valueOf(value)
    }
}