package com.samoondigital.yojnaplus.feature.up2003

data class UpDistrict(
    val id: String,
    val name: String,
)

data class UpAssembly(
    val acNumber: Int,
    val name: String,
) {
    val displayName: String
        get() = "$acNumber - $name"
}

data class UpPollingStation(
    val acNumber: Int,
    val partNumber: Int,
    val name: String,
    val pdfUrl: String,
) {
    val displayName: String
        get() = "Part $partNumber - $name"
}
