package com.samoondigital.yojnaplus.data.remote.dto

import com.samoondigital.yojnaplus.domain.model.Voter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Network DTO for a voter; mapped to the domain [Voter] before leaving the data layer. */
@Serializable
data class VoterDto(
    @SerialName("epic_no") val epicNumber: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("relative_name") val relativeName: String = "",
    @SerialName("age") val age: Int = 0,
    @SerialName("gender") val gender: String = "",
    @SerialName("assembly") val assembly: String = "",
    @SerialName("part_no") val partNumber: String = "",
    @SerialName("serial_no") val serialNumber: String = "",
)

fun VoterDto.toDomain(): Voter = Voter(
    epicNumber = epicNumber,
    name = name,
    relativeName = relativeName,
    age = age,
    gender = gender,
    assembly = assembly,
    partNumber = partNumber,
    serialNumber = serialNumber,
)

@Serializable
data class VoterSearchResponse(
    @SerialName("results") val results: List<VoterDto> = emptyList(),
)
