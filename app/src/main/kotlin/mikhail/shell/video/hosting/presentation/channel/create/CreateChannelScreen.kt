package mikhail.shell.video.hosting.presentation.channel.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.Channel

@Composable
fun CreateChannelScreen(
    modifier: Modifier = Modifier,
    state: CreateChannelScreenState,
    onSubmit: (ChannelInputState) -> Unit,
    onSuccess: (Channel) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        var title by remember { mutableStateOf("") }
        TextField(
            value = title,
            onValueChange = {
                title = it
            }
        )
        var alias by remember { mutableStateOf("") }
        TextField(
            value = alias,
            onValueChange = {
                alias = it
            },
        )
        var description by remember { mutableStateOf("") }
        TextField(
            value = description,
            onValueChange = {
                description = it
            },
            maxLines = 50
        )
        Button(
            onClick = {
                val input = ChannelInputState(title, alias, description)
                onSubmit(input)
            }
        ) {
            Text(
                text = "Создать"
            )
        }
        if (state.error != null) {
            val errorMsg = when (state.error) {
                else -> "Непредвиденная ошибка"
            }
            Text(
                text = errorMsg
            )
        } else if (state.channel != null) {
            Text(
                text = "Вы успешно создали канал"
            )
            onSuccess(state.channel)
        }
    }
}