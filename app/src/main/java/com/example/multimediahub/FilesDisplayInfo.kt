package com.example.multimediahub

enum class SelectedMediaType {
    All, Images, Music, Videos, PDFs
}

enum class SortBy {
    Name, LastModified, Size
}

enum class ViewBy {
    List, Grid
}

data class FilesDisplayInfo(
    var selectedMediaType: SelectedMediaType = SelectedMediaType.All,
    var sortBy: SortBy = SortBy.Name,
    var viewBy: ViewBy = ViewBy.List
)
