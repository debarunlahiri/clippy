package com.debarunlahiri.clippy.util

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.net.Uri
import com.debarunlahiri.clippy.data.local.entities.ClipType
import com.debarunlahiri.clippy.data.local.entities.ClipboardItem

/** Helper class for clipboard operations */
object ClipboardHelper {

    /** Convert ClipData to ClipboardItem for database storage */
    fun clipDataToClipboardItem(
            context: Context,
            clipData: ClipData,
            imageHelper: ImageHelper
    ): ClipboardItem? {
        if (clipData.itemCount == 0) return null

        val description = clipData.description
        val timestamp = System.currentTimeMillis()
        val mimeTypes = getMimeTypes(description)
        val itemCount = clipData.itemCount

        // Determine the type and extract content
        val (type, primaryText, fullText, htmlText, imageUri, uriString) =
                extractClipContent(context, clipData, imageHelper)

        return ClipboardItem(
                timestamp = timestamp,
                type = type,
                primaryText = primaryText,
                fullText = fullText,
                htmlText = htmlText,
                imageUri = imageUri,
                uriString = uriString,
                mimeTypes = mimeTypes,
                itemCount = itemCount
        )
    }

    /** Extract content from ClipData based on its type */
    private fun extractClipContent(
            context: Context,
            clipData: ClipData,
            imageHelper: ImageHelper
    ): ClipContent {
        val description = clipData.description
        val firstItem = clipData.getItemAt(0)

        // Check for image
        if (description.hasMimeType("image/*")) {
            val uri = firstItem.uri
            if (uri != null) {
                val savedPath = imageHelper.saveImageFromUri(context, uri)
                val preview = "Image"
                return ClipContent(
                        type = ClipType.IMAGE,
                        primaryText = preview,
                        imageUri = savedPath
                )
            }
        }

        // Check for URI
        val uri = firstItem.uri
        if (uri != null) {
            val uriString = uri.toString()
            val preview = uri.lastPathSegment ?: uriString
            return ClipContent(
                    type = ClipType.URI,
                    primaryText = truncateText(preview),
                    uriString = uriString
            )
        }

        // Check for HTML text
        val htmlText = firstItem.htmlText
        if (htmlText != null) {
            val plainText = firstItem.text?.toString() ?: ""
            return ClipContent(
                    type = ClipType.HTML,
                    primaryText = truncateText(plainText),
                    fullText = plainText,
                    htmlText = htmlText
            )
        }

        // Plain text
        val text = firstItem.text?.toString()
        if (text != null) {
            return ClipContent(
                    type = ClipType.TEXT,
                    primaryText = truncateText(text),
                    fullText = text
            )
        }

        // Multiple items or unknown
        if (clipData.itemCount > 1) {
            val preview = "Multiple items (${clipData.itemCount})"
            return ClipContent(type = ClipType.MULTIPLE, primaryText = preview)
        }

        // Unknown/Other
        return ClipContent(type = ClipType.OTHER, primaryText = "Unknown content")
    }

    /** Get comma-separated MIME types from ClipDescription */
    private fun getMimeTypes(description: ClipDescription): String {
        val mimeTypes = mutableListOf<String>()
        for (i in 0 until description.mimeTypeCount) {
            mimeTypes.add(description.getMimeType(i))
        }
        return mimeTypes.joinToString(", ")
    }

    /** Truncate text for preview */
    private fun truncateText(text: String): String {
        return if (text.length > Constants.MAX_PREVIEW_TEXT_LENGTH) {
            text.take(Constants.MAX_PREVIEW_TEXT_LENGTH) + "..."
        } else {
            text
        }
    }

    /** Convert ClipboardItem back to ClipData for copying to clipboard */
    fun clipboardItemToClipData(item: ClipboardItem): ClipData {
        return when (item.type) {
            ClipType.TEXT -> {
                ClipData.newPlainText("text", item.fullText ?: item.primaryText ?: "")
            }
            ClipType.HTML -> {
                ClipData.newHtmlText(
                        "html",
                        item.fullText ?: item.primaryText ?: "",
                        item.htmlText ?: ""
                )
            }
            ClipType.URI -> {
                val uri = Uri.parse(item.uriString ?: "")
                ClipData.newUri(null, "uri", uri)
            }
            ClipType.IMAGE -> {
                val uri = Uri.parse(item.imageUri ?: "")
                ClipData.newUri(null, "image", uri)
            }
            else -> {
                ClipData.newPlainText("text", item.primaryText ?: "")
            }
        }
    }
}

/** Data class to hold extracted clip content */
private data class ClipContent(
        val type: ClipType,
        val primaryText: String? = null,
        val fullText: String? = null,
        val htmlText: String? = null,
        val imageUri: String? = null,
        val uriString: String? = null
)
