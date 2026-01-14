package com.debarunlahiri.clippy.data.repository

import com.debarunlahiri.clippy.data.local.ClipboardDao
import com.debarunlahiri.clippy.data.local.entities.ClipboardItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repository for clipboard items
 * Provides a clean API for accessing clipboard data and enforces business logic
 */
class ClipboardRepository(
    private val clipboardDao: ClipboardDao,
    private val historyLimit: Int = 100
) {
    
    /**
     * Get all clipboard items as a Flow
     */
    fun getAllItems(): Flow<List<ClipboardItem>> {
        return clipboardDao.getAll()
    }
    
    /**
     * Get paginated items
     */
    suspend fun getPaginatedItems(limit: Int, offset: Int): List<ClipboardItem> {
        return clipboardDao.getAllPaginated(limit, offset)
    }
    
    /**
     * Get a single item by ID
     */
    suspend fun getItemById(id: Long): ClipboardItem? {
        return clipboardDao.getById(id)
    }
    
    /**
     * Get all pinned items
     */
    fun getPinnedItems(): Flow<List<ClipboardItem>> {
        return clipboardDao.getPinnedItems()
    }
    
    /**
     * Search items by query
     */
    fun searchItems(query: String): Flow<List<ClipboardItem>> {
        return clipboardDao.search(query)
    }
    
    /**
     * Insert a new clipboard item
     * Enforces history limit by deleting oldest non-pinned items
     * @return The ID of the inserted item, or null if it's a duplicate
     */
    suspend fun insertItem(item: ClipboardItem): Long? {
        // Check for duplicates
        if (isDuplicate(item)) {
            return null
        }
        
        // Insert the new item
        val id = clipboardDao.insert(item)
        
        // Enforce history limit
        enforceHistoryLimit()
        
        return id
    }
    
    /**
     * Delete an item
     */
    suspend fun deleteItem(item: ClipboardItem) {
        clipboardDao.delete(item)
    }
    
    /**
     * Delete an item by ID
     */
    suspend fun deleteItemById(id: Long) {
        clipboardDao.deleteById(id)
    }
    
    /**
     * Delete all items
     */
    suspend fun deleteAllItems() {
        clipboardDao.deleteAll()
    }
    
    /**
     * Toggle the pinned status of an item
     */
    suspend fun togglePinned(id: Long, isPinned: Boolean) {
        clipboardDao.updatePinned(id, isPinned)
    }
    
    /**
     * Get the total count of items
     */
    suspend fun getItemCount(): Int {
        return clipboardDao.getCount()
    }
    
    /**
     * Check if a new item is a duplicate of the most recent item
     */
    private suspend fun isDuplicate(newItem: ClipboardItem): Boolean {
        val mostRecent = clipboardDao.getMostRecent() ?: return false
        
        // Compare based on type
        return when (newItem.type) {
            com.debarunlahiri.clippy.data.local.entities.ClipType.TEXT,
            com.debarunlahiri.clippy.data.local.entities.ClipType.HTML -> {
                newItem.fullText == mostRecent.fullText
            }
            com.debarunlahiri.clippy.data.local.entities.ClipType.URI -> {
                newItem.uriString == mostRecent.uriString
            }
            com.debarunlahiri.clippy.data.local.entities.ClipType.IMAGE -> {
                // For images, we can't easily compare content, so we'll allow duplicates
                // In a more advanced implementation, we could compare image hashes
                false
            }
            else -> false
        }
    }
    
    /**
     * Enforce the history limit by deleting oldest non-pinned items
     */
    private suspend fun enforceHistoryLimit() {
        val nonPinnedCount = clipboardDao.getNonPinnedCount()
        if (nonPinnedCount > historyLimit) {
            val toDelete = nonPinnedCount - historyLimit
            clipboardDao.deleteOldestNonPinned(toDelete)
        }
    }
}
