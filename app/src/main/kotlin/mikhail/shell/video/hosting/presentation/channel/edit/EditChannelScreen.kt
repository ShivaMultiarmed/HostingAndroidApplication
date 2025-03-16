package mikhail.shell.video.hosting.presentation.channel.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import mikhail.shell.video.hosting.domain.errors.EditChannelError.TITLE_EMPTY
import mikhail.shell.video.hosting.domain.errors.EditChannelError.TITLE_TOO_LARGE
import mikhail.shell.video.hosting.domain.errors.equivalentTo
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.presentation.utils.ActionItem
import mikhail.shell.video.hosting.presentation.utils.DeletingItem
import mikhail.shell.video.hosting.presentation.utils.EditField
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.RevertingItem
import mikhail.shell.video.hosting.presentation.utils.TopBar

@Composable
fun EditChannelScreen(
    modifier: Modifier = Modifier,
    state: EditChannelScreenState,
    onSubmit: (EditChannelInputState) -> Unit,
    onSuccess: (Channel) -> Unit,
    onPopup: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val initialChannel = state.initialChannel
    if (initialChannel != null) {
        var title by rememberSaveable { mutableStateOf(initialChannel.title) }
        var alias by rememberSaveable { mutableStateOf(initialChannel.alias ?: "") }
        val scrollState = rememberScrollState()
        var description by rememberSaveable { mutableStateOf(initialChannel.description ?: "") }
        var avatarUri by rememberSaveable { mutableStateOf<Uri?>(null) }
        var avatarAction by rememberSaveable { mutableStateOf(EditAction.KEEP) }
        var coverUri by rememberSaveable { mutableStateOf<Uri?>(null) }
        var coverAction by rememberSaveable { mutableStateOf(EditAction.KEEP) }
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
                val titleActions = mutableListOf<ActionItem>()
                if (title != initialChannel.title) {
                    titleActions.add(
                        RevertingItem(
                            reverting = {
                                title = initialChannel.title
                            }
                        )
                    )
                }
                if (title.isNotEmpty()) {
                    titleActions.add(
                        DeletingItem(
                            deleting = {
                                title = ""
                            }
                        )
                    )
                }
                EditField(
                    actionItems = titleActions
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
                EditField(
                    actionItems = if (alias.isNotEmpty()) listOf(
                        DeletingItem(
                            deleting = {
                                alias = ""
                            }
                        )
                    ) else emptyList()
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
                EditField(
                    actionItems = if (description.isNotEmpty()) listOf(
                        DeletingItem(
                            deleting = {
                                description = ""
                            }
                        )
                    ) else emptyList()
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
                val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                    if (it != null) {
                        avatarUri = it
                        avatarAction = EditAction.UPDATE
                    }
                }
                val avatarActions = mutableListOf<ActionItem>()
                if (avatarAction != EditAction.KEEP) {
                    avatarActions.add(
                        RevertingItem(
                            reverting = {
                                avatarAction = EditAction.KEEP
                                avatarUri = null
                            }
                        )
                    )
                }
                if (avatarUri != null) {
                    avatarActions.add(
                        DeletingItem(
                            deleting = {
                                avatarUri = null
                                avatarAction = EditAction.REMOVE
                            }
                        )
                    )
                }
                EditField(
                    actionItems = avatarActions
                ) {
                    FileInputField(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.Person,
                        placeholder = if (avatarUri == null) "Выбрать аватар канала" else "Поменять аватар канала",
                        onClick = {
                            avatarPicker.launch("image/*")
                        }
                    )
                }
                val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                    if (it != null) {
                        coverUri = it
                        coverAction = EditAction.UPDATE
                    }
                }
                val coverActions = mutableListOf<ActionItem>()
                if (coverAction != EditAction.KEEP) {
                    coverActions.add(
                        RevertingItem(
                            reverting = {
                                coverAction = EditAction.KEEP
                                coverUri = null
                            }
                        )
                    )
                }
                if (coverUri != null) {
                    coverActions.add(
                        DeletingItem(
                            deleting = {
                                coverUri = null
                                coverAction = EditAction.REMOVE
                            }
                        )
                    )
                }
                EditField(
                    actionItems = coverActions
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
            }
        }
    }
}