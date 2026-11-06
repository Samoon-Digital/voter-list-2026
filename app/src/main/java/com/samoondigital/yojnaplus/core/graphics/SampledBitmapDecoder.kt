package com.samoondigital.yojnaplus.core.graphics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/** Reads image bounds without allocating pixels, then decodes only the needed resolution. */
object SampledBitmapDecoder {
    @JvmStatic
    fun decodePdfCacheFile(path: String, context: Context): Bitmap? {
        // Match Pdf-Viewer 2.4.0's default 3x maximum zoom. Pages scroll vertically,
        // so sampling is based on page width rather than the viewport's height.
        val targetWidth = (context.resources.displayMetrics.widthPixels * 3).coerceAtLeast(1)
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)
        if (!prepare(options, targetWidth, 1)) return null
        return BitmapFactory.decodeFile(path, options)
    }

    fun decodeByteArray(bytes: ByteArray, widthPx: Int, heightPx: Int): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        if (!prepare(options, widthPx, heightPx)) return null
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    private fun prepare(options: BitmapFactory.Options, widthPx: Int, heightPx: Int): Boolean {
        if (options.outWidth <= 0 || options.outHeight <= 0) return false
        val width = widthPx.coerceAtLeast(1)
        val height = heightPx.coerceAtLeast(1)
        var sample = 1
        while (options.outWidth / 2 / sample >= width && options.outHeight / 2 / sample >= height) {
            sample *= 2
        }
        options.inSampleSize = sample
        options.inJustDecodeBounds = false
        options.inScaled = false
        return true
    }
}
