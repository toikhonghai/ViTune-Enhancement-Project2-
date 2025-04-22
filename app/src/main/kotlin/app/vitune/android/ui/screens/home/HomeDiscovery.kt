package app.vitune.android.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import app.vitune.android.LocalPlayerAwareWindowInsets
import app.vitune.android.LocalPlayerServiceBinder
import app.vitune.android.R
import app.vitune.android.ui.components.FadingRow
import app.vitune.android.ui.components.LocalMenuState
import app.vitune.android.ui.components.ShimmerHost
import app.vitune.android.ui.components.themed.FloatingActionsContainerWithScrollToTop
import app.vitune.android.ui.components.themed.Header
import app.vitune.android.ui.components.themed.NonQueuedMediaItemMenu
import app.vitune.android.ui.components.themed.SecondaryTextButton
import app.vitune.android.ui.components.themed.TextPlaceholder
import app.vitune.android.ui.items.AlbumItem
import app.vitune.android.ui.items.AlbumItemPlaceholder
import app.vitune.android.ui.items.SongItem
import app.vitune.android.ui.screens.Route
import app.vitune.android.utils.asMediaItem
import app.vitune.android.utils.center
import app.vitune.android.utils.color
import app.vitune.android.utils.forcePlay
import app.vitune.android.utils.playingSong
import app.vitune.android.utils.rememberSnapLayoutInfo
import app.vitune.android.utils.secondary
import app.vitune.android.utils.semiBold
import app.vitune.compose.persist.persist
import app.vitune.core.ui.Dimensions
import app.vitune.core.ui.LocalAppearance
import app.vitune.core.ui.shimmer
import app.vitune.core.ui.utils.isLandscape
import app.vitune.providers.innertube.Innertube
import app.vitune.providers.innertube.models.NavigationEndpoint
import app.vitune.providers.innertube.requests.discoverPage

// TODO: a lot of duplicate code all around the codebase, especially for discover

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalFoundationApi::class)
@Route
@Composable
fun HomeDiscovery(
    onMoodClick: (mood: Innertube.Mood.Item) -> Unit,
    onNewReleaseAlbumClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onMoreMoodsClick: () -> Unit,
    onMoreAlbumsClick: () -> Unit,
    onPlaylistClick: (browseId: String) -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current
    val windowInsets = LocalPlayerAwareWindowInsets.current
    val menuState = LocalMenuState.current
    val binder = LocalPlayerServiceBinder.current

    val scrollState = rememberScrollState()
    val moodGridState = rememberLazyGridState()

    val endPaddingValues = windowInsets
        .only(WindowInsetsSides.End)
        .asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)
        .padding(endPaddingValues)

    var discoverPage by persist<Result<Innertube.DiscoverPage>>("home/discovery") // Lưu trạng thái của trang khám phá

    LaunchedEffect(Unit) {
        if (discoverPage?.isSuccess != true) discoverPage = Innertube.discoverPage() // Gọi API để lấy trang khám phá
    }

    BoxWithConstraints {
        val widthFactor = if (isLandscape && maxWidth * 0.475f >= 320.dp) 0.475f else 0.75f
        val moodSnapLayoutInfoProvider = rememberSnapLayoutInfo(
            lazyGridState = moodGridState,
            positionInLayout = { layoutSize, itemSize ->
                layoutSize * widthFactor / 2f - itemSize / 2f
            }
        )
        val itemWidth = maxWidth * widthFactor

        Column(
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    windowInsets
                        .only(WindowInsetsSides.Vertical)
                        .asPaddingValues()
                )
        ) {
            Header(
                title = stringResource(R.string.discover),
                modifier = Modifier.padding(endPaddingValues)
            )

            discoverPage?.getOrNull()?.let { page ->
                if (page.moods.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FadingRow(
                            modifier = Modifier.weight(
                                weight = 1f,
                                fill = false
                            )
                        ) {
                            BasicText(
                                text = stringResource(R.string.moods_and_genres),
                                style = typography.m.semiBold,
                                modifier = sectionTextModifier
                            )
                        }

                        SecondaryTextButton(
                            text = stringResource(R.string.more),
                            onClick = onMoreMoodsClick,
                            modifier = sectionTextModifier
                        )
                    }

                    LazyHorizontalGrid( // Lưới ngang cho các mood
                        state = moodGridState, // Trạng thái cuộn
                        rows = GridCells.Fixed(4), // 4 hàng
                        flingBehavior = rememberSnapFlingBehavior(moodSnapLayoutInfoProvider), // Hành vi cuộn
                        contentPadding = endPaddingValues, // Padding bên phải
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((4 * (64 + 4)).dp) // Chiều cao của mood
                    ) {
                        items( // Lặp qua các mood
                            items = page.moods.sortedBy { it.title },
                            key = { it.endpoint.params ?: it.title }
                        ) {
                            MoodItem(
                                mood = it,
                                onClick = { it.endpoint.browseId?.let { _ -> onMoodClick(it) } },
                                modifier = Modifier
                                    .width(itemWidth)
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                if (page.newReleaseAlbums.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FadingRow(
                            modifier = Modifier.weight(
                                weight = 1f,
                                fill = false
                            )
                        ) {
                            BasicText(
                                text = stringResource(R.string.new_released_albums),
                                style = typography.m.semiBold,
                                modifier = sectionTextModifier
                            )
                        }

                        SecondaryTextButton(
                            text = stringResource(R.string.more),
                            onClick = onMoreAlbumsClick,
                            modifier = sectionTextModifier
                        )
                    }

                    LazyRow(contentPadding = endPaddingValues) {
                        items(items = page.newReleaseAlbums, key = { it.key }) {
                            AlbumItem(
                                album = it,
                                thumbnailSize = Dimensions.thumbnails.album,
                                alternative = true,
                                modifier = Modifier.clickable(onClick = { onNewReleaseAlbumClick(it.key) })
                            )
                        }
                    }
                }

                if (page.trending.songs.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FadingRow(
                            modifier = Modifier.weight(
                                weight = 1f,
                                fill = false
                            )
                        ) {
                            BasicText(
                                text = stringResource(R.string.trending),
                                style = typography.m.semiBold,
                                modifier = sectionTextModifier
                            )
                        }

                        page.trending.endpoint?.browseId?.let { browseId ->
                            SecondaryTextButton(
                                text = stringResource(R.string.more),
                                onClick = { onPlaylistClick(browseId) },
                                modifier = sectionTextModifier
                            )
                        }
                    }

                    val trendingGridState = rememberLazyGridState()
                    val trendingSnapLayoutInfoProvider = rememberSnapLayoutInfo(
                        lazyGridState = trendingGridState,
                        positionInLayout = { layoutSize, itemSize ->
                            (layoutSize * widthFactor / 2f - itemSize / 2f)
                        }
                    )

                    val (currentMediaId, playing) = playingSong(binder)

                    LazyHorizontalGrid(
                        state = trendingGridState,
                        rows = GridCells.Fixed(4),
                        flingBehavior = rememberSnapFlingBehavior(trendingSnapLayoutInfoProvider),
                        contentPadding = endPaddingValues,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((Dimensions.thumbnails.song + Dimensions.items.verticalPadding * 2) * 4)
                    ) {
                        items(
                            items = page.trending.songs,
                            key = Innertube.SongItem::key
                        ) { song ->
                            SongItem(
                                song = song,
                                thumbnailSize = Dimensions.thumbnails.song,
                                modifier = Modifier
                                    .combinedClickable(
                                        onLongClick = {
                                            menuState.display {
                                                NonQueuedMediaItemMenu(
                                                    onDismiss = menuState::hide,
                                                    mediaItem = song.asMediaItem
                                                )
                                            }
                                        },
                                        onClick = {
                                            val mediaItem = song.asMediaItem
                                            binder?.stopRadio()
                                            binder?.player?.forcePlay(mediaItem)
                                            binder?.setupRadio(
                                                NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                            )
                                        }
                                    )
                                    .animateItem(fadeInSpec = null, fadeOutSpec = null)
                                    .width(itemWidth),
                                showDuration = false,
                                isPlaying = playing && currentMediaId == song.key
                            )
                        }
                    }
                }
            } ?: discoverPage?.exceptionOrNull()?.let {
                BasicText(
                    text = stringResource(R.string.error_message),
                    style = typography.s.secondary.center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(all = 16.dp)
                )
            } ?: ShimmerHost {
                TextPlaceholder(modifier = sectionTextModifier)
                LazyHorizontalGrid(
                    state = moodGridState,
                    rows = GridCells.Fixed(4),
                    flingBehavior = rememberSnapFlingBehavior(moodSnapLayoutInfoProvider),
                    contentPadding = endPaddingValues,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4 * (Dimensions.items.moodHeight + 4.dp))
                ) {
                    items(16) {
                        MoodItemPlaceholder(
                            width = itemWidth,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
                TextPlaceholder(modifier = sectionTextModifier)
                Row {
                    repeat(2) {
                        AlbumItemPlaceholder(
                            thumbnailSize = Dimensions.thumbnails.album,
                            alternative = true
                        )
                    }
                }
            }
        }

        FloatingActionsContainerWithScrollToTop(
            scrollState = scrollState,
            icon = R.drawable.search,
            onClick = onSearchClick
        )
    }
}

// Component hiển thị 1 mục "Mood" (Tâm trạng âm nhạc), có màu nền riêng, chữ trắng/đen tuỳ độ sáng.
@Composable
fun MoodItem(
    mood: Innertube.Mood.Item, // Dữ liệu mood (title, stripeColor)
    onClick: () -> Unit, // Callback khi người dùng nhấn vào
    modifier: Modifier = Modifier // Modifier tuỳ chỉnh từ bên ngoài
) {
    val typography = LocalAppearance.current.typography // Lấy font từ theme hiện tại
    val thumbnailShape = LocalAppearance.current.thumbnailShape // Bo góc từ theme

    // Lấy màu stripeColor của mood (dưới dạng hex) và convert thành Color
    val color by remember { derivedStateOf { Color(mood.stripeColor) } }

    // Sử dụng ElevatedCard làm container có bo góc và màu nền là stripeColor
    ElevatedCard(
        modifier = modifier.height(Dimensions.items.moodHeight), // Chiều cao cố định
        shape = thumbnailShape,
        colors = CardDefaults.elevatedCardColors(containerColor = color) // Nền là màu mood
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }, // Khi nhấn thì gọi onClick
            contentAlignment = Alignment.CenterStart
        ) {
            BasicText(
                text = mood.title, // Hiển thị tên mood
                style = typography.xs.semiBold.color(
                    // Nếu nền màu sáng thì dùng chữ đen, còn tối thì dùng trắng
                    if (color.luminance() >= 0.5f) Color.Black else Color.White
                ),
                modifier = Modifier.padding(start = 24.dp) // Cách trái 24dp
            )
        }
    }
}

// Component hiển thị placeholder (xương cá shimmer) khi chưa có dữ liệu Mood
@Composable
fun MoodItemPlaceholder(
    width: Dp, // Chiều rộng của placeholder
    modifier: Modifier = Modifier
) = Spacer(
    modifier = modifier
        .background(
            color = LocalAppearance.current.colorPalette.shimmer, // Màu shimmer
            shape = LocalAppearance.current.thumbnailShape // Bo góc giống MoodItem
        )
        .size(
            width = width,
            height = Dimensions.items.moodHeight // Chiều cao cố định giống MoodItem
        )
)
