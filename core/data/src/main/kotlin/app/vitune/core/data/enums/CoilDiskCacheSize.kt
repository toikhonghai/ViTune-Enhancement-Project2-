package app.vitune.core.data.enums

import app.vitune.core.data.utils.mb

@Suppress("unused", "EnumEntryName")
// file này chứa các enum class để định nghĩa kích thước bộ nhớ cache đĩa cho Coil
enum class CoilDiskCacheSize(val bytes: Long) {
    `64MB`(bytes = 64.mb),
    `128MB`(bytes = 128.mb),
    `256MB`(bytes = 256.mb),
    `512MB`(bytes = 512.mb),
    `1GB`(bytes = 1024.mb),
    `2GB`(bytes = 2048.mb)
}
