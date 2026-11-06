package com.samoondigital.yojnaplus

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.rajat.pdfviewer.util.CacheManager
import com.rajat.pdfviewer.util.CacheStrategy
import com.samoondigital.yojnaplus.core.graphics.SampledBitmapDecoder
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class BitmapSamplingDeviceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun oversizedCaptchaKeepsDisplayResolutionWithFewerAllocatedPixels() {
        val source = image(1600, 600)
        val originalBytes = source.allocationByteCount
        val encoded = encode(source)
        source.recycle()
        val decoded = requireNotNull(SampledBitmapDecoder.decodeByteArray(encoded, 300, 113))
        try {
            assertTrue(decoded.width in 300 until 600)
            assertTrue(decoded.height in 113 until 226)
            assertTrue(decoded.allocationByteCount <= originalBytes / 8)
            assertEquals(Color.BLUE, decoded.getPixel(decoded.width / 2, decoded.height / 2))
        } finally { decoded.recycle() }
    }

    @Test fun normalCaptchaIsNotUpscaledOrDegraded() {
        val source = image(160, 60)
        val encoded = encode(source)
        source.recycle()
        val decoded = requireNotNull(SampledBitmapDecoder.decodeByteArray(encoded, 300, 113))
        try {
            assertEquals(160, decoded.width)
            assertEquals(60, decoded.height)
            assertEquals(Color.BLUE, decoded.getPixel(80, 30))
        } finally { decoded.recycle() }
    }

    @Test fun invalidImageAndMissingCacheFileReturnNull() {
        assertNull(SampledBitmapDecoder.decodeByteArray(byteArrayOf(1, 2, 3), 300, 113))
        assertNull(SampledBitmapDecoder.decodePdfCacheFile(File(context.cacheDir, "missing-${UUID.randomUUID()}").path, context))
    }

    @Test fun realLibraryDiskCacheSamplesOversizedPagesBeforeAllocatingPixels() = runBlocking {
        checkLibraryCache(sourceWidth = targetPageWidth() * 4, expectSampling = true)
    }

    @Test fun realLibraryDiskCachePreservesResolutionThroughTheViewersMaximumZoom() = runBlocking {
        checkLibraryCache(sourceWidth = targetPageWidth(), expectSampling = false)
    }

    private suspend fun checkLibraryCache(sourceWidth: Int, expectSampling: Boolean) {
        val cacheKey = "bitmap-test-${UUID.randomUUID()}"
        val directory = File(context.cacheDir, "${CacheManager.CACHE_PATH}/$cacheKey")
        val source = image(sourceWidth, 256)
        val originalBytes = source.allocationByteCount
        try {
            CacheManager(context, cacheKey, CacheStrategy.MAXIMIZE_PERFORMANCE).addBitmapToCache(0, source)
            source.recycle()
            // A fresh manager guarantees this exercises the library's disk decoder, not its memory cache.
            val decoded = requireNotNull(
                CacheManager(context, cacheKey, CacheStrategy.MAXIMIZE_PERFORMANCE).getBitmapFromCache(0),
            )
            try {
                assertEquals(Color.BLUE, decoded.getPixel(decoded.width / 2, decoded.height / 2))
                if (expectSampling) {
                    assertTrue(decoded.width >= targetPageWidth())
                    assertTrue(decoded.width < targetPageWidth() * 2)
                    assertTrue(decoded.allocationByteCount <= originalBytes / 8)
                } else {
                    assertEquals(sourceWidth, decoded.width)
                    assertEquals(256, decoded.height)
                }
            } finally { decoded.recycle() }
        } finally {
            if (!source.isRecycled) source.recycle()
            directory.listFiles().orEmpty().forEach { file -> check(file.isFile && file.delete()) }
            if (directory.exists()) check(directory.delete())
        }
    }

    private fun targetPageWidth(): Int = context.resources.displayMetrics.widthPixels * 3

    private fun image(width: Int, height: Int): Bitmap =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.BLUE) }

    private fun encode(bitmap: Bitmap): ByteArray = ByteArrayOutputStream().use { output ->
        check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        output.toByteArray()
    }
}
