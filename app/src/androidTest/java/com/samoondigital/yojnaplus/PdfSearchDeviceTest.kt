package com.samoondigital.yojnaplus

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.net.toUri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.samoondigital.yojnaplus.feature.downloads.data.AppDatabase
import com.samoondigital.yojnaplus.feature.downloads.data.DownloadStatusEntity
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfSearchDeviceTest {
    @Test
    fun topDownloadedPdfFindsHindiMatOnFirstPage() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val database = Room.databaseBuilder(context, AppDatabase::class.java, "yojna-plus.db").build()
        val record = database.downloadRecordDao()
            .getAllOnce()
            .filter { it.status == DownloadStatusEntity.Completed.name && it.uri != null }
            .maxByOrNull { it.downloadedAtMillis }
            ?: error("No completed downloaded PDF found")

        val cacheFile = File(context.cacheDir, "pdf-search-device-test.pdf")
        context.contentResolver.openInputStream(record.uri!!.toUri()).use { input ->
            requireNotNull(input) { "Unable to open ${record.uri}" }
            cacheFile.outputStream().use { output -> input.copyTo(output) }
        }

        ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                assertTrue("PDF should have pages", renderer.pageCount > 0)
                val matches = renderer.openPage(0).use { page -> page.searchText("मत") }
                assertTrue("Expected मत on first page of ${record.fileName}", matches.isNotEmpty())
            }
        }
    }
}
