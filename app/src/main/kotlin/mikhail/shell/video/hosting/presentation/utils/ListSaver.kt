package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.toMutableStateList
import kotlinx.serialization.json.Json
import mikhail.shell.video.hosting.presentation.navigation.common.Route

object BackStackSaver: Saver<MutableList<Route>, Array<String>> {
    override fun SaverScope.save(value: MutableList<Route>): Array<String>? {
        return value.map {
            Json.encodeToString(Route.serializer(),it)
        }.toTypedArray()
    }
    override fun restore(value: Array<String>): MutableList<Route>? {
        return value.map {
            Json.decodeFromString(Route.serializer(), it)
        }.toMutableStateList()
    }
}