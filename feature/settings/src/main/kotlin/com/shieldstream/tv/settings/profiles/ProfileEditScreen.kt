package com.shieldstream.tv.settings.profiles

import com.shieldstream.tv.strings.R as StringsR
import com.shieldstream.tv.strings.localizedStringResource
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shieldstream.tv.model.Category
import com.shieldstream.tv.model.ProfileAvatars
import com.shieldstream.tv.model.SubtitlePreference
import com.shieldstream.tv.settings.HomeCategoriesDialog
import com.shieldstream.tv.ui.component.AppIcons
import com.shieldstream.tv.ui.component.ProfileAvatar
import com.shieldstream.tv.ui.component.tvClickable
import com.shieldstream.tv.ui.theme.InterFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileEditScreen(
    onDone: () -> Unit,
    onConnectTrakt: (profileId: String) -> Unit,
    viewModel: ProfileEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val editing = state as? ProfileEditUiState.Editing
    if (editing == null) {
        LaunchedEffect(Unit) { onDone() }
        return
    }
    val colorRequester = remember { FocusRequester() }
    val subtitlesRequester = remember { FocusRequester() }
    val profile = editing.profile
    var creatingPin by remember { mutableStateOf(false) }
    var firstPin by remember { mutableStateOf<String?>(null) }
    var pinError by remember { mutableStateOf<String?>(null) }
    var choosingCategories by remember { mutableStateOf(false) }
    var choosingIcon by remember { mutableStateOf(false) }

    var colorsExpanded by remember { mutableStateOf(false) }
    var subtitlesExpanded by remember { mutableStateOf(false) }

    Box(
        Modifier.fillMaxSize().background(Color(0xFF080809)),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 704.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                if (editing.isNew) localizedStringResource(StringsR.string.settings_add_profile)
                else localizedStringResource(StringsR.string.settings_edit_profile),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
            )
            EditorSection(localizedStringResource(StringsR.string.profile_editor_identity)) {
                EditorRow(
                    label = localizedStringResource(StringsR.string.settings_profile_icon),
                    onClick = { choosingIcon = true },
                ) {
                    ProfileAvatar(initial = profile.initial, avatar = profile.avatar, size = 72.dp, image = profile.avatarImage)
                }
                EditorDivider()
                NameField(
                    value = profile.name,
                    onValueChange = viewModel::setName,
                )
                EditorDivider()
                EditorRow(
                    label = localizedStringResource(StringsR.string.settings_color),
                    modifier = Modifier.focusRequester(colorRequester),
                    onClick = { colorsExpanded = !colorsExpanded },
                ) {
                    ProfileAvatar(initial = profile.initial, avatar = profile.avatar, size = 30.dp)
                    EditorDisclosure(expanded = colorsExpanded)
                }
                if (colorsExpanded) {
                    EditorDivider()
                    FlowRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ProfileAvatars.colors.indices.forEach { index ->
                            ColorChoice(
                                initial = profile.initial,
                                avatar = index,
                                selected = index == ProfileAvatars.safe(profile.avatar),
                                onClick = {
                                    viewModel.setAvatar(index)
                                    colorRequester.requestFocus()
                                    colorsExpanded = false
                                },
                            )
                        }
                    }
                }
            }
            EditorSection("Trakt") {
                val user = profile.traktUser
                EditorRow(
                    label = when {
                        editing.confirmDisconnect -> localizedStringResource(StringsR.string.settings_disconnect_trakt)
                        user != null -> localizedStringResource(StringsR.string.settings_connected)
                        else -> localizedStringResource(StringsR.string.settings_connect_trakt)
                    },
                    value = when {
                        !editing.traktAvailable -> localizedStringResource(StringsR.string.settings_trakt_not_available)
                        editing.busy -> localizedStringResource(StringsR.string.profile_editor_wait)
                        editing.confirmDisconnect && user != null ->
                            localizedStringResource(StringsR.string.settings_trakt_disconnect_user, user)
                        user != null -> "@$user"
                        else -> localizedStringResource(StringsR.string.profile_editor_not_connected)
                    },
                    enabled = editing.traktAvailable && !editing.busy,
                    onFocusLost = viewModel::cancelConfirmations,
                    onClick = {
                        if (user != null) viewModel.disconnectTrakt() else viewModel.save()?.let(onConnectTrakt)
                    },
                )
            }
            EditorSection(localizedStringResource(StringsR.string.profile_editor_language_playback)) {
                EditorRow(
                    label = localizedStringResource(StringsR.string.profile_editor_spanish_only),
                    value = toggleValue(profile.spanishOnlySources),
                    onClick = { viewModel.setSpanishOnly(!profile.spanishOnlySources) },
                )
                EditorDivider()
                EditorRow(
                    label = localizedStringResource(StringsR.string.profile_editor_spanish_first),
                    value = toggleValue(profile.spanishFirst || profile.spanishOnlySources),
                    enabled = !profile.spanishOnlySources,
                    onClick = { viewModel.setSpanishFirst(!profile.spanishFirst) },
                )
                EditorDivider()
                EditorRow(
                    label = localizedStringResource(StringsR.string.settings_default_subtitles),
                    value = subtitleLabel(profile.subtitlePreference),
                    modifier = Modifier.focusRequester(subtitlesRequester),
                    onClick = { subtitlesExpanded = !subtitlesExpanded },
                ) {
                    EditorDisclosure(expanded = subtitlesExpanded)
                }
                if (subtitlesExpanded) {
                    Text(
                        localizedStringResource(StringsR.string.settings_if_the_file_has_no_subtitles_in),
                        color = EditorMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    SubtitlePreference.entries.forEach { preference ->
                        EditorDivider()
                        val selected = preference == profile.subtitlePreference
                        EditorRow(
                            label = subtitleLabel(preference),
                            modifier = Modifier.padding(start = 16.dp).semantics { this.selected = selected },
                            onClick = {
                                viewModel.setSubtitlePreference(preference)
                                subtitlesRequester.requestFocus()
                                subtitlesExpanded = false
                            },
                        ) {
                            if (selected) Icon(AppIcons.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            EditorSection(localizedStringResource(StringsR.string.profile_editor_privacy_content)) {
                EditorRow(
                    label = localizedStringResource(StringsR.string.profile_editor_kids),
                    value = toggleValue(profile.kids),
                    onClick = { viewModel.setKids(!profile.kids) },
                )
                EditorDivider()
                EditorRow(
                    label = "PIN",
                    value = when {
                        !profile.locked -> localizedStringResource(StringsR.string.settings_set_pin)
                        editing.confirmRemovePin -> localizedStringResource(StringsR.string.settings_press_again_to_remove_the_pin)
                        else -> localizedStringResource(StringsR.string.settings_pin_set_ok_to_remove)
                    },
                    onFocusLost = viewModel::cancelConfirmations,
                    onClick = {
                        if (profile.locked) {
                            viewModel.removePin()
                        } else {
                            firstPin = null
                            pinError = null
                            creatingPin = true
                        }
                    },
                )
                if (!profile.kids) {
                    EditorDivider()
                    val visibleCount = Category.entries.count { it.name !in profile.hiddenCategories }
                    EditorRow(
                        label = localizedStringResource(StringsR.string.profile_editor_categories),
                        value = "$visibleCount / ${Category.entries.size}",
                        onClick = { choosingCategories = true },
                    )
                }
            }
            EditorSection(localizedStringResource(StringsR.string.profile_editor_actions)) {
                EditorRow(
                    label = localizedStringResource(StringsR.string.settings_save),
                    onClick = {
                        viewModel.save()
                        onDone()
                    },
                )
                EditorDivider()
                EditorRow(label = localizedStringResource(StringsR.string.settings_cancel), onClick = onDone)
                if (editing.canDelete) {
                    EditorDivider()
                    EditorRow(
                        label = localizedStringResource(StringsR.string.settings_delete_profile),
                        value = if (editing.confirmDelete) {
                            localizedStringResource(StringsR.string.settings_press_again_to_delete)
                        } else null,
                        onClick = { if (viewModel.delete()) onDone() },
                        onFocusLost = viewModel::cancelConfirmations,
                    )
                }
            }
        }
        if (choosingIcon) {
            ProfileIconPicker(
                profileId = profile.id,
                initial = profile.initial,
                avatar = profile.avatar,
                current = profile.avatarImage,
                onPick = { url ->
                    viewModel.setAvatarImage(url)
                    choosingIcon = false
                },
                onDismiss = { choosingIcon = false },
            )
        }
    }

    if (creatingPin) {
        val profileName = profile.name.ifBlank { localizedStringResource(StringsR.string.settings_this_profile) }
        val pinMismatchMessage = localizedStringResource(StringsR.string.settings_they_don_t_match_enter_the_new)
        PinDialog(
            title = if (firstPin == null) localizedStringResource(StringsR.string.settings_new_pin) else localizedStringResource(StringsR.string.settings_repeat_the_pin),
            subtitle = localizedStringResource(StringsR.string.settings_pin_digits_for, profileName),
            error = pinError,
            onPin = { pin ->
                val first = firstPin
                when {
                    first == null -> {
                        firstPin = pin
                        pinError = null
                    }
                    first == pin -> {
                        viewModel.setPin(pin)
                        creatingPin = false
                        firstPin = null
                    }
                    else -> {
                        firstPin = null
                        pinError = pinMismatchMessage
                    }
                }
            },
            onDismiss = {
                creatingPin = false
                firstPin = null
                pinError = null
            },
        )
    }

    if (!profile.kids && choosingCategories) {
        HomeCategoriesDialog(
            hidden = profile.hiddenCategories,
            onToggle = viewModel::toggleCategory,
            onToggleAll = viewModel::toggleAllCategories,
            onClose = { choosingCategories = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NameField(value: String, onValueChange: (String) -> Unit) {
    val requester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var editing by remember { mutableStateOf(false) }
    val nameLabel = localizedStringResource(StringsR.string.settings_name)
    LaunchedEffect(editing) {
        requester.requestFocus()
        if (editing) {
            withFrameNanos { }
            keyboard?.show()
        }
    }
    if (!editing) {
        EditorRow(
            label = nameLabel,
            value = value.ifEmpty { localizedStringResource(StringsR.string.settings_press_ok_to_type_the_name) },
            modifier = Modifier.focusRequester(requester),
            onClick = { editing = true },
        )
        return
    }

    val imeVisible = WindowInsets.isImeVisible
    var imeWasVisible by remember { mutableStateOf(false) }
    LaunchedEffect(imeVisible) {
        if (imeVisible) imeWasVisible = true
        else if (imeWasVisible) editing = false
    }
    val finishEditing = {
        keyboard?.hide()
        editing = false
    }
    BackHandler(onBack = finishEditing)

    BasicTextField(
        value = value,
        onValueChange = { onValueChange(it.replace("\n", "")) },
        singleLine = true,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            fontFamily = InterFontFamily,
        ),
        cursorBrush = SolidColor(Color.White),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
            showKeyboardOnFocus = false,
        ),
        keyboardActions = KeyboardActions(onDone = { finishEditing() }),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(requester)
            .semantics { contentDescription = nameLabel },
        decorationBox = { field ->
            Row(
                modifier = Modifier.editorRowSurface(focused = true).editorRowPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(nameLabel, color = Color.White, fontSize = 16.sp, modifier = Modifier.clearAndSetSemantics {})
                Box(Modifier.weight(1f), propagateMinConstraints = true) { field() }
            }
        },
    )
}

@Composable
private fun ColorChoice(initial: String, avatar: Int, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val description = localizedStringResource(StringsR.string.profile_editor_color_number, avatar + 1)
    Box(
        Modifier
            .semantics {
                contentDescription = description
                this.selected = selected
            }
            .border(
                width = 1.5.dp,
                color = when {
                    focused -> Color.White
                    selected -> Color.White.copy(alpha = 0.55f)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(10.dp),
            )
            .padding(4.dp)
            .onFocusChanged { focused = it.isFocused }
            .tvClickable(onClick = onClick),
    ) {
        ProfileAvatar(initial = initial, avatar = avatar, size = 34.dp)
    }
}

private val EditorMuted = Color(0xFFB9BAC1)
private val EditorPanel = Color(0xFF151518)
private val EditorPanelShape = RoundedCornerShape(12.dp)
private val EditorRowShape = RoundedCornerShape(8.dp)

@Composable
private fun EditorSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            modifier = Modifier.padding(start = 4.dp).semantics { heading() },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = EditorMuted,
        )
        Column(
            modifier = Modifier.fillMaxWidth().background(EditorPanel, EditorPanelShape).padding(4.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun EditorDivider() {
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(Color.White.copy(alpha = 0.07f)))
}

@Composable
private fun EditorRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    value: String? = null,
    enabled: Boolean = true,
    onFocusLost: () -> Unit = {},
    trailing: @Composable () -> Unit = {},
) {
    var focused by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .editorRowSurface(focused)
            .onFocusChanged {
                val hadFocus = focused
                focused = it.isFocused
                if (hadFocus && !focused) onFocusLost()
            }
            .tvClickable(enabled = enabled, onClick = onClick)
            .editorRowPadding(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val labelColor = if (enabled) Color.White else EditorMuted
        Text(label, modifier = Modifier.weight(1f), fontSize = 16.sp, color = labelColor)
        if (value != null) {
            Text(
                value,
                modifier = Modifier.widthIn(max = 240.dp),
                fontSize = 14.sp,
                color = if (focused) Color.White else EditorMuted,
                textAlign = TextAlign.End,
            )
        }
        trailing()
    }
}

private fun Modifier.editorRowSurface(focused: Boolean): Modifier = fillMaxWidth()
    .background(if (focused) Color(0xFF252529) else Color.Transparent, EditorRowShape)
    .border(1.dp, if (focused) Color.White else Color.Transparent, EditorRowShape)

private fun Modifier.editorRowPadding(): Modifier = heightIn(min = 52.dp).padding(horizontal = 16.dp, vertical = 8.dp)

@Composable
private fun EditorDisclosure(expanded: Boolean) {
    Text(
        if (expanded) "\u2303" else "\u2304",
        color = EditorMuted,
        fontSize = 17.sp,
        modifier = Modifier.clearAndSetSemantics {},
    )
}

@Composable
private fun toggleValue(enabled: Boolean): String =
    localizedStringResource(if (enabled) StringsR.string.settings_on else StringsR.string.settings_off)

@Composable
private fun subtitleLabel(preference: SubtitlePreference): String = localizedStringResource(
    when (preference) {
        SubtitlePreference.OFF -> StringsR.string.settings_off
        SubtitlePreference.SPANISH -> StringsR.string.settings_spanish
        SubtitlePreference.ENGLISH -> StringsR.string.settings_english
    },
)
