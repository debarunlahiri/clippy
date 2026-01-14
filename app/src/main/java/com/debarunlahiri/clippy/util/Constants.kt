package com.debarunlahiri.clippy.util

/**
 * Constants used throughout the application
 */
object Constants {
    
    // Database
    const val DEFAULT_HISTORY_LIMIT = 100
    const val MAX_PREVIEW_TEXT_LENGTH = 200
    
    // Service
    const val CLIPBOARD_SERVICE_NOTIFICATION_ID = 1001
    const val CLIPBOARD_SERVICE_CHANNEL_ID = "clipboard_monitor_channel"
    const val CLIPBOARD_SERVICE_CHANNEL_NAME = "Clipboard Monitor"
    
    // Actions
    const val ACTION_START_SERVICE = "com.debarunlahiri.clippy.START_SERVICE"
    const val ACTION_STOP_SERVICE = "com.debarunlahiri.clippy.STOP_SERVICE"
    
    // Image storage
    const val IMAGES_DIR = "clipboard_images"
    const val THUMBNAILS_DIR = "clipboard_thumbnails"
    const val THUMBNAIL_SIZE = 200 // pixels
    const val MAX_IMAGE_SIZE = 10 * 1024 * 1024 // 10 MB
    
    // Preferences
    const val PREFS_NAME = "clippy_preferences"
    const val PREF_SERVICE_ENABLED = "service_enabled"
    const val PREF_HISTORY_LIMIT = "history_limit"
    const val PREF_AUTO_CLEAR_DAYS = "auto_clear_days"
    const val PREF_SHOW_NOTIFICATIONS = "show_notifications"
    const val PREF_START_ON_BOOT = "start_on_boot"
    const val PREF_THEME = "theme"
    
    // Theme values
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"
    const val THEME_SYSTEM = "system"
    
    // Navigation
    const val ROUTE_HOME = "home"
    const val ROUTE_SETTINGS = "settings"
    const val ROUTE_ITEM_DETAIL = "item_detail"
    const val ARG_ITEM_ID = "itemId"
    
    // MIME types
    const val MIME_TYPE_TEXT = "text/plain"
    const val MIME_TYPE_HTML = "text/html"
    const val MIME_TYPE_IMAGE = "image/*"
}
