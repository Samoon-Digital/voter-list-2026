package com.samoondigital.yojnaplus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.samoondigital.yojnaplus.data.local.entity.VoterResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoterResultDao {

    @Query("SELECT * FROM voter_results WHERE searchQuery = :query AND searchType = :type ORDER BY timestamp DESC")
    fun observeBySearch(query: String, type: String): Flow<List<VoterResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<VoterResultEntity>)

    @Query("DELETE FROM voter_results WHERE searchQuery = :query AND searchType = :type")
    suspend fun deleteBySearch(query: String, type: String)
}
