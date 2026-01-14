# Clippy Clipboard Manager - Implementation Plan

**Version**: 1.0  
**Platform**: Android (Kotlin + Jetpack Compose)  
**Date**: January 14, 2026

## Project Overview

Clippy is a comprehensive Android clipboard manager that monitors system-wide clipboard changes, maintains persistent history, and supports multiple data types (text, images, URIs). The app uses MVVM architecture with Jetpack Compose for UI.

---

## Phase 1: Project Setup & Dependencies

### 1.1 Update build.gradle.kts (App Level)

Add required dependencies:

- **Room** (database): `room-runtime`, `room-ktx`, `room-compiler` (KSP)
- **Coroutines**: Already included via lifecycle-runtime-ktx
- **Coil** (image loading): `coil-compose`
- **DataStore** (preferences): `datastore-preferences`
- **WorkManager** (optional for reliability): `work-runtime-ktx`

### 1.2 Update build.gradle.kts (Project Level)

- Add KSP plugin for Room annotation processing
- Ensure Kotlin version supports latest Compose

### 1.3 Update AndroidManifest.xml

Add permissions:

- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_DATA_SYNC` (Android 14+)
- `POST_NOTIFICATIONS` (Android 13+)
- `RECEIVE_BOOT_COMPLETED` (for auto-start)

---

## Phase 2: Data Layer

### 2.1 Database Schema (Room)

**Entity: ClipboardItem**

```kotlin
@Entity(tableName = "clipboard_items")
data class ClipboardItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val type: ClipType, // TEXT, IMAGE, URI, HTML, MULTIPLE
    val primaryText: String?, // Preview text
    val fullText: String?, // Full text content
    val htmlText: String?, // HTML content
    val imageUri: String?, // Local file path for images
    val uriString: String?, // URI content
    val mimeTypes: String, // Comma-separated MIME types
    val isPinned: Boolean = false,
    val itemCount: Int = 1 // For multiple items in one clip
)

enum class ClipType {
    TEXT, IMAGE, URI, HTML, MULTIPLE, OTHER
}
```

**DAO: ClipboardDao**

- `insert(item: ClipboardItem): Long`
- `getAll(): Flow<List<ClipboardItem>>`
- `getAllPaginated(limit: Int, offset: Int): List<ClipboardItem>`
- `getById(id: Long): ClipboardItem?`
- `delete(item: ClipboardItem)`
- `deleteAll()`
- `deleteOldestNonPinned(limit: Int)`
- `updatePinned(id: Long, isPinned: Boolean)`
- `search(query: String): Flow<List<ClipboardItem>>`
- `getPinnedItems(): Flow<List<ClipboardItem>>`

**Database: ClipboardDatabase**

- Abstract class extending RoomDatabase
- Singleton pattern with companion object

### 2.2 Repository Pattern

**ClipboardRepository**

- Wraps DAO operations
- Handles business logic (duplicate detection, history limit enforcement)
- Provides clean API for ViewModels

---

## Phase 3: Clipboard Monitoring Service

### 3.1 ClipboardMonitorService (Foreground Service)

**Responsibilities:**

- Register `OnPrimaryClipChangedListener`
- Detect clipboard changes system-wide
- Extract ClipData and process different types
- Save to database via repository
- Show foreground notification
- Handle service lifecycle

**Key Methods:**

- `onCreate()`: Initialize listener, start foreground
- `onStartCommand()`: Handle start/stop commands
- `onDestroy()`: Unregister listener, cleanup
- `processClipData(clipData: ClipData)`: Main processing logic
- `saveClipboardItem(clipData: ClipData)`: Save to database
- `isDuplicate(clipData: ClipData): Boolean`: Check if same as last item

### 3.2 ClipData Processing

**Handle different types:**

- **Text**: Extract text, check for HTML
- **Image**: Save bitmap to internal storage, generate thumbnail
- **URI**: Extract URI string, persist permissions if needed
- **Multiple items**: Store as grouped entry

### 3.3 Foreground Notification

- Persistent notification showing "Clippy is monitoring clipboard"
- Notification channel setup
- Tap to open app
- Action buttons: Pause/Resume, Stop

### 3.4 Boot Receiver

**BootReceiver (BroadcastReceiver)**

- Listen for `BOOT_COMPLETED`
- Start ClipboardMonitorService if enabled in settings
- Check battery optimization status

---

## Phase 4: UI Layer (Jetpack Compose)

### 4.1 Navigation Setup

**NavHost with routes:**

- `home` - Main clipboard history screen
- `settings` - Settings screen
- `item_detail/{itemId}` - Detail view for single item

### 4.2 Main Screen (ClipboardHistoryScreen)

**Components:**

- **TopAppBar**: Title, search icon, menu (clear all, settings)
- **SearchBar**: Filter history by text
- **LazyColumn**: Scrollable list of clipboard items
- **FAB**: Quick actions (clear all, settings)
- **Empty State**: Illustration when no items

**ClipboardItemCard (Composable):**

- Preview based on type (text snippet, image thumbnail, URI icon)
- Timestamp (relative: "2 minutes ago")
- Pin/favorite icon
- Tap to copy
- Long-press for context menu (delete, pin, share)

**ViewModel: ClipboardHistoryViewModel**

- `clipboardItems: StateFlow<List<ClipboardItem>>`
- `searchQuery: MutableStateFlow<String>`
- `filteredItems: StateFlow<List<ClipboardItem>>`
- `copyToClipboard(item: ClipboardItem)`
- `deleteItem(item: ClipboardItem)`
- `togglePin(item: ClipboardItem)`
- `clearAll()`
- `shareItem(item: ClipboardItem)`

### 4.3 Item Detail Screen

**Full view of single clipboard item:**

- Full text (scrollable)
- Full-size image (zoomable)
- URI with action buttons (open, share)
- Metadata (timestamp, MIME types, size)
- Actions: Copy, Delete, Pin, Share

### 4.4 Settings Screen

**Preferences (using DataStore):**

- Enable/disable service
- History limit (50, 100, 200, unlimited)
- Auto-clear after X days (7, 30, 90, never)
- Show notification for new clips
- Theme (Light, Dark, System)
- Start on boot
- About section (version, licenses)

**ViewModel: SettingsViewModel**

- Read/write preferences via DataStore
- Start/stop service
- Clear history

---

## Phase 5: Core Features Implementation

### 5.1 Copy to Clipboard

- Use `ClipboardManager.setPrimaryClip()`
- Show toast confirmation
- Optionally show brief notification

### 5.2 Duplicate Detection

- Compare new clip with most recent item
- Check text content, URI, or image hash
- Skip saving if duplicate

### 5.3 History Limit Enforcement

- When inserting new item, check count
- If over limit, delete oldest non-pinned items
- Pinned items never auto-deleted

### 5.4 Image Handling

- Save bitmaps to `context.filesDir/images/`
- Generate thumbnails (200x200dp)
- Use Coil for efficient loading
- Clean up orphaned images on delete

### 5.5 URI Permission Persistence

- For content:// URIs, call `takePersistableUriPermission()`
- Handle permission errors gracefully

### 5.6 Search & Filter

- Real-time search in text content
- Filter by type (text, images, URIs)
- Sort by date or pinned status

---

## Phase 6: Polish & Optimization

### 6.1 Material 3 Design

- Use Material 3 color scheme
- Dynamic colors (Android 12+)
- Proper elevation and shadows
- Smooth animations and transitions

### 6.2 Performance

- Pagination for large history (load 50 at a time)
- Image thumbnail caching
- Efficient database queries with indexes
- Avoid main thread blocking

### 6.3 Accessibility

- Content descriptions for all icons
- Semantic properties for screen readers
- Scalable text sizes
- High contrast support

### 6.4 Error Handling

- Handle clipboard access errors
- Handle storage errors (disk full)
- Handle permission denials
- Show user-friendly error messages

### 6.5 Testing

- Unit tests for Repository and ViewModels
- Instrumented tests for Database
- UI tests for main flows (copy, delete, pin)

---

## Phase 7: Advanced Features (Optional for v1.0)

### 7.1 Quick Paste Overlay

- Floating window with recent clips
- Requires `SYSTEM_ALERT_WINDOW` permission
- Quick access from any app

### 7.2 Categories/Tags

- Auto-categorize (links, emails, phone numbers)
- Manual tagging
- Filter by category

### 7.3 Export/Import

- Export history to JSON
- Import from backup
- Share history file

---

## Implementation Order (Recommended)

1. **Setup** (Phase 1): Dependencies, permissions
2. **Database** (Phase 2): Room entities, DAOs, repository
3. **Service** (Phase 3.1-3.2): Basic clipboard monitoring
4. **UI Foundation** (Phase 4.1-4.2): Navigation, main screen, basic list
5. **Core Features** (Phase 5.1-5.3): Copy, delete, duplicate detection
6. **Service Polish** (Phase 3.3-3.4): Notification, boot receiver
7. **UI Polish** (Phase 4.3-4.4): Detail screen, settings
8. **Advanced Features** (Phase 5.4-5.6): Images, search, filters
9. **Final Polish** (Phase 6): Design, performance, accessibility
10. **Testing & Deployment**: QA, bug fixes, release

---

## File Structure

```
app/src/main/java/com/debarunlahiri/clippy/
├── data/
│   ├── local/
│   │   ├── ClipboardDatabase.kt
│   │   ├── ClipboardDao.kt
│   │   └── entities/
│   │       └── ClipboardItem.kt
│   ├── repository/
│   │   └── ClipboardRepository.kt
│   └── preferences/
│       └── SettingsDataStore.kt
├── service/
│   ├── ClipboardMonitorService.kt
│   └── BootReceiver.kt
├── ui/
│   ├── screens/
│   │   ├── history/
│   │   │   ├── ClipboardHistoryScreen.kt
│   │   │   └── ClipboardHistoryViewModel.kt
│   │   ├── detail/
│   │   │   ├── ItemDetailScreen.kt
│   │   │   └── ItemDetailViewModel.kt
│   │   └── settings/
│   │       ├── SettingsScreen.kt
│   │       └── SettingsViewModel.kt
│   ├── components/
│   │   ├── ClipboardItemCard.kt
│   │   ├── EmptyState.kt
│   │   └── SearchBar.kt
│   ├── navigation/
│   │   └── NavGraph.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── util/
│   ├── ClipboardHelper.kt
│   ├── ImageHelper.kt
│   ├── DateFormatter.kt
│   └── Constants.kt
└── MainActivity.kt
```

---

## Key Technical Decisions

1. **Jetpack Compose** for modern, declarative UI
2. **Room** for robust local database
3. **MVVM** architecture for separation of concerns
4. **Foreground Service** for reliable background monitoring
5. **Coil** for efficient image loading
6. **DataStore** for type-safe preferences
7. **Kotlin Coroutines & Flow** for async operations
8. **Material 3** for modern design language

---

## Success Criteria

- ✅ Captures all clipboard changes system-wide
- ✅ Supports text, images, and URIs
- ✅ Runs reliably in background
- ✅ Smooth, responsive UI
- ✅ Low battery and memory usage
- ✅ No crashes or data loss
- ✅ Intuitive user experience
- ✅ Proper permission handling
- ✅ Accessible to all users

---

## Next Steps

1. Review and approve this implementation plan
2. Begin Phase 1: Update dependencies and permissions
3. Implement database layer (Phase 2)
4. Build clipboard monitoring service (Phase 3)
5. Create UI screens (Phase 4)
6. Integrate and test all components
7. Polish and optimize
8. Release v1.0

**Estimated Timeline**: 2-3 weeks for full implementation and testing.
