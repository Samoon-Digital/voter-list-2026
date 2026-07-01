package com.samoondigital.yojnaplus.feature.downloads.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_records")
data class DownloadRecordEntity(
    @PrimaryKey val id: String,
    val fileName: String,
    val uri: String?,
    val district: String,
    val assembly: String,
    val village: String,
    val fileSizeBytes: Long?,
    val downloadedAtMillis: Long,
    val status: String,
    val progress: Int,
    val errorMessage: String?,
)
