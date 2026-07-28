package com.samoondigital.yojnaplus.feature.chandigarh

data class ChandigarhArea(
    val id: String,
    val name: String,
)

data class ChandigarhPollingStation(
    val psNumber: Int,
    val area: String,
    val name: String,
    val pdfUrl: String,
) {
    val displayName: String
        get() = "PS $psNumber - $name"
}