package com.samoondigital.yojnaplus.feature.westbengal

data class WestBengalDistrict(
    val id: String,
    val name: String,
)

data class WestBengalAssembly(
    val id: String,
    val number: Int?,
    val name: String,
) {
    val displayName: String
        get() = number?.let { "$it - $name" } ?: name
}

data class WestBengalPart(
    val psNumber: Int,
    val pollingStationName: String,
    val acId: String,
    val pdfFileName: String,
) {
    val displayName: String
        get() = "PS $psNumber - $pollingStationName"
}
