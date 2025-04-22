package app.vitune.android.ui.components.themed

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import app.vitune.android.LocalPlayerAwareWindowInsets
import app.vitune.android.R
import app.vitune.android.ui.screens.settings.SwitchSettingsEntry
import app.vitune.android.utils.center
import app.vitune.android.utils.color
import app.vitune.android.utils.semiBold
import app.vitune.core.ui.Dimensions
import app.vitune.core.ui.LocalAppearance
import app.vitune.core.ui.utils.isLandscape
import app.vitune.core.ui.utils.roundedShape
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

class TabsBuilder @PublishedApi internal constructor() {
    companion object {
        @Composable
        inline fun rememberTabs(crossinline content: TabsBuilder.() -> Unit) = rememberSaveable(
            saver = listSaver(
                save = { it }, // Lưu danh sách các tab khi trạng thái thay đổi
                restore = { it.toImmutableList() } // Khôi phục danh sách tab khi cần
            )
        ) {
            // Khởi tạo TabsBuilder và áp dụng nội dung
            TabsBuilder().apply(content).tabs.values.toImmutableList()
        }
    }

    @PublishedApi
    internal val tabs = mutableMapOf<String, Tab>() // Lưu trữ các tab theo key

    /**
     * Tạo tab sử dụng key dạng số nguyên (ID) và tiêu đề là resource string.
     */
    fun tab(
        key: Int,
        @StringRes title: Int,
        @DrawableRes icon: Int,
        canHide: Boolean = true
    ): Tab = tab(key.toString(), title, icon, canHide) // Chuyển `key` thành chuỗi để đảm bảo nhất quán

    /**
     * Tạo tab sử dụng key dạng chuỗi và tiêu đề là resource string.
     */
    fun tab(
        key: String,
        @StringRes title: Int,
        @DrawableRes icon: Int,
        canHide: Boolean = true
    ): Tab {
        require(key.isNotBlank()) { "key cannot be blank" } // Key không được rỗng
        require(!tabs.containsKey(key)) { "key already exists" } // Không trùng key
        require(icon != 0) { "icon is 0" } // Đảm bảo icon hợp lệ

        val ret = Tab.ResourcesTab(
            key = key,
            titleRes = title,
            icon = icon,
            canHide = canHide
        )
        tabs += key to ret // Thêm vào danh sách tab
        return ret
    }

    /**
     * Tạo tab sử dụng key dạng số nguyên và tiêu đề là chuỗi text.
     */
    fun tab(
        key: Int,
        title: String,
        @DrawableRes icon: Int,
        canHide: Boolean = true
    ): Tab = tab(key.toString(), title, icon, canHide)

    /**
     * Tạo tab sử dụng key dạng chuỗi và tiêu đề là chuỗi text.
     */
    fun tab(
        key: String,
        title: String,
        @DrawableRes icon: Int,
        canHide: Boolean = true
    ): Tab {
        require(key.isNotBlank()) { "key cannot be blank" } // Key không được rỗng
        require(title.isNotBlank()) { "title cannot be blank" } // Tiêu đề không được rỗng
        require(!tabs.containsKey(key)) { "key already exists" } // Không trùng key
        require(icon != 0) { "icon is 0" } // Icon phải hợp lệ

        val ret = Tab.StaticTab(
            key = key,
            titleText = title,
            icon = icon,
            canHide = canHide
        )
        tabs += key to ret // Thêm vào danh sách tab
        return ret
    }
}

@Parcelize
// `Tab` là một sealed class đại diện cho các tab trong ứng dụng.
// Sealed class giúp giới hạn tập hợp con của các lớp con có thể mở rộng từ nó.
sealed class Tab : Parcelable {
    // Mỗi tab có một khóa duy nhất để xác định.
    abstract val key: String

    // Hàm tiêu đề dạng Composable để hiển thị tên tab.
    @IgnoredOnParcel
    abstract val title: @Composable () -> String

    // Biểu tượng của tab.
    @get:DrawableRes
    abstract val icon: Int

    // Biến xác định tab có thể ẩn đi không.
    abstract val canHide: Boolean

    // `ResourcesTab` sử dụng ID chuỗi tài nguyên để đặt tiêu đề.
    data class ResourcesTab(
        override val key: String,
        @StringRes
        private val titleRes: Int,
        @DrawableRes
        override val icon: Int,
        override val canHide: Boolean
    ) : Tab() {
        @IgnoredOnParcel
        override val title: @Composable () -> String = { stringResource(titleRes) }
    }

    // `StaticTab` sử dụng một chuỗi trực tiếp để đặt tiêu đề.
    data class StaticTab(
        override val key: String,
        private val titleText: String,
        @DrawableRes
        override val icon: Int,
        override val canHide: Boolean
    ) : Tab() {
        @IgnoredOnParcel
        override val title: @Composable () -> String = { titleText }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
// Hàm này tạo một thanh điều hướng theo chiều dọc (Navigation Rail) với các tab.
inline fun NavigationRail(
    topIconButtonId: Int, // ID của icon nút ở trên cùng
    noinline onTopIconButtonClick: () -> Unit, // Hàm xử lý khi nhấn vào icon nút trên cùng
    tabIndex: Int, // Chỉ mục tab hiện tại
    crossinline onTabIndexChange: (Int) -> Unit, // Hàm thay đổi chỉ mục tab
    hiddenTabs: ImmutableList<String>, // Danh sách các tab bị ẩn
    crossinline setHiddenTabs: (List<String>) -> Unit, // Hàm để cập nhật danh sách tab ẩn
    modifier: Modifier = Modifier,
    tabsEditingTitle: String = stringResource(R.string.tabs), // Tiêu đề chỉnh sửa tab
    crossinline content: TabsBuilder.() -> Unit // Nội dung của các tab
) {
    val (colorPalette, typography) = LocalAppearance.current // Lấy thông tin về giao diện

    val tabs = TabsBuilder.rememberTabs(content) // Ghi nhớ danh sách tab từ content
    val isLandscape = isLandscape // Kiểm tra có đang ở chế độ ngang không

    val paddingValues = LocalPlayerAwareWindowInsets.current
        .only(WindowInsetsSides.Vertical + WindowInsetsSides.Start)
        .asPaddingValues()

    var editing by remember { mutableStateOf(false) } // Biến trạng thái để theo dõi chế độ chỉnh sửa tab

    // Nếu đang chỉnh sửa tab, hiển thị hộp thoại chỉnh sửa
    if (editing) DefaultDialog(
        onDismiss = { editing = false },
        horizontalPadding = 0.dp
    ) {
        BasicText(
            text = tabsEditingTitle,
            style = typography.s.center.semiBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn {
            items(
                items = tabs,
                key = { it.key }
            ) { tab ->
                SwitchSettingsEntry(
                    title = tab.title(),
                    text = null,
                    isChecked = tab.key !in hiddenTabs,
                    onCheckedChange = {
                        if (!it && hiddenTabs.size == tabs.size - 1) return@SwitchSettingsEntry // Đảm bảo ít nhất một tab được hiển thị

                        setHiddenTabs(if (it) hiddenTabs - tab.key else hiddenTabs + tab.key)
                    },
                    isEnabled = tab.canHide && (tab.key in hiddenTabs || hiddenTabs.size < tabs.size - 1)
                )
            }
        }
    }

    // Hiển thị thanh điều hướng theo chiều dọc
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
    ) {
        // Vùng chứa icon trên cùng
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .size(
                    width = if (isLandscape) Dimensions.navigationRail.widthLandscape
                    else Dimensions.navigationRail.width,
                    height = Dimensions.items.headerHeight
                )
        ) {
            Image(
                painter = painterResource(topIconButtonId),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                modifier = Modifier
                    .offset(
                        x = if (isLandscape) 0.dp else Dimensions.navigationRail.iconOffset,
                        y = 48.dp
                    )
                    .clip(CircleShape)
                    .clickable(onClick = onTopIconButtonClick)
                    .padding(all = 12.dp)
                    .size(22.dp)
            )
        }

        // Hiển thị danh sách các tab
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.width(
                if (isLandscape) Dimensions.navigationRail.widthLandscape
                else Dimensions.navigationRail.width
            )
        ) {
            val transition = updateTransition(targetState = tabIndex, label = null)

            tabs.fastForEachIndexed { index, tab ->
                AnimatedVisibility(
                    visible = tabIndex == index || tab.key !in hiddenTabs,
                    label = ""
                ) {
                    val dothAlpha by transition.animateFloat(label = "") {
                        if (it == index) 1f else 0f
                    }

                    val textColor by transition.animateColor(label = "") {
                        if (it == index) colorPalette.text else colorPalette.textDisabled
                    }

                    // Nội dung của icon tab
                    val iconContent: @Composable () -> Unit = {
                        Image(
                            painter = painterResource(tab.icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .vertical(enabled = !isLandscape)
                                .graphicsLayer {
                                    alpha = dothAlpha
                                    translationX = (1f - dothAlpha) * -48.dp.toPx()
                                    rotationZ = if (isLandscape) 0f else -90f
                                }
                                .size(Dimensions.navigationRail.iconOffset * 2)
                        )
                    }

                    // Nội dung của tiêu đề tab
                    val textContent: @Composable () -> Unit = {
                        BasicText(
                            text = tab.title(),
                            style = typography.xs.semiBold.center.color(textColor),
                            modifier = Modifier
                                .vertical(enabled = !isLandscape)
                                .rotate(if (isLandscape) 0f else -90f)
                                .padding(horizontal = 16.dp),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2
                        )
                    }

                    // Xử lý sự kiện nhấn tab
                    val contentModifier = Modifier
                        .clip(24.dp.roundedShape)
                        .combinedClickable(
                            onClick = { onTabIndexChange(index) },
                            onLongClick = { editing = true }
                        )

                    // Hiển thị theo hướng dọc hoặc ngang tùy vào chế độ landscape
                    if (isLandscape) Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = contentModifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                    ) {
                        iconContent()
                        textContent()
                    } else Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = contentModifier.padding(horizontal = 8.dp)
                    ) {
                        iconContent()
                        textContent()
                    }
                }
            }
        }
    }
}

// Modifier giúp xoay layout theo hướng dọc nếu `enabled` bật.
fun Modifier.vertical(enabled: Boolean = true) =
    if (enabled)
        layout { measurable, constraints ->
            val placeable = measurable.measure(constraints.copy(maxWidth = Int.MAX_VALUE))
            layout(placeable.height, placeable.width) {
                placeable.place(
                    x = -(placeable.width / 2 - placeable.height / 2), // Dịch chuyển vị trí để phù hợp với xoay dọc.
                    y = -(placeable.height / 2 - placeable.width / 2)
                )
            }
        } else this
