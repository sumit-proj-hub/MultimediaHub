package com.example.multimediahub

enum class SelectedMediaType {
    All, Images, Music, Videos, PDFs;
    companion object {
        val Default = All
    }
}

enum class SortBy {
    Name, LastModified, Size;
    companion object {
        val Default = LastModified
    }
}

enum class ViewBy {
    List, Grid;
    companion object {
        val Default = List
    }
}

data class FilesDisplayInfo(
    var selectedMediaType: SelectedMediaType = SelectedMediaType.Default,
    var sortBy: SortBy = SortBy.Default,
    var viewBy: ViewBy = ViewBy.Default
)
