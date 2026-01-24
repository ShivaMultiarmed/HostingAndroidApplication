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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.UnexpectedError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_NAME_LENGTH
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TEXT_LENGTH
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_USERNAME_LENGTH
import mikhail.shell.video.hosting.presentation.utils.Dialog
import mikhail.shell.video.hosting.presentation.utils.EditingState
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.FileInputField
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.StandardEditField
import mikhail.shell.video.hosting.presentation.utils.StartingComponent
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.presentation.utils.exists
import mikhail.shell.video.hosting.presentation.utils.getFileErrorMessage
import mikhail.shell.video.hosting.presentation.utils.rememberAsyncImagePainter
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingScreenState as ScreenState

@Composable
fun UserEditingScreen(
    state: ScreenState,
    onAction: (ScreenAction) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState
            )
        },
        topBar = {
            if (state is ScreenState.Editing) {
                TopBar(
                    title = stringResource(R.string.edit_profile_title),
                    onPopup = {
                        onAction(ScreenAction.Cancel)
                    },
                    onSubmit = {
                        onAction(ScreenAction.Submit)
                    },
                    inProgress = state.isLoading
                )
            } else {
                TopBar(
                    title = stringResource(R.string.edit_profile_title),
                    onPopup = {
                        onAction(ScreenAction.Cancel)
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state) {
                is ScreenState.Editing -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        val nickErrMsg = when (state.user.nick.error) {
                            is TextError -> when (state.user.nick.error) {
                                TextError.EMPTY -> stringResource(R.string.nick_empty_error)
                                TextError.LONG -> stringResource(
                                    R.string.text_too_large_error,
                                    MAX_NAME_LENGTH
                                )

                                TextError.EXISTS -> stringResource(R.string.nick_exists_error)
                                else -> null
                            }
                            is NetworkError -> stringResource(R.string.nick_validation_unavailable)
                            UnexpectedError -> stringResource(R.string.unexpected_error)
                            else -> null
                        }
                        StandardEditField(
                            modifier = Modifier,
                            firstTime = false,
                            edited = state.user.nick.value != state.user.nick.initial,
                            empty = state.user.nick.value.isEmpty(),
                            onRevert = {
                                onAction(ScreenAction.ChangeNick(state.user.nick.initial))
                            },
                            onRemove = {
                                onAction(ScreenAction.ChangeNick(""))
                            }
                        ) {
                            InputField(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Rounded.AlternateEmail,
                                value = state.user.nick.value,
                                onValueChange = {
                                    onAction(ScreenAction.ChangeNick(it))
                                },
                                onFocus = {
                                    onAction(ScreenAction.FocusNick)
                                },
                                onBlur = {
                                    onAction(ScreenAction.BlurNick)
                                },
                                label = stringResource(R.string.nick_label),
                                errorMsg = nickErrMsg
                            )
                        }
                        val nameErrMsg = when (state.user.name.error) {
                            TextError.EMPTY -> stringResource(R.string.name_empty, MAX_NAME_LENGTH)
                            TextError.LONG -> stringResource(
                                R.string.text_too_large_error,
                                MAX_NAME_LENGTH
                            )

                            else -> null
                        }
                        StandardEditField(
                            modifier = Modifier,
                            firstTime = false,
                            edited = state.user.name.value != state.user.name.initial,
                            empty = state.user.name.value.isEmpty(),
                            onRevert = {
                                onAction(ScreenAction.ChangeName(state.user.name.initial))
                            },
                            onRemove = {
                                onAction(ScreenAction.ChangeName(""))
                            }
                        ) {
                            InputField(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Rounded.Person,
                                value = state.user.name.value,
                                onValueChange = {
                                    onAction(ScreenAction.ChangeName(it))
                                },
                                onFocus = {
                                    onAction(ScreenAction.FocusName)
                                },
                                onBlur = {
                                    onAction(ScreenAction.BlurName)
                                },
                                label = stringResource(R.string.name_label),
                                errorMsg = nameErrMsg
                            )
                        }
                        val avatarPicker =
                            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                                if (it != null) {
                                    onAction(ScreenAction.ChangeAvatar(EditingState.Editing(it.toString())))
                                }
                            }
                        val avatarPainter = rememberAsyncImagePainter((state.user.avatar.initial as EditingState.Keeping).value!!)
                        val avatarErrMsg = getFileErrorMessage(state.user.avatar.error)
                        Column {
                            StandardEditField(
                                modifier = Modifier,
                                firstTime = false,
                                edited = state.user.avatar.value is EditingState.Editing || state.user.avatar.value is EditingState.Removing && avatarPainter.exists() == true,
                                empty = !(state.user.avatar.value is EditingState.Editing || avatarPainter.exists() == true && state.user.avatar.value !is EditingState.Removing),
                                onRevert = {
                                    onAction(ScreenAction.ChangeAvatar(state.user.avatar.initial))
                                },
                                onRemove = {
                                    onAction(ScreenAction.ChangeAvatar(EditingState.Removing))
                                }
                            ) {
                                FileInputField(
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = Icons.Rounded.Image,
                                    placeholder = when  {
                                        state.user.avatar.value is EditingState.Editing
                                            || avatarPainter.exists() == true
                                                && state.user.avatar.value is EditingState.Keeping -> stringResource(R.string.profile_choose_another_avatar_label)
                                        else -> stringResource(R.string.profile_choose_avatar_label)
                                    },
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
                                if (avatarPainter.exists() == true) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = stringResource(R.string.profile_current_avatar_hint)
                                        )
                                        Image(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(CircleShape),
                                            painter = avatarPainter,
                                            contentScale = ContentScale.Crop,
                                            contentDescription = null
                                        )
                                    }
                                }
                                if (state.user.avatar.value is EditingState.Editing) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = stringResource(R.string.profile_chosen_avatar_hint)
                                        )
                                        AsyncImage(
                                            model = state.user.avatar.value.value,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                            if (avatarPainter.exists() == true && state.user.avatar.value is EditingState.Removing) {
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
                        val telError = when (state.user.tel.error) {
                            TextError.EMPTY -> stringResource(R.string.tel_empty)
                            TextError.SHORT -> stringResource(R.string.phone_number_short_error)
                            TextError.LONG -> stringResource(R.string.phone_number_long_error)
                            TextError.PATTERN -> stringResource(R.string.phone_number_malformed_error)
                            else -> null
                        }
                        StandardEditField(
                            modifier = Modifier,
                            firstTime = false,
                            edited = state.user.tel.value != state.user.tel.initial,
                            empty = state.user.tel.value.isEmpty(),
                            onRevert = {
                                onAction(ScreenAction.ChangeTelephone(state.user.tel.initial))
                            },
                            onRemove = {
                                onAction(ScreenAction.ChangeTelephone(""))
                            }
                        ) {
                            InputField(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Rounded.PhoneAndroid,
                                value = state.user.tel.value,
                                errorMsg = telError,
                                onValueChange = {
                                    onAction(ScreenAction.ChangeTelephone(it))
                                },
                                onFocus = {
                                    onAction(ScreenAction.FocusTelephone)
                                },
                                onBlur = {
                                    onAction(ScreenAction.BlurTelephone)
                                },
                                label = stringResource(R.string.profile_telephone_label),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                        }
                        val emailError = when (state.user.email.error) {
                            TextError.EMPTY -> stringResource(R.string.email_empty)
                            TextError.LONG -> stringResource(
                                R.string.text_too_large_error,
                                MAX_USERNAME_LENGTH
                            )

                            TextError.PATTERN -> stringResource(R.string.email_malformed_error)
                            else -> null
                        }
                        StandardEditField(
                            firstTime = false,
                            edited = state.user.email.value != state.user.email.initial,
                            empty = state.user.email.value.isEmpty(),
                            onRevert = {
                                onAction(ScreenAction.ChangeEmail(state.user.email.initial))
                            },
                            onRemove = {
                                onAction(ScreenAction.ChangeEmail(""))
                            }
                        ) {
                            InputField(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Rounded.Email,
                                value = state.user.email.value,
                                errorMsg = emailError,
                                onValueChange = {
                                    onAction(ScreenAction.ChangeEmail(it))
                                },
                                onFocus = {
                                    onAction(ScreenAction.FocusEmail)
                                },
                                onBlur = {
                                    onAction(ScreenAction.BlurEmail)
                                },
                                label = stringResource(R.string.email_label),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                        }
                        val bioError = when (state.user.bio.error) {
                            TextError.EMPTY -> stringResource(R.string.bio_empty)
                            TextError.LONG -> stringResource(
                                R.string.text_too_large_error,
                                MAX_TEXT_LENGTH
                            )

                            else -> null
                        }
                        StandardEditField(
                            firstTime = false,
                            edited = state.user.bio.value != state.user.bio.initial,
                            empty = state.user.bio.value.isEmpty(),
                            onRevert = {
                                onAction(ScreenAction.ChangeBio(state.user.bio.initial))
                            },
                            onRemove = {
                                onAction(ScreenAction.ChangeBio(""))
                            }
                        ) {
                            InputField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                icon = Icons.Rounded.DensityMedium,
                                value = state.user.bio.value,
                                onValueChange = {
                                    onAction(ScreenAction.ChangeBio(it))
                                },
                                onFocus = {
                                    onAction(ScreenAction.FocusBio)
                                },
                                onBlur = {
                                    onAction(ScreenAction.BlurBio)
                                },
                                label = stringResource(R.string.profile_bio),
                                errorMsg = bioError,
                                maxLines = 50,
                            )
                        }
                        var isAccountRemovingDialogVisible by rememberSaveable {
                            mutableStateOf(
                                false
                            )
                        }
                        if (isAccountRemovingDialogVisible) {
                            Dialog(
                                onSubmit = {
                                    onAction(ScreenAction.Remove)
                                },
                                onDismiss = {
                                    isAccountRemovingDialogVisible = false
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
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.error,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(20.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            PrimaryProgressButton(
                                needsCaution = true,
                                inProgress = state.isRemoving,
                                onClick = {
                                    isAccountRemovingDialogVisible = true
                                },
                                text = stringResource(R.string.delete_account_button)
                            )
                        }
                    }

                }

                is ScreenState.Starting -> {
                    StartingComponent(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is ScreenState.Failure -> {
                    ErrorComponent(
                        modifier = Modifier.fillMaxSize(),
                        onRetry = {
                            onAction(ScreenAction.Restart)
                        }
                    )
                }

                else -> Unit
            }
        }
    }
}
