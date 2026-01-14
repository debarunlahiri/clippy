package com.debarunlahiri.clippy.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/** Receiver that starts the clipboard monitoring service on device boot */
class BootReceiver : BroadcastReceiver() {

    private val TAG = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, starting clipboard service")

            // TODO: Check if service is enabled in preferences
            // For now, we'll always start the service
            ClipboardMonitorService.start(context)
        }
    }
}
