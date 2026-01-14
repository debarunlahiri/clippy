package com.debarunlahiri.clippy.data.local

import androidx.room.*
import com.debarunlahiri.clippy.data.local.entities.ClipboardItem
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for clipboard items
 */
@Dao
interface ClipboardDao {
    
    /**
     * Insert a new clipboard item
     * @return The ID of the inserted item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClipboardItem): Long
    
    /**
     * Get all clipboard items ordered by timestamp (newest first)
     */
    @Query("SELECT * FROM clipboard_items ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ClipboardItem>>
    
    /**
     * Get paginated clipboard items
     */
    @Query("SELECT * FROM clipboard_items ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getAllPaginated(limit: Int, offset: Int): List<ClipboardItem>
    
    /**
     * Get a single clipboard item by ID
     */
    @Query("SELECT * FROM clipboard_items WHERE id = :id")
    suspend fun getById(id: Long): ClipboardItem?
    
    /**
     * Get all pinned items
     */
    @Query("SELECT * FROM clipboard_items WHERE isPinned = 1 ORDER BY timestamp DESC")
    fun getPinnedItems(): Flow<List<ClipboardItem>>
    
    /**
     * Search clipboard items by text content
     */
    @Query("""
        SELECT * FROM clipboard_items 
        WHERE primaryText LIKE '%' || :query || '%' 
           OR fullText LIKE '%' || :query || '%'
           OR uriString LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun search(query: String): Flow<List<ClipboardItem>>
    
    /**
     * Delete a specific item
     */
    @Delete
    suspend fun delete(item: ClipboardItem)
    
    /**
     * Delete an item by ID
     */
    @Query("DELETE FROM clipboard_items WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    /**
     * Delete all items
     */
    @Query("DELETE FROM clipboard_items")
    suspend fun deleteAll()
    
    /**
     * Delete oldest non-pinned items to maintain history limit
     */
    @Query("""
        DELETE FROM clipboard_items 
        WHERE id IN (
            SELECT id FROM clipboard_items 
            WHERE isPinned = 0 
            ORDER BY timestamp ASC 
            LIMIT :count
        )
    """)
    suspend fun deleteOldestNonPinned(count: Int)
    
    /**
     * Update the pinned status of an item
     */
    @Query("UPDATE clipboard_items SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinned(id: Long, isPinned: Boolean)
    
    /**
     * Get the total count of items
     */
    @Query("SELECT COUNT(*) FROM clipboard_items")
    suspend fun getCount(): Int
    
    /**
     * Get the count of non-pinned items
     */
    @Query("SELECT COUNT(*) FROM clipboard_items WHERE isPinned = 0")
    suspend fun getNonPinnedCount(): Int
    
    /**
     * Get the most recent item (for duplicate detection)
     */
    @Query("SELECT * FROM clipboard_items ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecent(): ClipboardItem?
}
