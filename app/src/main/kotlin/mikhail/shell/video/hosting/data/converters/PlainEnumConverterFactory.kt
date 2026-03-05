package mikhail.shell.video.hosting.data.converters

import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class PlainEnumConverterFactory : Converter.Factory() {
    override fun stringConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        return when (type) {
            is Class<*> if type.isEnum -> Converter<Enum<*>, String> { enumInstance ->
                enumInstance.name.lowercase()
            }
            else -> null
        }
    }
}