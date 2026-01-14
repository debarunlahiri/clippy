package com.debarunlahiri.clippy.service

import android.app.*
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.debarunlahiri.clippy.MainActivity
import com.debarunlahiri.clippy.R
import com.debarunlahiri.clippy.data.local.ClipboardDatabase
import com.debarunlahiri.clippy.data.repository.ClipboardRepository
import com.debarunlahiri.clippy.util.ClipboardHelper
import com.debarunlahiri.clippy.util.Constants
import com.debarunlahiri.clippy.util.ImageHelper
import kotlinx.coroutines.*

/** Foreground service that monitors clipboard changes */
class ClipboardMonitorService : Service() {

    private val TAG = "ClipboardMonitorService"

    private lateinit var clipboardManager: ClipboardManager
    private lateinit var repository: ClipboardRepository
    private val imageHelper = ImageHelper()

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val clipboardListener =
            ClipboardManager.OnPrimaryClipChangedListener { handleClipboardChange() }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        // Initialize clipboard manager
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Initialize repository
        val database = ClipboardDatabase.getInstance(applicationContext)
        repository = ClipboardRepository(database.clipboardDao())

        // Register clipboard listener
        clipboardManager.addPrimaryClipChangedListener(clipboardListener)

        // Create notification channel
        createNotificationChannel()

        // Start foreground with notification
        startForeground(Constants.CLIPBOARD_SERVICE_NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        when (intent?.action) {
            Constants.ACTION_STOP_SERVICE -> {
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")

        // Unregister clipboard listener
        clipboardManager.removePrimaryClipChangedListener(clipboardListener)

        // Cancel all coroutines
        serviceScope.cancel()
    }

    /** Handle clipboard change event */
    private fun handleClipboardChange() {
        serviceScope.launch {
            try {
                val clipData = clipboardManager.primaryClip
                if (clipData == null || clipData.itemCount == 0) {
                    Log.d(TAG, "Empty clipboard data, ignoring")
                    return@launch
                }

                Log.d(TAG, "Clipboard changed, processing...")

                // Convert ClipData to ClipboardItem
                val clipboardItem =
                        ClipboardHelper.clipDataToClipboardItem(
                                applicationContext,
                                clipData,
                                imageHelper
                        )

                if (clipboardItem == null) {
                    Log.d(TAG, "Failed to convert ClipData to ClipboardItem")
                    return@launch
                }

                // Insert into database (repository handles duplicate detection)
                val id = repository.insertItem(clipboardItem)

                if (id != null) {
                    Log.d(TAG, "Clipboard item saved with ID: $id")
                } else {
                    Log.d(TAG, "Duplicate item, not saved")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing clipboard change", e)
            }
        }
    }

    /** Create notification channel for Android O+ */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                    NotificationChannel(
                                    Constants.CLIPBOARD_SERVICE_CHANNEL_ID,
                                    Constants.CLIPBOARD_SERVICE_CHANNEL_NAME,
                                    NotificationManager.IMPORTANCE_LOW
                            )
                            .apply {
                                description = "Monitors clipboard for changes"
                                setShowBadge(false)
                            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /** Create foreground service notification */
    private fun createNotification(): Notification {
        // Intent to open the app
        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent =
                PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE)

        // Intent to stop the service
        val stopIntent =
                Intent(this, ClipboardMonitorService::class.java).apply {
                    action = Constants.ACTION_STOP_SERVICE
                }
        val stopPendingIntent =
                PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, Constants.CLIPBOARD_SERVICE_CHANNEL_ID)
                .setContentTitle("Clippy is monitoring")
                .setContentText("Clipboard history is being recorded")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(openAppPendingIntent)
                .addAction(android.R.drawable.ic_delete, "Stop", stopPendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
    }

    companion object {
        /** Start the clipboard monitoring service */
        fun start(context: Context) {
            val intent = Intent(context, ClipboardMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /** Stop the clipboard monitoring service */
        fun stop(context: Context) {
            val intent = Intent(context, ClipboardMonitorService::class.java)
            context.stopService(intent)
        }
    }
}
