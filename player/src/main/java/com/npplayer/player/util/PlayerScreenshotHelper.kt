package com.npplayer.player.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.SurfaceView
import android.widget.Toast
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PlayerScreenshotHelper {

    suspend fun captureScreenshot(
        context: Context,
        playerView: PlayerView?,
        videoPath: String,
        videoTitle: String,
        positionMs: Long,
        includeFilters: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        if (includeFilters && playerView != null) {
            val surfaceView = findSurfaceView(playerView)
            if (surfaceView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val width = surfaceView.width
                val height = surfaceView.height
                if (width > 0 && height > 0) {
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    android.view.PixelCopy.request(
                        surfaceView,
                        bitmap,
                        { copyResult ->
                            if (copyResult == android.view.PixelCopy.SUCCESS) {
                                saveBitmapToGallery(context, bitmap, videoTitle)
                                onComplete(true)
                            } else {
                                // Fallback to raw frame extractor
                                captureRawFrame(context, videoPath, videoTitle, positionMs, onComplete)
                            }
                        },
                        Handler(Looper.getMainLooper())
                    )
                    return
                }
            }
        }

        // Clean original frame without filters
        captureRawFrame(context, videoPath, videoTitle, positionMs, onComplete)
    }

    private fun captureRawFrame(
        context: Context,
        videoPath: String,
        videoTitle: String,
        positionMs: Long,
        onComplete: (Boolean) -> Unit
    ) {
        Thread {
            try {
                val retriever = MediaMetadataRetriever()
                if (videoPath.startsWith("content://") || videoPath.startsWith("http://") || videoPath.startsWith("https://")) {
                    retriever.setDataSource(context, Uri.parse(videoPath))
                } else {
                    retriever.setDataSource(videoPath)
                }

                val timeUs = positionMs * 1000L
                val bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.getFrameAtTime(timeUs)

                retriever.release()

                if (bitmap != null) {
                    val success = saveBitmapToGallery(context, bitmap, videoTitle)
                    Handler(Looper.getMainLooper()).post { onComplete(success) }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Failed to capture frame", Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Screenshot error: ${e.message}", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            }
        }.start()
    }

    private fun findSurfaceView(view: android.view.View): SurfaceView? {
        if (view is SurfaceView) return view
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = findSurfaceView(view.getChildAt(i))
                if (child != null) return child
            }
        }
        return null
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, videoTitle: String): Boolean {
        return try {
            val cleanTitle = videoTitle.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val filename = "NPPlayer_${cleanTitle}_${System.currentTimeMillis()}.png"

            var outputStream: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "NP Player")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    outputStream = context.contentResolver.openOutputStream(uri)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "NP Player"
                val file = File(imagesDir)
                if (!file.exists()) file.mkdirs()
                val imageFile = File(imagesDir, filename)
                outputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            outputStream?.flush()
            outputStream?.close()

            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Screenshot saved to gallery", Toast.LENGTH_SHORT).show()
            }
            true
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Failed to save screenshot: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }
}
