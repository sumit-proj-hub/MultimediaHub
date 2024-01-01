package com.example.multimediahub.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.multimediahub.FilesDisplayInfo
import com.example.multimediahub.ReducedMediaInfo
import com.example.multimediahub.SelectedMediaType
import com.example.multimediahub.SortBy
import com.example.multimediahub.ViewBy

private sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Recent : BottomNavItem(
        route = "recent",
        label = "Recent",
        icon = Icons.Default.History
    )

    object Files : BottomNavItem(
        route = "files",
        label = "Files",
        icon = Icons.Default.FileCopy
    )

    object Folders : BottomNavItem(
        route = "folders",
        label = "Folders",
        icon = Icons.Default.Folder
    )
}

@Composable
private fun BottomNavGraph(
    displayInfo: FilesDisplayInfo,
    navController: NavHostController,
    paddingValues: PaddingValues,
    onBackHandler: () -> Boolean,
    scrollDirectionListener: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Files.route,
        modifier = Modifier.padding(paddingValues = paddingValues)
    ) {
        val modifier = Modifier.padding(8.dp)
        val backHandler = {
            if (onBackHandler()) {
                navController.popBackStack()
                if (navController.currentBackStack.value.isEmpty())
                    (navController.context as Activity).finish()
            }
        }
        composable(BottomNavItem.Recent.route) {
            BackHandler { backHandler() }
            RecentScreen(displayInfo, scrollDirectionListener, modifier = modifier)
        }
        composable(BottomNavItem.Files.route) {
            BackHandler { backHandler() }
            FilesScreen(displayInfo, scrollDirectionListener, modifier = modifier)
        }
        composable(BottomNavItem.Folders.route) {
            BackHandler { backHandler() }
            FoldersScreen(displayInfo, scrollDirectionListener, modifier = modifier)
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val screens = listOf(BottomNavItem.Recent, BottomNavItem.Files, BottomNavItem.Folders)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar(modifier = Modifier.height(60.dp)) {
        screens.forEach {
            val isSelected = it.route == currentRoute
            NavigationBarItem(
                selected = isSelected,
                onClick = { navController.navigate(it.route) },
                icon = { Icon(imageVector = it.icon, contentDescription = it.label) },
                label = { Text(it.label) },
                modifier = Modifier.alpha(if (isSelected) 1.0F else 0.6F),
            )
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var selectedMediaType by rememberSaveable { mutableStateOf(SelectedMediaType.Default) }
    var sortBy by rememberSaveable { mutableStateOf(SortBy.Default) }
    var viewBy by rememberSaveable { mutableStateOf(ViewBy.Default) }
    var isSearchBarActive by rememberSaveable { mutableStateOf(false) }
    var allowSort by rememberSaveable { mutableStateOf(false) }
    var isScrollingDown by remember { mutableStateOf(false) }
    val displayInfo = FilesDisplayInfo(selectedMediaType, sortBy, viewBy)
    LaunchedEffect(Unit) {
        navController.addOnDestinationChangedListener { controller, _, _ ->
            allowSort =
                controller.currentBackStackEntry?.destination?.route != BottomNavItem.Recent.route
        }
    }
    Surface {
        Scaffold(
            topBar = {
                Head(
                    displayInfo = displayInfo,
                    onMediaTypeClick = { selectedMediaType = it },
                    allowSort = allowSort,
                    onSortByClick = { sortBy = it },
                    onViewByClick = {
                        viewBy = when (viewBy) {
                            ViewBy.List -> ViewBy.Grid
                            ViewBy.Grid -> ViewBy.List
                        }
                    },
                    isSearchBarActive = isSearchBarActive,
                    setSearchBarActive = {
                        isSearchBarActive = it
                    }
                )
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = !isSearchBarActive && !isScrollingDown,
                    enter = slideInVertically(
                        animationSpec = tween(durationMillis = 200),
                        initialOffsetY = { it / 2 }),
                    exit = slideOutVertically(
                        animationSpec = tween(durationMillis = 200),
                        targetOffsetY = { it / 2 })
                ) {
                    BottomNavigationBar(navController = navController)
                }
            },
        ) { paddingValues ->
            BottomNavGraph(
                displayInfo,
                navController = navController,
                paddingValues = paddingValues,
                onBackHandler = {
                    if (isSearchBarActive) {
                        isSearchBarActive = false
                        return@BottomNavGraph false
                    }
                    return@BottomNavGraph true
                },
                scrollDirectionListener = {
                    isScrollingDown = it
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileSearch(isSearchBarActive: Boolean, setSearchBarActive: (Boolean) -> Unit) {
    var query by remember { mutableStateOf("") }
    var mediaList by remember { mutableStateOf<List<ReducedMediaInfo>?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val contentResolver = LocalContext.current.contentResolver
    if (!isSearchBarActive)
        query = ""
    LaunchedEffect(isSearchBarActive) {
        mediaList =
            if (isSearchBarActive) ReducedMediaInfo.getReducedMediaList(contentResolver) else null
    }
    SearchBar(
        query = query,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        onQueryChange = { query = it },
        onSearch = { keyboardController?.hide() },
        active = isSearchBarActive,
        onActiveChange = { setSearchBarActive(it) },
        placeholder = { Text("Search a file") },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            if (isSearchBarActive) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.clickable {
                        if (query.isNotEmpty()) {
                            query = ""
                        } else {
                            setSearchBarActive(false)
                        }
                    }
                )
            }
        }
    ) {
        if (query.isEmpty())
            return@SearchBar
        val scrollBarState = rememberLazyListState()
        val context = LocalContext.current
        LazyColumn(
            state = scrollBarState,
            modifier = Modifier.simpleVerticalScrollbar(scrollBarState)
        ) {
            items(mediaList?.filter { it.name.contains(query, true) } ?: emptyList()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp)
                        .clickable { onMediaClick(context, it.mediaType, it.filePath) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    it.MediaIcon(
                        Modifier
                            .size(50.dp)
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = it.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaTypeSelector(
    selectedType: SelectedMediaType,
    onClick: (selectedType: SelectedMediaType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SelectedMediaType.values().forEach {
                SwitchButton(
                    text = it.toString(),
                    isChecked = it == selectedType,
                    onClick = { onClick(it) }
                )
            }
        }
    }
}

@Composable
private fun SwitchButton(
    text: String,
    isChecked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isChecked) Color.DarkGray else MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.5.dp, SolidColor(borderColor)),
        color = if (isChecked) Color.DarkGray else MaterialTheme.colorScheme.background
    ) {
        Text(
            text = text,
            color = if (isChecked) Color.White else borderColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SortAndView(
    sortBy: SortBy,
    viewBy: ViewBy,
    allowSort: Boolean,
    onSortByClick: (SortBy) -> Unit,
    onViewByClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSortByExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (allowSort) {
            Text(
                text = "Sort By: ",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 6.dp, top = 3.dp, bottom = 3.dp)
            )
            Row(
                modifier = modifier
                    .padding(vertical = 3.dp)
                    .clickable { isSortByExpanded = !isSortByExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sortBy.string,
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown Arrow"
                )
                DropdownMenu(
                    expanded = isSortByExpanded,
                    onDismissRequest = { isSortByExpanded = false }) {
                    SortBy.values().forEach {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = it.string,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                onSortByClick(it)
                                isSortByExpanded = false
                            }
                        )
                    }
                }
            }
        } else Spacer(modifier = Modifier)
        Icon(
            imageVector = when (viewBy) {
                ViewBy.List -> Icons.Default.GridView
                ViewBy.Grid -> Icons.AutoMirrored.Filled.List
            },
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.End)
                .clickable { onViewByClick() }
        )
    }
}

@Composable
private fun Head(
    displayInfo: FilesDisplayInfo,
    onMediaTypeClick: (selectedType: SelectedMediaType) -> Unit,
    onSortByClick: (SortBy) -> Unit,
    onViewByClick: () -> Unit,
    allowSort: Boolean,
    isSearchBarActive: Boolean,
    setSearchBarActive: (Boolean) -> Unit
) {
    Column(modifier = Modifier.animateContentSize()) {
        FileSearch(isSearchBarActive, setSearchBarActive)
        MediaTypeSelector(displayInfo.selectedMediaType, onMediaTypeClick)
        SortAndView(
            displayInfo.sortBy,
            displayInfo.viewBy,
            allowSort,
            onSortByClick,
            onViewByClick
        )
    }
}