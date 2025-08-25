package mikhail.shell.video.hosting.presentation.user.edit

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.models.EditAction.KEEP
import mikhail.shell.video.hosting.domain.models.EditAction.REMOVE
import mikhail.shell.video.hosting.domain.models.EditAction.UPDATE
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_IMAGE_SIZE
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_NAME_LENGTH
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TEXT_LENGTH
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_USERNAME_LENGTH
import mikhail.shell.video.hosting.domain.validation.mb
import mikhail.shell.video.hosting.presentation.utils.Dialog
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler
import mikhail.shell.video.hosting.presentation.utils.StandardEditField
import mikhail.shell.video.hosting.presentation.utils.Title
import mikhail.shell.video.hosting.presentation.utils.TopBar

@Composable
fun UserEditingScreen(
    state: UserEditingScreenState,
    onEvent: (UserEditingUiEvent) -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    when (state) {
        is UserEditingScreenState.Editing -> {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                snackbarHost = {
                    SnackbarHost(snackBarHostState)
                },
                topBar = {
                    TopBar(
                        title = stringResource(R.string.edit_profile_title),
                        onPopup = {
                            onEvent(UserEditingUiEvent.Cancel)
                        },
                        onSubmit = {
                            onEvent(UserEditingUiEvent.Submit)
                        },
                        inProgress = state.isLoading
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
                    val nickErrMsg = when (state.editedUser.nickError) {
                        is TextError -> when (state.editedUser.nickError) {
                            TextError.EMPTY -> stringResource(R.string.nick_empty_error)
                            TextError.LONG -> stringResource(
                                R.string.text_too_large_error,
                                MAX_NAME_LENGTH
                            )

                            TextError.EXISTS -> stringResource(R.string.nick_exists_error)
                            else -> null
                        }

                        is NetworkError -> stringResource(R.string.nick_validation_unavailable)
                        else -> null
                    }
                    StandardEditField(
                        modifier = Modifier,
                        firstTime = false,
                        updated = state.editedUser.nick != state.initialUser.nick,
                        empty = state.editedUser.nick.isEmpty(),
                        onRevert = {
                            onEvent(UserEditingUiEvent.NickChanged(state.initialUser.nick))
                        },
                        onDelete = {
                            onEvent(UserEditingUiEvent.NickChanged(""))
                        }
                    ) {
                        InputField(
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.AlternateEmail,
                            value = state.editedUser.nick,
                            onValueChange = {
                                onEvent(UserEditingUiEvent.NickChanged(it))
                            },
                            placeholder = stringResource(R.string.nick_label),
                            errorMsg = nickErrMsg
                        )
                    }
                    val nameErrMsg = when (state.editedUser.nameError) {
                        TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_NAME_LENGTH)
                        else -> null
                    }
                    StandardEditField(
                        modifier = Modifier,
                        firstTime = false,
                        updated = state.editedUser.name != state.initialUser.name,
                        empty = state.editedUser.nick.isEmpty(),
                        onRevert = {
                            onEvent(UserEditingUiEvent.NameChanged(state.initialUser.name))
                        },
                        onDelete = {
                            onEvent(UserEditingUiEvent.NameChanged(""))
                        }
                    ) {
                        InputField(
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.Person,
                            value = state.editedUser.name,
                            onValueChange = {
                                onEvent(UserEditingUiEvent.NameChanged(it))
                            },
                            placeholder = stringResource(R.string.name_label),
                            errorMsg = nameErrMsg
                        )
                    }
                    val avatarPicker =
                        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                            if (it != null) {
                                onEvent(UserEditingUiEvent.AvatarChanged(it.toString(), UPDATE))
                            }
                        }
                    var avatarExists by rememberSaveable { mutableStateOf(null as Boolean?) }
                    val avatarErrMsg = when (state.editedUser.avatarError) {
                        FileError.LARGE -> stringResource(R.string.file_too_large_error,"${MAX_IMAGE_SIZE.mb} MB")
                        FileError.NOT_SUPPORTED -> stringResource(R.string.type_not_valid_error)
                        else -> null
                    }
                    Column {
                        StandardEditField(
                            modifier = Modifier,
                            firstTime = false,
                            updated = state.editedUser.avatarAction == UPDATE || state.editedUser.avatarAction == REMOVE && avatarExists == true,
                            empty = !(state.editedUser.avatar != null || avatarExists == true && state.editedUser.avatarAction != REMOVE),
                            onRevert = {
                                onEvent(UserEditingUiEvent.AvatarChanged(null, KEEP))
                            },
                            onDelete = {
                                onEvent(UserEditingUiEvent.AvatarChanged(null, REMOVE))
                            }
                        ) {
                            FileInputField(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Rounded.Image,
                                placeholder = if (state.editedUser.avatar == null) stringResource(R.string.profile_choose_avatar_label)
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
                            if (state.editedUser.avatar != null) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(R.string.profile_chosen_avatar_hint)
                                    )
                                    val painter = rememberAsyncImagePainter(model = state.editedUser.avatar)
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
                        if (avatarExists == true && state.editedUser.avatarAction == REMOVE) {
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
                    val telError = when (state.editedUser.telError) {
                        TextError.SHORT -> stringResource(R.string.phone_number_short_error)
                        TextError.LONG -> stringResource(R.string.phone_number_long_error)
                        TextError.PATTERN -> stringResource(R.string.phone_number_malformed_error)
                        else -> null
                    }
                    StandardEditField(
                        modifier = Modifier,
                        firstTime = false,
                        updated = state.editedUser.tel != (state.initialUser.tel),
                        empty = state.editedUser.tel.isEmpty(),
                        onRevert = {
                            onEvent(UserEditingUiEvent.TelChanged(state.initialUser.tel))

                        },
                        onDelete = {
                            onEvent(UserEditingUiEvent.TelChanged(""))
                        }
                    ) {
                        InputField(
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.PhoneAndroid,
                            value = state.editedUser.tel,
                            errorMsg = telError,
                            onValueChange = {
                                onEvent(UserEditingUiEvent.TelChanged(it))
                            },
                            placeholder = stringResource(R.string.profile_telephone_label),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }
                    val emailError = when (state.editedUser.emailError) {
                        TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_USERNAME_LENGTH)
                        TextError.PATTERN -> stringResource(R.string.email_malformed_error)
                        else -> null
                    }
                    StandardEditField(
                        modifier = Modifier,
                        firstTime = false,
                        updated = state.editedUser.email != state.initialUser.email,
                        empty = state.editedUser.email.isEmpty(),
                        onRevert = {
                            onEvent(UserEditingUiEvent.EmailChanged(state.initialUser.email))
                        },
                        onDelete = {
                            onEvent(UserEditingUiEvent.EmailChanged(""))
                        }
                    ) {
                        InputField(
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.Email,
                            value = state.editedUser.email,
                            errorMsg = emailError,
                            onValueChange = {
                                onEvent(UserEditingUiEvent.EmailChanged(it))
                            },
                            placeholder = stringResource(R.string.email_label),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }
                    val bioError = when (state.editedUser.bioError) {
                        TextError.LONG -> stringResource(R.string.text_too_large_error, MAX_TEXT_LENGTH)
                        else -> null
                    }
                    StandardEditField(
                        firstTime = false,
                        updated = state.editedUser.bio != state.initialUser.bio,
                        empty = state.editedUser.bio.isEmpty(),
                        onRevert = {
                            onEvent(UserEditingUiEvent.BioChanged(state.initialUser.bio))

                        },
                        onDelete = {
                            onEvent(UserEditingUiEvent.BioChanged(""))
                        }
                    ) {
                        InputField(
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.DensityMedium,
                            value = state.editedUser.bio,
                            onValueChange = {
                                onEvent(UserEditingUiEvent.BioChanged(it))
                            },
                            placeholder = stringResource(R.string.profile_bio),
                            errorMsg = bioError,
                            maxLines = 50,
                        )
                    }
                    var isRemoveAccountDialogVisible by rememberSaveable { mutableStateOf(false) }
                    if (isRemoveAccountDialogVisible) {
                        Dialog(
                            onSubmit = {
                                onEvent(UserEditingUiEvent.Remove)
                            },
                            onDismiss = {
                                isRemoveAccountDialogVisible = false
                            },
                            dialogTitle = stringResource(R.string.delete_account_warning_title),
                            dialogDescription = stringResource(R.string.delete_account_warning_message)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(10.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        PrimaryProgressButton(
                            needsCaution = true,
                            inProgress = state.isRemoving,
                            onClick = {
                                isRemoveAccountDialogVisible = true
                            },
                            text = stringResource(R.string.delete_account_button)
                        )
                    }
                }
            }
            StandardComplexErrorHandler(
                error = state.error,
                snackBarHostState = snackBarHostState,
                notFoundMessage = stringResource(R.string.user_not_found)
            )
            StandardComplexErrorHandler(
                error = state.removingError,
                snackBarHostState = snackBarHostState,
                notFoundMessage = stringResource(R.string.user_not_found)
            )
        }

        is UserEditingScreenState.Loading -> {
            LoadingComponent(
                modifier = Modifier.fillMaxSize()
            )
        }

        is UserEditingScreenState.Failure -> {
            ErrorComponent(
                modifier = Modifier.fillMaxSize(),
                onRetry = {
                    onEvent(UserEditingUiEvent.Reload)
                }
            )
            StandardComplexErrorHandler(
                error = state.error,
                snackBarHostState = snackBarHostState,
                notFoundMessage = stringResource(R.string.user_not_found)
            )
        }

        is UserEditingScreenState.Removed -> AccountRemovedScreen()

        is UserEditingScreenState.Success -> Unit
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
