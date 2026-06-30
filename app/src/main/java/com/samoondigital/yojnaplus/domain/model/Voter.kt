package com.samoondigital.yojnaplus.domain.model

/** Core domain entity representing a voter record returned by a search. */
data class Voter(
    val epicNumber: String,
    val name: String,
    val relativeName: String,
    val age: Int,
    val gender: String,
    val assembly: String,
    val partNumber: String,
    val serialNumber: String,
)
