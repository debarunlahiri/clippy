package com.debarunlahiri.clippy.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/** Helper class for image operations */
class ImageHelper {

    private val TAG = "ImageHelper"

    /**
     * Save an image from URI to internal storage
     * @return The file path of the saved image, or null if failed
     */
    fun saveImageFromUri(context: Context, uri: Uri): String? {
        try {
            // Read the image from URI
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI: $uri")
                return null
            }

            // Save the full image
            val imagePath = saveImage(context, bitmap, Constants.IMAGES_DIR)

            // Create and save thumbnail
            val thumbnail = createThumbnail(bitmap)
            saveThumbnail(context, thumbnail, imagePath)

            bitmap.recycle()
            thumbnail.recycle()

            return imagePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image from URI", e)
            return null
        }
    }

    /** Save a bitmap to internal storage */
    private fun saveImage(context: Context, bitmap: Bitmap, dirName: String): String {
        val dir = File(context.filesDir, dirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val fileName = "${UUID.randomUUID()}.jpg"
        val file = File(dir, fileName)

        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }

        return file.absolutePath
    }

    /** Save a thumbnail image */
    private fun saveThumbnail(context: Context, thumbnail: Bitmap, originalPath: String) {
        val dir = File(context.filesDir, Constants.THUMBNAILS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val originalFile = File(originalPath)
        val thumbnailFile = File(dir, originalFile.name)

        FileOutputStream(thumbnailFile).use { out ->
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
    }

    /** Create a thumbnail from a bitmap */
    private fun createThumbnail(bitmap: Bitmap): Bitmap {
        val size = Constants.THUMBNAIL_SIZE
        val width = bitmap.width
        val height = bitmap.height

        val scale = minOf(size.toFloat() / width, size.toFloat() / height)

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /** Get the thumbnail path for an image */
    fun getThumbnailPath(context: Context, imagePath: String): String {
        val originalFile = File(imagePath)
        val thumbnailDir = File(context.filesDir, Constants.THUMBNAILS_DIR)
        return File(thumbnailDir, originalFile.name).absolutePath
    }

    /** Delete an image and its thumbnail */
    fun deleteImage(context: Context, imagePath: String?) {
        if (imagePath == null) return

        try {
            // Delete main image
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                imageFile.delete()
            }

            // Delete thumbnail
            val thumbnailPath = getThumbnailPath(context, imagePath)
            val thumbnailFile = File(thumbnailPath)
            if (thumbnailFile.exists()) {
                thumbnailFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image", e)
        }
    }

    /** Clean up orphaned images (images not referenced in database) */
    fun cleanupOrphanedImages(context: Context, validImagePaths: Set<String>) {
        try {
            // Clean main images
            val imagesDir = File(context.filesDir, Constants.IMAGES_DIR)
            cleanupDirectory(imagesDir, validImagePaths)

            // Clean thumbnails
            val thumbnailsDir = File(context.filesDir, Constants.THUMBNAILS_DIR)
            cleanupDirectory(
                    thumbnailsDir,
                    validImagePaths.map { getThumbnailPath(context, it) }.toSet()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up orphaned images", e)
        }
    }

    /** Clean up a directory by deleting files not in the valid set */
    private fun cleanupDirectory(dir: File, validPaths: Set<String>) {
        if (!dir.exists()) return

        dir.listFiles()?.forEach { file ->
            if (file.absolutePath !in validPaths) {
                file.delete()
            }
        }
    }
}
