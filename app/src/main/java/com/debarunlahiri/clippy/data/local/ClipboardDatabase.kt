package com.debarunlahiri.clippy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.debarunlahiri.clippy.data.local.entities.ClipboardItem

/**
 * Room database for clipboard items
 */
@Database(
    entities = [ClipboardItem::class],
    version = 1,
    exportSchema = false
)
abstract class ClipboardDatabase : RoomDatabase() {
    
    abstract fun clipboardDao(): ClipboardDao
    
    companion object {
        @Volatile
        private var INSTANCE: ClipboardDatabase? = null
        
        private const val DATABASE_NAME = "clippy_database"
        
        /**
         * Get the singleton database instance
         */
        fun getInstance(context: Context): ClipboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClipboardDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
