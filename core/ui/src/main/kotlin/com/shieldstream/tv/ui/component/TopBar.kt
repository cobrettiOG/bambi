package com.shieldstream.tv.ui.component

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.animateIntSizeAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shieldstream.tv.strings.R as StringsR
import com.shieldstream.tv.strings.localizedStringResource
import com.shieldstream.tv.ui.R
import com.shieldstream.tv.ui.theme.AppColors
import com.shieldstream.tv.ui.theme.LocalLightweightUi
import kotlin.math.roundToInt

enum class TopTab {
    SEARCH,
    HOME,
    EPISODES,
    TV,
    MOVIES,
    MY_LIST,
}

@get:StringRes
private val TopTab.labelResource: Int
    get() = when (this) {
        TopTab.SEARCH -> StringsR.string.common_search
        TopTab.HOME -> StringsR.string.common_home
        TopTab.EPISODES -> StringsR.string.common_episodes
        TopTab.TV -> StringsR.string.common_tv_shows
        TopTab.MOVIES -> StringsR.string.common_movies
        TopTab.MY_LIST -> StringsR.string.common_my_list
    }

@Composable
fun TopBar(
    selected: TopTab,
    onSelect: (TopTab) -> Unit,
    selectedRequester: FocusRequester,
    modifier: Modifier = Modifier,
    profileInitial: String? = null,
    profileAvatar: Int = 0,
    profileImage: String? = null,
    onProfile: () -> Unit = {},
    newEpisodeCount: Int = 0,
    /** Indicador de descargas arriba a la derecha (solo si hay alguna en marcha); al pulsarlo abre Descargas. */
    downloads: DownloadIndicatorState? = null,
    onDownloads: () -> Unit = {},
    onTabFocused: ((TopTab) -> Unit)? = null,
) {
    val tabs = TopTab.entries
    val tabRequesters = remember { tabs.associateWith { FocusRequester() } }
    val profileRequester = remember { FocusRequester() }
    val downloadsRequester = remember { FocusRequester() }
    val order = buildList {
        if (profileInitial != null) add(profileRequester)
        addAll(tabRequesters.values)
        if (downloads != null) add(downloadsRequester)
    }
    var focusedControl by remember { mutableStateOf<FocusRequester?>(null) }

    fun Modifier.trackFocus(requester: FocusRequester): Modifier =
        focusRequester(requester).onFocusChanged {
            if (it.isFocused) focusedControl = requester
            else if (focusedControl == requester) focusedControl = null
        }

    Row(
        modifier
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val step = when (event.key) {
                    Key.DirectionLeft -> -1
                    Key.DirectionRight -> 1
                    else -> return@onPreviewKeyEvent false
                }
                val index = order.indexOf(focusedControl)
                if (index < 0) return@onPreviewKeyEvent false
                val next = order.getOrNull(index + step) ?: return@onPreviewKeyEvent true
                if (next.requestFocus()) {
                    tabRequesters.entries.firstOrNull { it.value == next }?.key?.let { onTabFocused?.invoke(it) }
                }
                true
            }
            .fillMaxWidth()
            .height(64.dp)
            .background(AppColors.Background)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (profileInitial != null) {
            ProfileButton(profileInitial, profileAvatar, profileImage, onProfile, Modifier.trackFocus(profileRequester))
        }
        val focusedTab = tabRequesters.entries.firstOrNull { it.value == focusedControl }?.key
        val bounds = remember { mutableStateMapOf<TopTab, TabBounds>() }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(Modifier.horizontalScroll(rememberScrollState())) {
                focusedTab?.let { bounds[it] }?.let { FocusPill(it, Modifier.matchParentSize()) }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEach { tab ->
                        val label = localizedStringResource(tab.labelResource)
                        val requester = tabRequesters.getValue(tab)
                        val selection = if (tab == selected) Modifier.focusRequester(selectedRequester) else Modifier
                        TopBarItem(
                            icon = if (tab == TopTab.SEARCH) AppIcons.Search else null,
                            label = if (tab == TopTab.SEARCH) null else label,
                            iconDescription = if (tab == TopTab.SEARCH) label else null,
                            selected = tab == selected,
                            focused = tab == focusedTab,
                            showActive = tab == selected && focusedControl == null,
                            badgeCount = if (tab == TopTab.EPISODES) newEpisodeCount else 0,
                            onClick = { onSelect(tab) },
                            modifier = selection.trackFocus(requester).onPlaced { coordinates ->
                                val position = coordinates.positionInParent()
                                bounds[tab] = TabBounds(
                                    IntOffset(position.x.roundToInt(), position.y.roundToInt()),
                                    coordinates.size,
                                )
                            },
                        )
                    }
                }
            }
        }
        if (downloads != null) {
            DownloadIndicator(
                state = downloads,
                onClick = onDownloads,
                modifier = Modifier.widthIn(max = 110.dp).trackFocus(downloadsRequester),
            )
        }
        Image(
            painter = painterResource(R.drawable.bambi_logo),
            contentDescription = "Bambi",
            modifier = Modifier.size(36.dp),
        )
    }
}

private data class TabBounds(val position: IntOffset, val size: IntSize)
private val FocusPillColor = Color(0xFF353538)

@Composable
private fun FocusPill(bounds: TabBounds, modifier: Modifier) {
    val immediate = LocalLightweightUi.current
    val position by animateIntOffsetAsState(bounds.position, if (immediate) snap() else tween(140), label = "tabFocusPosition")
    val pillSize by animateIntSizeAsState(bounds.size, if (immediate) snap() else tween(140), label = "tabFocusSize")
    Canvas(modifier) {
        drawRoundRect(
            color = FocusPillColor,
            topLeft = Offset(position.x.toFloat(), position.y.toFloat()),
            size = Size(pillSize.width.toFloat(), pillSize.height.toFloat()),
            cornerRadius = CornerRadius(pillSize.height / 2f),
        )
    }
}

@Composable
private fun ProfileButton(initial: String, avatar: Int, image: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    val description = localizedStringResource(StringsR.string.settings_who_s_watching)
    Box(
        modifier
            .semantics {
                contentDescription = description
                role = Role.Button
            }
            .background(if (focused) FocusPillColor else Color.Transparent, CircleShape)
            .onFocusChanged { focused = it.isFocused }
            .tvClickable(onClick = onClick)
            .padding(5.dp),
    ) {
        ProfileAvatar(initial = initial, avatar = avatar, size = 32.dp, image = image)
    }
}

@Composable
private fun TopBarItem(
    icon: ImageVector?,
    selected: Boolean,
    focused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    iconDescription: String? = null,
    showActive: Boolean = false,
    badgeCount: Int = 0,
) {
    val foreground = if (focused || showActive) Color.White else Color.White.copy(alpha = 0.76f)
    Box(
        modifier = modifier
            .height(40.dp)
            .then(if (icon != null) Modifier.width(40.dp) else Modifier)
            .semantics {
                role = Role.Tab
                this.selected = selected
            }
            .tvClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = iconDescription, tint = foreground, modifier = Modifier.size(21.dp))
            if (showActive) {
                Box(Modifier.align(Alignment.BottomCenter).size(3.dp).background(Color.White.copy(alpha = 0.65f), CircleShape))
            }
        } else if (label != null) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    label,
                    color = foreground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                if (badgeCount > 0) {
                    Spacer(Modifier.width(5.dp))
                    Box(Modifier.size(6.dp).background(AppColors.Alert, CircleShape))
                }
            }
        }
    }
}
