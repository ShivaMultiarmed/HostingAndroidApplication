package mikhail.shell.video.hosting.presentation.channel.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.DensityMedium
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import mikhail.shell.video.hosting.domain.errors.EditChannelError.TITLE_EMPTY
import mikhail.shell.video.hosting.domain.errors.EditChannelError.TITLE_TOO_LARGE
import mikhail.shell.video.hosting.domain.errors.equivalentTo
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.EditAction.KEEP
import mikhail.shell.video.hosting.domain.models.EditAction.REMOVE
import mikhail.shell.video.hosting.domain.models.EditAction.UPDATE
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.StandardEditField
import mikhail.shell.video.hosting.presentation.utils.TopBar

@Composable
fun EditChannelScreen(
    modifier: Modifier = Modifier,
    state: EditChannelScreenState,
    onSubmit: (EditChannelInputState) -> Unit,
    onSuccess: (Channel) -> Unit,
    onPopup: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val initialChannel = state.initialChannel
    if (initialChannel != null) {
        var title by rememberSaveable { mutableStateOf(initialChannel.title) }
        var alias by rememberSaveable { mutableStateOf(initialChannel.alias ?: "") }
        val scrollState = rememberScrollState()
        var description by rememberSaveable { mutableStateOf(initialChannel.description ?: "") }
        var avatarUri by rememberSaveable { mutableStateOf<Uri?>(null) }
        var avatarAction by rememberSaveable { mutableStateOf(KEEP) }
        var avatarExists by rememberSaveable { mutableStateOf(null as Boolean?) }
        var coverUri by rememberSaveable { mutableStateOf<Uri?>(null) }
        var coverAction by rememberSaveable { mutableStateOf(KEEP) }
        var coverExists by rememberSaveable { mutableStateOf(null as Boolean?) }
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            topBar = {
                TopBar(
                    title = "Редактировать канал",
                    onPopup = onPopup,
                    inProgress = state.isLoading,
                    onSubmit = {
                        val input = EditChannelInputState(
                            title,
                            alias,
                            description,
                            coverUri.toString(),
                            coverAction,
                            avatarUri.toString(),
                            avatarAction
                        )
                        onSubmit(input)
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .verticalScroll(scrollState)
            ) {
                LaunchedEffect(state.editedChannel) {
                    if (state.editedChannel != null) {
                        snackbarHostState.showSnackbar(
                            message = "Канал был успешно изменён.",
                            duration = SnackbarDuration.Long
                        )
                        onSuccess(state.editedChannel)
                    }
                }
                val titleErrMsg = if (state.error.equivalentTo(TITLE_EMPTY)) {
                    "Заполните название"
                } else if (state.error.equivalentTo(TITLE_TOO_LARGE)) {
                    "Название слишком большое"
                } else null
                StandardEditField(
                    modifier = Modifier.fillMaxWidth(),
                    firstTime = false,
                    updated = title != (initialChannel.title),
                    empty = title.isEmpty(),
                    onRevert = {
                        title = initialChannel.title
                    },
                    onDelete = {
                        title = ""
                    }
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.Title,
                        value = title,
                        onValueChange = {
                            title = it
                        },
                        placeholder = "Название",
                        errorMsg = titleErrMsg
                    )
                }
                StandardEditField(
                    modifier = Modifier.fillMaxWidth(),
                    firstTime = false,
                    updated = alias != (initialChannel.alias?: ""),
                    empty = alias.isEmpty(),
                    onRevert = {
                        alias = initialChannel.alias?: ""
                    },
                    onDelete = {
                        alias = ""
                    }
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.AlternateEmail,
                        value = alias,
                        onValueChange = {
                            alias = it
                        },
                        placeholder = "Никнейм канала"
                    )
                }
                StandardEditField(
                    modifier = Modifier.fillMaxWidth(),
                    firstTime = false,
                    updated = description != (initialChannel.description?: ""),
                    empty = description.isEmpty(),
                    onRevert = {
                        description = initialChannel.description?: ""
                    },
                    onDelete = {
                        description = ""
                    }
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.DensityMedium,
                        value = description,
                        onValueChange = {
                            description = it
                        },
                        placeholder = "Описание",
                        maxLines = 50,
                    )
                }
                val avatarPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                        if (it != null) {
                            avatarUri = it
                            avatarAction = UPDATE
                        }
                    }
                Column {
                    StandardEditField(
                        modifier = Modifier.fillMaxWidth(),
                        firstTime = false,
                        updated = avatarAction == UPDATE || avatarAction == REMOVE && avatarExists == true,
                        empty = !(avatarUri != null || avatarExists == true && avatarAction != REMOVE),
                        onRevert = {
                            avatarUri = null
                            avatarAction = KEEP
                        },
                        onDelete = {
                            avatarUri = null
                            avatarAction = REMOVE
                        }
                    ) {
                        FileInputField(
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.Person,
                            placeholder = if (avatarUri == null) "Выбрать аватар" else "Поменять аватар",
                            onClick = {
                                avatarPicker.launch("image/*")
                            }
                        )
                    }
                    Column {
                        if (avatarExists != false) {
                            Text(
                                text = "Текущий аватар"
                            )
                            AsyncImage(
                                modifier = Modifier
                                    .width(300.dp)
                                    .aspectRatio(16f / 9)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop,
                                model = initialChannel.avatarUrl,
                                contentDescription = title,
                                onSuccess = {
                                    avatarExists = true
                                },
                                onError = {
                                    avatarExists = false
                                }
                            )
                        }
                        if (avatarUri != null) {
                            Text("Вы выбрали")
                            val painter = rememberAsyncImagePainter(model = avatarUri)
                            Image(
                                painter = painter,
                                contentDescription = title,
                                modifier = Modifier
                                    .width(300.dp)
                                    .aspectRatio(16f / 9)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        if (avatarExists == true) {
                            if (avatarAction == REMOVE) {
                                Text(
                                    text = "Вы удалите аватар."
                                )
                            }
                        }
                    }
                }

                val coverPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                        if (it != null) {
                            coverUri = it
                            coverAction = UPDATE
                        }
                    }
                Column {
                    StandardEditField(
                        modifier = Modifier.fillMaxWidth(),
                        firstTime = false,
                        updated = coverAction == UPDATE || coverAction == REMOVE && coverExists == true,
                        empty = !(coverUri != null || coverExists == true && coverAction != REMOVE),
                        onRevert = {
                            coverUri = null
                            coverAction = KEEP
                        },
                        onDelete = {
                            coverUri = null
                            coverAction = REMOVE
                        }
                    ) {
                        FileInputField(
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.Wallpaper,
                            placeholder = if (coverUri == null) "Выбрать обложку" else "Поменять обложку",
                            onClick = {
                                coverPicker.launch("image/*")
                            }
                        )
                    }
                    Column {
                        if (coverExists != false) {
                            Text(
                                text = "Текущая обложка"
                            )
                            AsyncImage(
                                modifier = Modifier
                                    .width(300.dp)
                                    .aspectRatio(16f / 9)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop,
                                model = initialChannel.coverUrl,
                                contentDescription = title,
                                onSuccess = { coverExists = true },
                                onError = { coverExists = false }
                            )
                        }
                        if (coverUri != null) {
                            Text("Вы выбрали")
                            val painter = rememberAsyncImagePainter(model = coverUri)
                            Image(
                                painter = painter,
                                contentDescription = title,
                                modifier = Modifier
                                    .width(300.dp)
                                    .aspectRatio(16f / 9)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        if (coverExists == true) {
                            if (coverAction == REMOVE) {
                                Text(
                                    text = "Вы удалите обложку"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}