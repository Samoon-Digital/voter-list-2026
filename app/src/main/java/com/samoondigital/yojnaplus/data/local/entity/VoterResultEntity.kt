package com.samoondigital.yojnaplus.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "voter_results",
    indices = [Index(value = ["searchQuery", "searchType"])],
)
data class VoterResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epicNumber: String,
    val name: String,
    val relativeName: String,
    val age: Int,
    val gender: String,
    val assembly: String,
    val partNumber: String,
    val serialNumber: String,
    val stateName: String,
    val pollingStation: String,
    val searchQuery: String,
    val searchType: String,
    val timestamp: Long,
)
