package com.fxc.themusicapp.data

data class PlaylistDisplayItem(
    val name: String,
    val url: String,
    val uploaderName: String,
    val itemCount: Int = -1,
    val thumbnailUrl: String? = null
) {
    val id: String
        get() = if (url.contains("list=")) url.substringAfter("list=") else url
}
