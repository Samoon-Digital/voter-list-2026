package com.samoondigital.yojnaplus.feature.jharkhand

data class JharkhandDistrict(
    val id: String,
    val name: String,
)

data class JharkhandAssembly(
    val id: String,
    val name: String,
) {
    val displayName: String
        get() = name
}

data class JharkhandPart(
    val id: String,
    val name: String,
) {
    val partNumber: Int
        get() = id.toIntOrNull() ?: 0

    val displayName: String
        get() = "Part $name"
}

data class JharkhandCaptcha(
    val imageBase64: String,
)

data class JharkhandPdf(
    val bytes: ByteArray,
    val fileName: String,
)
