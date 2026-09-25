package com.samoondigital.yojnaplus.feature.biharurban

data class BiharUrbanOption(
    val value: String,
    val label: String,
)

data class BiharUrbanPdfLink(
    val url: String,
    val label: String,
    val fileName: String,
)

enum class BiharUrbanStep {
    District,
    Subdivision,
    Municipality,
    PdfList,
}

data class BiharUrbanUiState(
    val step: BiharUrbanStep = BiharUrbanStep.District,
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val message: String? = null,
    val districts: List<BiharUrbanOption> = emptyList(),
    val subdivisions: List<BiharUrbanOption> = emptyList(),
    val municipalities: List<BiharUrbanOption> = emptyList(),
    val pdfLinks: List<BiharUrbanPdfLink> = emptyList(),
    val selectedDistrict: BiharUrbanOption? = null,
    val selectedSubdivision: BiharUrbanOption? = null,
    val selectedMunicipality: BiharUrbanOption? = null,
    val downloadingUrl: String? = null,
) {
    val stepNumber: Int
        get() = when (step) {
            BiharUrbanStep.District -> 1
            BiharUrbanStep.Subdivision -> 2
            BiharUrbanStep.Municipality -> 3
            BiharUrbanStep.PdfList -> 4
        }

    val totalSteps: Int = 4
}

sealed interface BiharUrbanEvent {
    data class OpenPdf(val uri: String, val title: String) : BiharUrbanEvent
}
