 package app.vitune.providers.innertube.utils

import app.vitune.providers.innertube.Innertube
import app.vitune.providers.innertube.models.MusicTwoRowItemRenderer
//companion object trong Kotlin là một đối tượng (object) bên trong một class, có thể được gọi mà không cần tạo instance của class đó.

 // được sử dụng để chuyển đổi từ một đối tượng MusicTwoRowItemRenderer thành các đối tượng khác trong ứng dụng YouTube Music.
fun Innertube.AlbumItem.Companion.from(renderer: MusicTwoRowItemRenderer) = Innertube.AlbumItem(
    info = renderer
        .title
        ?.runs
        ?.firstOrNull()
        ?.let(Innertube::Info),
    authors = null,
    year = renderer
        .subtitle
        ?.runs
        ?.lastOrNull()
        ?.text,
    thumbnail = renderer
        .thumbnailRenderer
        ?.musicThumbnailRenderer
        ?.thumbnail
        ?.thumbnails
        ?.firstOrNull()
).takeIf { it.info?.endpoint?.browseId != null }

fun Innertube.ArtistItem.Companion.from(renderer: MusicTwoRowItemRenderer) = Innertube.ArtistItem(
    info = renderer
        .title
        ?.runs
        ?.firstOrNull()
        ?.let(Innertube::Info),
    subscribersCountText = renderer
        .subtitle
        ?.runs
        ?.firstOrNull()
        ?.text,
    thumbnail = renderer
        .thumbnailRenderer
        ?.musicThumbnailRenderer
        ?.thumbnail
        ?.thumbnails
        ?.firstOrNull()
).takeIf { it.info?.endpoint?.browseId != null }

fun Innertube.PlaylistItem.Companion.from(renderer: MusicTwoRowItemRenderer) =
    Innertube.PlaylistItem(
        info = renderer
            .title
            ?.runs
            ?.firstOrNull()
            ?.let(Innertube::Info),
        channel = renderer
            .subtitle
            ?.runs
            ?.getOrNull(2)
            ?.let(Innertube::Info),
        songCount = renderer
            .subtitle
            ?.runs
            ?.getOrNull(4)
            ?.text
            ?.split(' ')
            ?.firstOrNull()
            ?.toIntOrNull(),
        thumbnail = renderer
            .thumbnailRenderer
            ?.musicThumbnailRenderer
            ?.thumbnail
            ?.thumbnails
            ?.firstOrNull()
    ).takeIf { it.info?.endpoint?.browseId != null }
