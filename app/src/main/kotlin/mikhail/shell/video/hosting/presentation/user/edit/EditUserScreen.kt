package mikhail.shell.video.hosting.presentation.user.edit

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.DensityMedium
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneAndroid
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import mikhail.shell.video.hosting.domain.errors.EditUserError
import mikhail.shell.video.hosting.domain.errors.equivalentTo
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.EditAction.KEEP
import mikhail.shell.video.hosting.domain.models.EditAction.REMOVE
import mikhail.shell.video.hosting.domain.models.EditAction.UPDATE
import mikhail.shell.video.hosting.presentation.utils.Dialog
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.StandardEditField
import mikhail.shell.video.hosting.presentation.utils.TopBar

@Composable
fun EditUserScreen(
    state: EditUserScreenState,
    userId: Long,
    onInitialize: () -> Unit = {},
    onEdit: (EditUserInputState) -> Unit = {},
    onEditSuccess: (userId: Long) -> Unit = {},
    onRemove: () -> Unit = {},
    onRemoveSuccess: () -> Unit = {},
    onPopup: () -> Unit = {}
) {
    if (state.initialUser != null) {
        val snackbarHostState = remember { SnackbarHostState() }
        var nick by rememberSaveable { mutableStateOf(state.initialUser.nick) }
        var name by rememberSaveable { mutableStateOf(state.initialUser.name?: "") }
        var avatarUri by rememberSaveable { mutableStateOf(null as Uri?) }
        var avatarAction by rememberSaveable { mutableStateOf(EditAction.KEEP) }
        var bio by rememberSaveable { mutableStateOf(state.initialUser.bio?: "") }
        var tel by rememberSaveable { mutableStateOf(state.initialUser.tel?.toString()?: "") }
        var email by rememberSaveable { mutableStateOf(state.initialUser.email?: "") }
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
            topBar = {
                TopBar(
                    title = "Редактировать профиль",
                    onPopup = onPopup,
                    onSubmit = {
                        val input = EditUserInputState(nick, name, avatarUri?.toString(), avatarAction, bio, tel, email)
                        onEdit(input)
                    },
                    inProgress = state.isEditing || state.isRemoving
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState()),
            ) {
                val nickErrMsg = if (state.editUserError.equivalentTo(EditUserError.NICK_EMPTY)) {
                    "Заполните никнейм"
                } else if (state.editUserError.equivalentTo(EditUserError.NICK_TOO_LARGE)) {
                    "Никнейм больше 255 символов"
                } else null
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = nick != state.initialUser.nick,
                    empty = nick.isEmpty(),
                    onRevert = {
                        nick = state.initialUser.nick
                    },
                    onDelete = {
                        nick = ""
                    }
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.AlternateEmail,
                        value = nick,
                        onValueChange = {
                            nick = it
                        },
                        placeholder = "Никнейм",
                        errorMsg = nickErrMsg
                    )
                }
                val nameErrMsg = if (state.editUserError.equivalentTo(EditUserError.NAME_TOO_LARGE)) {
                    "Имя больше 255 символов"
                } else null
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = name != (state.initialUser.name ?: ""),
                    empty = name.isEmpty(),
                    onRevert = {
                        name = state.initialUser.name?: ""
                    },
                    onDelete = {
                        name = ""
                    }
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.Person,
                        value = name,
                        onValueChange = {
                            name = it
                        },
                        placeholder = "Имя",
                        errorMsg = nameErrMsg
                    )
                }
                val avatarPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                        if (it != null) {
                            avatarUri = it
                            avatarAction = UPDATE
                        }
                    }
                var avatarExists by rememberSaveable { mutableStateOf(null as Boolean?) }
                Column {
                    StandardEditField(
                        modifier = Modifier,
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
                            icon = Icons.Rounded.Image,
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
                                model = state.initialUser.avatar,
                                contentDescription = null,
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
                                contentDescription = null,
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
                val telError = if (state.editUserError.equivalentTo(EditUserError.TEL_MALFORMED)) {
                    "Введите корректный номер телефона"
                } else null
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = tel != (state.initialUser.tel?: ""),
                    empty = tel.isEmpty(),
                    onRevert = {
                        tel = state.initialUser.tel?.toString()?: ""
                    },
                    onDelete = {
                        tel = ""
                    }
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.PhoneAndroid,
                        value = tel,
                        errorMsg = telError,
                        onValueChange = {
                            tel = it
                        },
                        placeholder = "Телефон",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
                val emailError = if (state.editUserError.equivalentTo(EditUserError.EMAIL_MALFORMED)) {
                    "Введите корректный e-mail"
                } else if (state.editUserError.equivalentTo(EditUserError.EMAIL_TOO_LARGE)) {
                    "Email больше чем 255 символов"
                } else null
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = email != (state.initialUser.email?: ""),
                    empty = email.isEmpty(),
                    onRevert = {
                        email = state.initialUser.email?: ""
                    },
                    onDelete = {
                        email = ""
                    }
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.Email,
                        value = email,
                        errorMsg = emailError,
                        onValueChange = {
                            email = it
                        },
                        placeholder = "E-mail",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }
                val bioError = if (state.editUserError.equivalentTo(EditUserError.BIO_TOO_LARGE)) {
                    "Описание больше 5000 символов."
                } else null
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = bio != (state.initialUser.bio?: ""),
                    empty = bio.isEmpty(),
                    onRevert = {
                        bio = state.initialUser.bio?: ""
                    },
                    onDelete = {
                        bio = ""
                    }
                ) {
                    InputField(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.DensityMedium,
                        value = bio,
                        onValueChange = {
                            bio = it
                        },
                        placeholder = "Описание",
                        errorMsg = bioError,
                        maxLines = 50,
                    )
                }
                var isRemoveAccountDialogVisible by rememberSaveable { mutableStateOf(false) }
                if (isRemoveAccountDialogVisible) {
                    Dialog(
                        onSubmit = onRemove,
                        onDismiss = {
                            isRemoveAccountDialogVisible = false
                        },
                        dialogTitle = "Удаление аккаунта",
                        dialogDescription = "Вы уверены, что хотите удалить аккаунт?\n Вы потеряете все свои данные."
                    )
                }
                PrimaryButton(
                    needsCaution = true,
                    text = "Удалить аккаунт",
                    isActivated = state.isRemoving || state.isRemovalConfirmed == true,
                    onClick = {
                        isRemoveAccountDialogVisible = true
                    }
                )
            }
        }
        LaunchedEffect(state.editedUser) {
            state.editedUser?.let {
                delay(1000)
                snackbarHostState.showSnackbar(
                    message = "Профиль успешно отредактирован",
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
                onEditSuccess(userId)
            }
        }
        LaunchedEffect(state.isRemovalConfirmed) {
            if (state.isRemovalConfirmed == true) {
                delay(1000)
                snackbarHostState.showSnackbar(
                    message = "Вы удалили свой аккаунт",
                    withDismissAction = true,
                    duration = SnackbarDuration.Long
                )
                onRemoveSuccess()
            }
        }
    } else if (state.isInitializing) {
        LoadingComponent(
            modifier = Modifier.fillMaxSize()
        )
    } else {
        ErrorComponent(
            modifier = Modifier.fillMaxSize(),
            onRetry = onInitialize
        )
    }
}