package com.samoondigital.yojnaplus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room entity caching a user's recent search queries. */
@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val query: String,
    val timestamp: Long,
    val searchType: String = "MOBILE",
)
