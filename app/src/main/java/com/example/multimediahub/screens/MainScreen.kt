package com.example.multimediahub.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.multimediahub.FilesDisplayInfo
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
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Files.route,
        modifier = Modifier.padding(paddingValues = paddingValues)
    ) {
        val modifier = Modifier.padding(8.dp)
        composable(BottomNavItem.Recent.route) { RecentScreen(displayInfo, modifier = modifier) }
        composable(BottomNavItem.Files.route) { FilesScreen(displayInfo, modifier = modifier) }
        composable(BottomNavItem.Folders.route) { FoldersScreen(displayInfo, modifier = modifier) }
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
    val displayInfo = FilesDisplayInfo(selectedMediaType, sortBy, viewBy)
    Surface {
        Scaffold(
            topBar = {
                Head(
                    displayInfo = displayInfo,
                    onMediaTypeClick = { selectedMediaType = it },
                    onSortByClick = {
                        sortBy = when (sortBy) {
                            SortBy.Name -> SortBy.LastModified
                            SortBy.LastModified -> SortBy.Size
                            SortBy.Size -> SortBy.Name
                        }
                    },
                    onViewByClick = {
                        viewBy = when (viewBy) {
                            ViewBy.List -> ViewBy.Grid
                            ViewBy.Grid -> ViewBy.List
                        }
                    }
                )
            },
            bottomBar = { BottomNavigationBar(navController = navController) },
        ) {
            BottomNavGraph(displayInfo, navController = navController, paddingValues = it)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileSearch() {
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    SearchBar(
        query = query,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        onQueryChange = { query = it },
        onSearch = {},
        active = active,
        onActiveChange = { active = it },
        placeholder = { Text("Search a file") },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            if (active) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.clickable {
                        if (query.isNotEmpty()) {
                            query = ""
                        } else {
                            active = false
                        }
                    })
            }
        }
    ) {

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
    onSortByClick: () -> Unit,
    onViewByClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Sort By: $sortBy",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .border(0.5.dp, LocalContentColor.current)
                .padding(vertical = 3.dp, horizontal = 6.dp)
                .clickable { onSortByClick() }
        )
        Icon(
            imageVector = when (viewBy) {
                ViewBy.List -> Icons.Default.GridView
                ViewBy.Grid -> Icons.AutoMirrored.Filled.List
            },
            contentDescription = null,
            modifier = Modifier.clickable { onViewByClick() }
        )
    }
}

@Composable
private fun Head(
    displayInfo: FilesDisplayInfo,
    onMediaTypeClick: (selectedType: SelectedMediaType) -> Unit,
    onSortByClick: () -> Unit,
    onViewByClick: () -> Unit
) {
    Column {
        FileSearch()
        MediaTypeSelector(displayInfo.selectedMediaType, onMediaTypeClick)
        SortAndView(displayInfo.sortBy, displayInfo.viewBy, onSortByClick, onViewByClick)
    }
}