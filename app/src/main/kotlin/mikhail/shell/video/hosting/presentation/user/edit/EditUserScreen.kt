package mikhail.shell.video.hosting.presentation.user.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.DensityMedium
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.EditUserError
import mikhail.shell.video.hosting.domain.errors.equivalentTo
import mikhail.shell.video.hosting.domain.models.EditAction.KEEP
import mikhail.shell.video.hosting.domain.models.EditAction.REMOVE
import mikhail.shell.video.hosting.domain.models.EditAction.UPDATE
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import mikhail.shell.video.hosting.domain.validation.constructInfoMessage
import mikhail.shell.video.hosting.presentation.utils.Dialog
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.StandardEditField
import mikhail.shell.video.hosting.presentation.utils.Title
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
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    if (state.initialUser != null) {
        var nick by rememberSaveable { mutableStateOf(state.initialUser.nick) }
        var name by rememberSaveable { mutableStateOf(state.initialUser.name ?: "") }
        var avatarUri by rememberSaveable { mutableStateOf(null as Uri?) }
        var avatarAction by rememberSaveable { mutableStateOf(KEEP) }
        var bio by rememberSaveable { mutableStateOf(state.initialUser.bio ?: "") }
        var tel by rememberSaveable { mutableStateOf(state.initialUser.tel?.toString() ?: "") }
        var email by rememberSaveable { mutableStateOf(state.initialUser.email ?: "") }
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
            topBar = {
                TopBar(
                    title = stringResource(R.string.edit_profile_title),
                    onPopup = onPopup,
                    onSubmit = {
                        val input = EditUserInputState(
                            nick,
                            name,
                            avatarUri?.toString(),
                            avatarAction,
                            bio,
                            tel,
                            email
                        )
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
                val nickErrMsg = constructInfoMessage(
                    state.editUserError,
                    mapOf(
                        EditUserError.NICK_EMPTY to stringResource(R.string.nick_empty_error),
                        EditUserError.NICK_TOO_LARGE to stringResource(R.string.text_too_large_error, ValidationRules.MAX_NAME_LENGTH)
                    )
                )
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
                        placeholder = stringResource(R.string.nick_label),
                        errorMsg = nickErrMsg
                    )
                }
                val nameErrMsg = constructInfoMessage(
                    state.editUserError,
                    mapOf(
                        EditUserError.NAME_TOO_LARGE to stringResource(R.string.text_too_large_error, ValidationRules.MAX_NAME_LENGTH)
                    )
                )
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = name != (state.initialUser.name ?: ""),
                    empty = name.isEmpty(),
                    onRevert = {
                        name = state.initialUser.name ?: ""
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
                        placeholder = stringResource(R.string.name_label),
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
                val avatarErrMsg = constructInfoMessage(
                    state.editUserError,
                    mapOf(
                        EditUserError.AVATAR_TYPE_NOT_VALID to stringResource(R.string.type_not_valid_error),
                        EditUserError.AVATAR_TOO_LARGE to stringResource(R.string.file_too_large_error,
                            (ValidationRules.MAX_IMAGE_SIZE / 1024 / 1024).toString() + " MB")
                    )
                )
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
                            placeholder = if (avatarUri == null) stringResource(R.string.profile_choose_avatar_label)
                            else stringResource(R.string.profile_choose_another_avatar_label),
                            onClick = {
                                avatarPicker.launch("image/*")
                            },
                            errorMsg = avatarErrMsg
                        )
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            30.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        if (avatarExists != false) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.profile_current_avatar_hint)
                                )
                                AsyncImage(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape),
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
                        }
                        if (avatarUri != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.profile_chosen_avatar_hint)
                                )
                                val painter = rememberAsyncImagePainter(model = avatarUri)
                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    if (avatarExists == true && avatarAction == REMOVE) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.profile_delete_avatar_hint)
                            )
                        }
                    }
                }
                val telError = if (state.editUserError.equivalentTo(EditUserError.TEL_MALFORMED)) {
                    stringResource(R.string.phone_number_malformed_error)
                } else null
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = tel != (state.initialUser.tel ?: ""),
                    empty = tel.isEmpty(),
                    onRevert = {
                        tel = state.initialUser.tel?.toString() ?: ""
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
                        placeholder = stringResource(R.string.profile_telephone_label),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
                val emailError = constructInfoMessage(
                    state.editUserError,
                    mapOf(
                        EditUserError.EMAIL_MALFORMED to stringResource(R.string.email_malformed_error),
                        EditUserError.EMAIL_TOO_LARGE to stringResource(R.string.text_too_large_error, ValidationRules.MAX_USERNAME_LENGTH)
                    )
                )
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = email != (state.initialUser.email ?: ""),
                    empty = email.isEmpty(),
                    onRevert = {
                        email = state.initialUser.email ?: ""
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
                        placeholder = stringResource(R.string.email_label),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }
                val bioError = if (state.editUserError.equivalentTo(EditUserError.BIO_TOO_LARGE)) {
                    stringResource(R.string.text_too_large_error, ValidationRules.MAX_TEXT_LENGTH)
                } else null
                StandardEditField(
                    modifier = Modifier,
                    firstTime = false,
                    updated = bio != (state.initialUser.bio ?: ""),
                    empty = bio.isEmpty(),
                    onRevert = {
                        bio = state.initialUser.bio ?: ""
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
                        placeholder = stringResource(R.string.profile_bio),
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
                        dialogTitle = "",
                        dialogDescription = ""
                    )
                }
                Box (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(10.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    PrimaryButton(
                        needsCaution = true,
                        text = stringResource(R.string.delete_account_button),
                        isActivated = state.isRemoving || state.isRemovalConfirmed == true,
                        onClick = {
                            isRemoveAccountDialogVisible = true
                        }
                    )
                }
            }
        }
        LaunchedEffect(state.editedUser) {
            state.editedUser?.let {
                delay(1000)
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.profile_edit_success),
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
                onEditSuccess(userId)
            }
        }

    } else if (state.isInitializing) {
        LoadingComponent(
            modifier = Modifier.fillMaxSize()
        )
    } else if (state.isRemovalConfirmed != true) {
        ErrorComponent(
            modifier = Modifier.fillMaxSize(),
            onRetry = onInitialize
        )
    } else {
        AccountRemovedScreen()
    }
    LaunchedEffect(state.isRemovalConfirmed) {
        if (state.isRemovalConfirmed == true) {
            delay(3000)
            onRemoveSuccess()
        }
    }
}

@Composable
@Preview
fun AccountRemovedScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .aspectRatio(1f),
                imageVector = Icons.Rounded.PersonOff,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = stringResource(R.string.delete_account_success)
            )
            Title(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                text = stringResource(R.string.delete_account_success)
            )
        }
    }
}
