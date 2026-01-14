package com.debarunlahiri.clippy.ui.screens.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.debarunlahiri.clippy.data.local.entities.ClipType
import com.debarunlahiri.clippy.data.local.entities.ClipboardItem
import com.debarunlahiri.clippy.util.DateFormatter
import java.io.File

/** Main clipboard history screen */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipboardHistoryScreen(
        viewModel: ClipboardHistoryViewModel = viewModel(),
        onNavigateToSettings: () -> Unit,
        onNavigateToDetail: (Long) -> Unit
) {
    val filteredItems by viewModel.filteredItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Clippy") },
                        actions = {
                            // Search icon
                            IconButton(onClick = { /* TODO: Expand search */}) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }

                            // Menu
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                                }

                                DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                            text = { Text("Clear all") },
                                            onClick = {
                                                showMenu = false
                                                showClearDialog = true
                                            },
                                            leadingIcon = {
                                                Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = null
                                                )
                                            }
                                    )
                                    DropdownMenuItem(
                                            text = { Text("Settings") },
                                            onClick = {
                                                showMenu = false
                                                onNavigateToSettings()
                                            },
                                            leadingIcon = {
                                                Icon(
                                                        Icons.Default.Settings,
                                                        contentDescription = null
                                                )
                                            }
                                    )
                                }
                            }
                        }
                )
            }
    ) { paddingValues ->
        if (filteredItems.isEmpty()) {
            // Empty state
            EmptyState(modifier = Modifier.fillMaxSize().padding(paddingValues))
        } else {
            // Clipboard items list
            LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = filteredItems, key = { it.id }) { item ->
                    ClipboardItemCard(
                            item = item,
                            onCopy = { viewModel.copyToClipboard(item) },
                            onDelete = { viewModel.deleteItem(item) },
                            onTogglePin = { viewModel.togglePin(item) },
                            onShare = { viewModel.shareItem(item) },
                            onClick = { onNavigateToDetail(item.id) }
                    )
                }
            }
        }
    }

    // Clear all confirmation dialog
    if (showClearDialog) {
        AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Clear all items?") },
                text = {
                    Text("This will delete all clipboard history. This action cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                            onClick = {
                                viewModel.clearAll()
                                showClearDialog = false
                            }
                    ) { Text("Clear") }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
                }
        )
    }
}

/** Empty state when no clipboard items */
@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
                text = "No clipboard history yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
                text = "Copy something to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

/** Card for displaying a single clipboard item */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ClipboardItemCard(
        item: ClipboardItem,
        onCopy: () -> Unit,
        onDelete: () -> Unit,
        onTogglePin: () -> Unit,
        onShare: () -> Unit,
        onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
            modifier =
                    Modifier.fillMaxWidth()
                            .combinedClickable(onClick = onCopy, onLongClick = { showMenu = true }),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Header row with type icon and timestamp
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Type icon
                    Icon(
                            imageVector = getIconForType(item.type),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                    )

                    // Timestamp
                    Text(
                            text = DateFormatter.formatRelativeTime(item.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Pin icon
                if (item.isPinned) {
                    Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content preview
            when (item.type) {
                ClipType.IMAGE -> {
                    // Show image thumbnail
                    if (item.imageUri != null) {
                        AsyncImage(
                                model = File(item.imageUri),
                                contentDescription = "Image preview",
                                modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    }
                }
                ClipType.URI -> {
                    // Show URI with link icon
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                                text = item.uriString ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                else -> {
                    // Show text preview
                    Text(
                            text = item.primaryText ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Context menu
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                    text = { Text("Copy") },
                    onClick = {
                        showMenu = false
                        onCopy()
                    },
                    leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) }
            )
            DropdownMenuItem(
                    text = { Text(if (item.isPinned) "Unpin" else "Pin") },
                    onClick = {
                        showMenu = false
                        onTogglePin()
                    },
                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
            )
            DropdownMenuItem(
                    text = { Text("Share") },
                    onClick = {
                        showMenu = false
                        onShare()
                    },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
            )
            DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
            )
        }
    }
}

/** Get icon for clipboard item type */
private fun getIconForType(type: ClipType) =
        when (type) {
            ClipType.TEXT -> Icons.Default.Edit
            ClipType.IMAGE -> Icons.Default.Image
            ClipType.URI -> Icons.Default.Info
            ClipType.HTML -> Icons.Default.Edit
            ClipType.MULTIPLE -> Icons.Default.List
            ClipType.OTHER -> Icons.Default.Info
        }
