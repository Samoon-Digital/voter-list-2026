package com.samoondigital.yojnaplus

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SdkSuppress
import com.samoondigital.yojnaplus.feature.downloads.data.*
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DownloadFileMutationDeviceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var database: AppDatabase
    private lateinit var repository: DownloadRepository
    private val files = mutableListOf<File>()
    private val mediaUris = mutableListOf<Uri>()
    private val dao get() = database.downloadRecordDao()

    @Before fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = DownloadRepository(context, dao)
    }

    @After fun tearDown() {
        files.forEach { it.delete() }
        mediaUris.forEach { context.contentResolver.delete(it, null, null) }
        database.close()
    }

    @Test fun fileProviderRenameUpdatesThePhysicalFileAndReadableUri() = runBlocking {
        val source = testFile()
        val record = record(source.name, providerUri(source))
        dao.upsert(record)
        val target = File(source.parentFile, "p1-renamed-${UUID.randomUUID()}.pdf").also(files::add)
        repository.rename(record.id, target.name)
        val updated = requireNotNull(dao.getById(record.id))
        assertFalse(source.exists())
        assertTrue(target.isFile)
        assertEquals(target.name, updated.fileName)
        assertEquals(providerUri(target).toString(), updated.uri)
        context.contentResolver.openInputStream(Uri.parse(updated.uri)).use {
            assertEquals("%PDF-test", requireNotNull(it).bufferedReader().readText())
        }
    }

    @Test fun collisionDoesNotOverwriteAnotherPdfOrRenameTheDatabaseEntry() = runBlocking {
        val source = testFile()
        val target = testFile().apply { writeText("Existing PDF") }
        val record = record(source.name, providerUri(source))
        dao.upsert(record)
        assertTrue(runCatching { repository.rename(record.id, target.name) }.isFailure)
        assertEquals(record, dao.getById(record.id))
        assertEquals("%PDF-test", source.readText())
        assertEquals("Existing PDF", target.readText())
    }

    @Test fun failedPhysicalDeletePreservesTheDatabaseEntry() = runBlocking {
        val missing = testFile().also { assertTrue(it.delete()) }
        val record = record(missing.name, providerUri(missing))
        dao.upsert(record)
        assertTrue(runCatching { repository.delete(record.id) }.isFailure)
        assertEquals(record, dao.getById(record.id))
    }

    @Test fun successfulDeleteRemovesBothFileAndRecord() = runBlocking {
        val file = testFile()
        val record = record(file.name, providerUri(file))
        dao.upsert(record)
        repository.delete(record.id)
        assertFalse(file.exists())
        assertNull(dao.getById(record.id))
    }

    @Test fun missingFileCannotBeRenamedOnlyInTheDatabase() = runBlocking {
        val file = testFile().also { assertTrue(it.delete()) }
        val record = record(file.name, providerUri(file))
        dao.upsert(record)
        assertTrue(runCatching { repository.rename(record.id, "renamed.pdf") }.isFailure)
        assertEquals(record, dao.getById(record.id))
    }

    @Test @SdkSuppress(minSdkVersion = 29)
    fun mediaStoreRenameAndDeleteTrackTheRealStoredFile() = runBlocking {
        val name = "p1-media-${UUID.randomUUID()}.pdf"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/VoterList2026/")
        }
        val uri = requireNotNull(context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values))
        mediaUris.add(uri)
        context.contentResolver.openOutputStream(uri)!!.use { it.write("%PDF-test".toByteArray()) }
        val record = record(name, uri)
        dao.upsert(record)
        repository.rename(record.id, "p1-media-renamed-${UUID.randomUUID()}.pdf")
        val updated = requireNotNull(dao.getById(record.id))
        val storedName = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)!!.use {
            assertTrue(it.moveToFirst())
            it.getString(0)
        }
        assertEquals(storedName, updated.fileName)
        assertEquals(uri.toString(), updated.uri)
        repository.delete(record.id)
        assertNull(dao.getById(record.id))
        // A zero-row provider update/delete must fail without removing or renaming the record.
        dao.upsert(record)
        assertTrue(runCatching { repository.rename(record.id, "missing.pdf") }.isFailure)
        assertTrue(runCatching { repository.delete(record.id) }.isFailure)
        assertEquals(record, dao.getById(record.id))
    }

    private fun testFile() = File(context.cacheDir, "p1-file-${UUID.randomUUID()}.pdf")
        .apply { writeText("%PDF-test"); files.add(this) }

    private fun providerUri(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    private fun record(name: String, uri: Uri) = DownloadRecordEntity(
        id = UUID.randomUUID().toString(), fileName = name, uri = uri.toString(),
        district = "Test", assembly = "Test", village = "Test", fileSizeBytes = 9,
        downloadedAtMillis = 0, status = DownloadStatusEntity.Completed.name, progress = 100, errorMessage = null,
    )
}
