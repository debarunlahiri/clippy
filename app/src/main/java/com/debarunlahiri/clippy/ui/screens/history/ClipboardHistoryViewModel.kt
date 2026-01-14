package com.debarunlahiri.clippy.ui.screens.history

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.debarunlahiri.clippy.data.local.ClipboardDatabase
import com.debarunlahiri.clippy.data.local.entities.ClipboardItem
import com.debarunlahiri.clippy.data.repository.ClipboardRepository
import com.debarunlahiri.clippy.util.ClipboardHelper
import com.debarunlahiri.clippy.util.ImageHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** ViewModel for the clipboard history screen */
class ClipboardHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClipboardRepository
    private val imageHelper = ImageHelper()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // All clipboard items
    private val allItems: StateFlow<List<ClipboardItem>>

    // Filtered items based on search query
    val filteredItems: StateFlow<List<ClipboardItem>>

    init {
        val database = ClipboardDatabase.getInstance(application)
        repository = ClipboardRepository(database.clipboardDao())

        allItems =
                repository
                        .getAllItems()
                        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        filteredItems =
                combine(allItems, searchQuery) { items, query ->
                            if (query.isBlank()) {
                                items
                            } else {
                                items.filter { item ->
                                    item.primaryText?.contains(query, ignoreCase = true) == true ||
                                            item.fullText?.contains(query, ignoreCase = true) ==
                                                    true ||
                                            item.uriString?.contains(query, ignoreCase = true) ==
                                                    true
                                }
                            }
                        }
                        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /** Update search query */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** Copy an item back to the clipboard */
    fun copyToClipboard(item: ClipboardItem) {
        val context = getApplication<Application>()
        val clipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clipData = ClipboardHelper.clipboardItemToClipData(item)
        clipboardManager.setPrimaryClip(clipData)

        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    /** Delete an item */
    fun deleteItem(item: ClipboardItem) {
        viewModelScope.launch {
            // Delete associated image if exists
            if (item.imageUri != null) {
                imageHelper.deleteImage(getApplication(), item.imageUri)
            }

            repository.deleteItem(item)
            Toast.makeText(getApplication(), "Item deleted", Toast.LENGTH_SHORT).show()
        }
    }

    /** Toggle pin status of an item */
    fun togglePin(item: ClipboardItem) {
        viewModelScope.launch {
            repository.togglePinned(item.id, !item.isPinned)
            val message = if (item.isPinned) "Unpinned" else "Pinned"
            Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
        }
    }

    /** Clear all items */
    fun clearAll() {
        viewModelScope.launch {
            // Get all items to delete their images
            val items = allItems.value
            items.forEach { item ->
                if (item.imageUri != null) {
                    imageHelper.deleteImage(getApplication(), item.imageUri)
                }
            }

            repository.deleteAllItems()
            Toast.makeText(getApplication(), "All items cleared", Toast.LENGTH_SHORT).show()
        }
    }

    /** Share an item */
    fun shareItem(item: ClipboardItem) {
        // TODO: Implement share functionality
        Toast.makeText(getApplication(), "Share functionality coming soon", Toast.LENGTH_SHORT)
                .show()
    }
}
