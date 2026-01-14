package com.debarunlahiri.clippy.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a clipboard item stored in the database
 */
@Entity(tableName = "clipboard_items")
data class ClipboardItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Timestamp when the item was copied (milliseconds since epoch) */
    val timestamp: Long,
    
    /** Type of clipboard content */
    val type: ClipType,
    
    /** Preview text for display in list (truncated if necessary) */
    val primaryText: String?,
    
    /** Full text content (for TEXT type) */
    val fullText: String?,
    
    /** HTML content (if available) */
    val htmlText: String?,
    
    /** Local file path for saved images */
    val imageUri: String?,
    
    /** URI string for URI type items */
    val uriString: String?,
    
    /** Comma-separated MIME types */
    val mimeTypes: String,
    
    /** Whether this item is pinned/favorited */
    val isPinned: Boolean = false,
    
    /** Number of items if this is a multi-item clip */
    val itemCount: Int = 1
)

/**
 * Enum representing different types of clipboard content
 */
enum class ClipType {
    TEXT,       // Plain text
    IMAGE,      // Image/bitmap
    URI,        // Web link or file URI
    HTML,       // HTML formatted text
    MULTIPLE,   // Multiple items in one clip
    OTHER       // Other/unknown MIME types
}
