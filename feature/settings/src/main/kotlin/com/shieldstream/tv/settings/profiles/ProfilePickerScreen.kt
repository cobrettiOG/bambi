package com.shieldstream.tv.settings.profiles

import com.shieldstream.tv.ui.theme.AppColors

import com.shieldstream.tv.settings.R as SettingsR
import com.shieldstream.tv.strings.R as StringsR
import com.shieldstream.tv.strings.localizedStringResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shieldstream.tv.model.Profile
import com.shieldstream.tv.model.ProfileManagement
import com.shieldstream.tv.ui.R
import com.shieldstream.tv.ui.component.AppIcons
import com.shieldstream.tv.ui.component.ProfileAvatar
import com.shieldstream.tv.ui.component.onBackKey
import com.shieldstream.tv.ui.component.tvClickable
import kotlinx.coroutines.delay

internal val ProfileBackground = Brush.verticalGradient(listOf(Color(0xFF14151F), Color.Black))
internal val ProfileMuted = Color.White.copy(alpha = 0.55f)

@Composable
fun ProfilePickerScreen(
    onSelected: () -> Unit,
    onEdit: (String) -> Unit,
    onAdd: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ProfilesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ready = state as? ProfilesUiState.Ready ?: run {
        Box(Modifier.fillMaxSize().background(PickerBackground)) {
            PickerSettingsButton(onOpenSettings, Modifier.align(Alignment.TopEnd).padding(top = 28.dp, end = 48.dp))
        }
        return
    }
    var manage by remember { mutableStateOf(false) }
    var managementUnlocked by remember { mutableStateOf(false) }
    var managementAction by remember { mutableStateOf<ManagementAction?>(null) }
    var managementPinError by remember { mutableStateOf<String?>(null) }
    val managementPinProfile = ProfileManagement.pinProfile(ready.profiles)

    fun requestManagement(action: ManagementAction) {
        if (managementPinProfile == null || managementUnlocked) {
            managementUnlocked = true
            when (action) {
                ManagementAction.MANAGE -> manage = true
                ManagementAction.ADD -> onAdd()
            }
        } else {
            managementPinError = null
            managementAction = action
        }
    }
    var pinFor by remember { mutableStateOf<Pair<Profile, Boolean>?>(null) }
    var pinError by remember { mutableStateOf<String?>(null) }
    val activeRequester = remember { FocusRequester() }
    val settingsRequester = remember { FocusRequester() }
    LaunchedEffect(manage) {
        repeat(4) {
            if (runCatching { activeRequester.requestFocus() }.getOrDefault(false)) return@LaunchedEffect
            delay(150)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(PickerBackground)
            .onBackKey(enabled = manage) { manage = false },
    ) {
        Image(
            painter = painterResource(R.drawable.bambi_logo),
            contentDescription = "Bambi",
            modifier = Modifier.align(Alignment.TopStart).padding(start = 48.dp, top = 34.dp).size(30.dp),
        )

        PickerSettingsButton(
            onClick = onOpenSettings,
            modifier = Modifier.align(Alignment.TopEnd)
                .padding(top = 28.dp, end = 48.dp)
                .focusRequester(settingsRequester)
                .focusProperties { down = activeRequester },
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center),
        ) {
            Text(
                if (manage) localizedStringResource(StringsR.string.settings_manage_profiles) else localizedStringResource(StringsR.string.settings_who_s_watching),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(36.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                ready.profiles.forEach { profile ->
                    ProfileTile(
                        profile = profile,
                        manage = manage,
                        onClick = {
                            when {
                                profile.locked && !(manage && managementUnlocked) -> {
                                    pinError = null
                                    pinFor = profile to manage
                                }
                                manage -> onEdit(profile.id)
                                else -> {
                                    viewModel.select(profile.id)
                                    onSelected()
                                }
                            }
                        },
                        modifier = (if (profile.id == ready.activeId) Modifier.focusRequester(activeRequester) else Modifier)
                            .focusProperties { up = settingsRequester },
                    )
                }
                if (ready.canAdd) {
                    AddTile(
                        onClick = { requestManagement(ManagementAction.ADD) },
                        modifier = Modifier.focusProperties { up = settingsRequester },
                    )
                }
            }
            Spacer(Modifier.height(36.dp))
            PickerAction(
                text = if (manage) localizedStringResource(StringsR.string.settings_done) else localizedStringResource(StringsR.string.settings_manage_profiles),
                icon = if (manage) AppIcons.Check else AppIcons.Edit,
                onClick = {
                    if (manage) manage = false else requestManagement(ManagementAction.MANAGE)
                },
            )
        }
    }

    val wrongPinMessage = localizedStringResource(StringsR.string.settings_wrong_pin)
    managementAction?.let { action ->
        val profile = managementPinProfile ?: return@let
        PinDialog(
            title = localizedStringResource(StringsR.string.settings_management_pin),
            subtitle = localizedStringResource(StringsR.string.settings_management_pin_for, profile.name),
            error = managementPinError,
            onPin = { pin ->
                if (com.shieldstream.tv.model.ProfilePin.matches(profile, pin)) {
                    managementUnlocked = true
                    managementAction = null
                    managementPinError = null
                    when (action) {
                        ManagementAction.MANAGE -> manage = true
                        ManagementAction.ADD -> onAdd()
                    }
                } else {
                    managementPinError = wrongPinMessage
                }
            },
            onDismiss = {
                managementAction = null
                managementPinError = null
            },
        )
    }

    pinFor?.let { (profile, forEdit) ->
        PinDialog(
            title = localizedStringResource(StringsR.string.settings_profile_pin, profile.name),
            subtitle = localizedStringResource(StringsR.string.settings_enter_the_4_digit_pin),
            error = pinError,
            onPin = { pin ->
                if (com.shieldstream.tv.model.ProfilePin.matches(profile, pin)) {
                    pinFor = null
                    pinError = null
                    if (forEdit) {
                        onEdit(profile.id)
                    } else {
                        viewModel.select(profile.id)
                        onSelected()
                    }
                } else {
                    pinError = wrongPinMessage
                }
            },
            onDismiss = {
                pinFor = null
                pinError = null
            },
        )
    }
}

private enum class ManagementAction { MANAGE, ADD }

private val PickerBackground = Color(0xFF101114)
private val TileSize = 128.dp
private val TileShape = RoundedCornerShape(18.dp)
private val TileLabel = Color(0xFFB8BAC2)

@Composable
private fun ProfileTile(profile: Profile, manage: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PickerTile(label = profile.name, kids = profile.kids, modifier = modifier, onClick = onClick, metadata = {
        if (profile.traktUser != null) {
            Text("Trakt ?", color = AppColors.Success.copy(alpha = 0.8f), fontSize = 12.sp)
        }
        if (profile.locked) {
            Icon(
                AppIcons.Lock,
                contentDescription = localizedStringResource(StringsR.string.settings_pin_protected),
                tint = TileLabel,
                modifier = Modifier.size(13.dp),
            )
        }
    }) {
        ProfileAvatar(
            initial = profile.initial,
            avatar = profile.avatar,
            size = TileSize,
            shape = TileShape,
            image = profile.avatarImage,
        )
        if (manage) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(26.dp)
                    .background(PickerBackground, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun AddTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    PickerTile(label = localizedStringResource(StringsR.string.settings_add_profile), modifier = modifier, onClick = onClick) { focused ->
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = if (focused) 0.08f else 0.04f), TileShape)
                .border(1.dp, Color.White.copy(alpha = 0.12f), TileShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                AppIcons.Add,
                contentDescription = null,
                tint = if (focused) Color.White else TileLabel,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

@Composable
private fun PickerTile(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kids: Boolean = false,
    metadata: @Composable () -> Unit = {},
    content: @Composable BoxScope.(Boolean) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(TileSize + 20.dp)
            .onFocusChanged { focused = it.isFocused }
            .tvClickable(onClick = onClick),
    ) {
        Box(
            Modifier
                .size(TileSize)
                .graphicsLayer {
                    val scale = if (focused) 1.04f else 1f
                    scaleX = scale
                    scaleY = scale
                }
                .border(1.5.dp, if (focused) Color.White else Color.Transparent, TileShape),
        ) {
            content(focused)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val labelColor = if (focused) Color.White else TileLabel
            Text(
                text = label,
                modifier = Modifier.weight(1f, fill = false),
                color = labelColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            if (kids) {
                Icon(
                    painter = painterResource(SettingsR.drawable.ic_child_profile),
                    contentDescription = localizedStringResource(StringsR.string.settings_kids),
                    tint = labelColor,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Row(
            modifier = Modifier.height(24.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            metadata()
        }
    }
}

@Composable
private fun PickerAction(text: String, icon: ImageVector, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(10.dp)
    val contentColor = if (focused) Color.White else TileLabel
    Row(
        modifier = Modifier
            .onFocusChanged { focused = it.isFocused }
            .background(Color.White.copy(alpha = if (focused) 0.08f else 0.04f), shape)
            .border(1.dp, if (focused) Color.White else Color.White.copy(alpha = 0.12f), shape)
            .tvClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
        Text(text, color = contentColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PickerSettingsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    val label = localizedStringResource(StringsR.string.common_settings)
    Box(
        modifier
            .size(42.dp)
            .background(if (focused) Color(0xFF353538) else Color.Transparent, shape)
            .border(1.dp, if (focused) Color.White else Color.Transparent, shape)
            .semantics {
                role = Role.Button
                contentDescription = label
            }
            .onFocusChanged { focused = it.isFocused }
            .tvClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            AppIcons.Settings,
            contentDescription = null,
            tint = if (focused) Color.White else TileLabel,
            modifier = Modifier.size(20.dp),
        )
    }
}

internal val TraktRed = Color(0xFFED1C24)
