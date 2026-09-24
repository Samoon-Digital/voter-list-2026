package com.samoondigital.yojnaplus.feature.upcurrent

data class UpSecOption(
    val value: String,
    val label: String,
)

data class UpUrbanDownloadOption(
    val id: String,
    val name: String,
    val value: String,
    val label: String,
    val hiddenFieldOverrides: Map<String, String> = emptyMap(),
)

data class UpPdfPayload(
    val bytes: ByteArray,
    val fileName: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UpPdfPayload
        return bytes.contentEquals(other.bytes) && fileName == other.fileName
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + fileName.hashCode()
        return result
    }
}

sealed interface UpSubmitResult {
    data class Pdf(val pdf: UpPdfPayload) : UpSubmitResult
    data class ServerMessage(val message: String) : UpSubmitResult
    data class UrbanDownloadOptions(val options: List<UpUrbanDownloadOption>) : UpSubmitResult
}

enum class UpCurrentFlow {
    Rural,
    Urban,
}

enum class UpCurrentStep {
    District,
    Block,
    GramPanchayat,
    UrbanBodyType,
    UrbanDistrict,
    UrbanUlb,
    UrbanWard,
    Captcha,
    UrbanPdfType,
}

data class UpCurrentUiState(
    val flow: UpCurrentFlow,
    val step: UpCurrentStep,
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val message: String? = null,
    val districts: List<UpSecOption> = emptyList(),
    val blocks: List<UpSecOption> = emptyList(),
    val gramPanchayats: List<UpSecOption> = emptyList(),
    val selectedDistrict: UpSecOption? = null,
    val selectedBlock: UpSecOption? = null,
    val selectedGramPanchayat: UpSecOption? = null,
    val urbanBodyTypes: List<UpSecOption> = emptyList(),
    val urbanDistricts: List<UpSecOption> = emptyList(),
    val urbanUlbs: List<UpSecOption> = emptyList(),
    val urbanWards: List<UpSecOption> = emptyList(),
    val urbanDownloadOptions: List<UpUrbanDownloadOption> = emptyList(),
    val selectedUrbanBodyType: UpSecOption? = null,
    val selectedUrbanDistrict: UpSecOption? = null,
    val selectedUrbanUlb: UpSecOption? = null,
    val selectedUrbanWard: UpSecOption? = null,
    val selectedUrbanDownloadOption: UpUrbanDownloadOption? = null,
    val captchaBytes: ByteArray? = null,
    val captchaInput: String = "",
) {
    val stepNumber: Int
        get() = when (flow) {
            UpCurrentFlow.Rural -> when (step) {
                UpCurrentStep.District -> 1
                UpCurrentStep.Block -> 2
                UpCurrentStep.GramPanchayat -> 3
                UpCurrentStep.Captcha -> 4
                else -> 1
            }
            UpCurrentFlow.Urban -> when (step) {
                UpCurrentStep.UrbanBodyType -> 1
                UpCurrentStep.UrbanDistrict -> 2
                UpCurrentStep.UrbanUlb -> 3
                UpCurrentStep.UrbanWard -> 4
                UpCurrentStep.Captcha -> 5
                UpCurrentStep.UrbanPdfType -> 6
                else -> 1
            }
        }

    val totalSteps: Int
        get() = if (flow == UpCurrentFlow.Rural) 4 else 6

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UpCurrentUiState
        return flow == other.flow &&
            step == other.step &&
            isLoading == other.isLoading &&
            isDownloading == other.isDownloading &&
            downloadProgress == other.downloadProgress &&
            message == other.message &&
            districts == other.districts &&
            blocks == other.blocks &&
            gramPanchayats == other.gramPanchayats &&
            selectedDistrict == other.selectedDistrict &&
            selectedBlock == other.selectedBlock &&
            selectedGramPanchayat == other.selectedGramPanchayat &&
            urbanBodyTypes == other.urbanBodyTypes &&
            urbanDistricts == other.urbanDistricts &&
            urbanUlbs == other.urbanUlbs &&
            urbanWards == other.urbanWards &&
            urbanDownloadOptions == other.urbanDownloadOptions &&
            selectedUrbanBodyType == other.selectedUrbanBodyType &&
            selectedUrbanDistrict == other.selectedUrbanDistrict &&
            selectedUrbanUlb == other.selectedUrbanUlb &&
            selectedUrbanWard == other.selectedUrbanWard &&
            selectedUrbanDownloadOption == other.selectedUrbanDownloadOption &&
            captchaBytes.contentEquals(other.captchaBytes) &&
            captchaInput == other.captchaInput
    }

    override fun hashCode(): Int {
        var result = flow.hashCode()
        result = 31 * result + step.hashCode()
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + isDownloading.hashCode()
        result = 31 * result + downloadProgress
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + districts.hashCode()
        result = 31 * result + blocks.hashCode()
        result = 31 * result + gramPanchayats.hashCode()
        result = 31 * result + (selectedDistrict?.hashCode() ?: 0)
        result = 31 * result + (selectedBlock?.hashCode() ?: 0)
        result = 31 * result + (selectedGramPanchayat?.hashCode() ?: 0)
        result = 31 * result + urbanBodyTypes.hashCode()
        result = 31 * result + urbanDistricts.hashCode()
        result = 31 * result + urbanUlbs.hashCode()
        result = 31 * result + urbanWards.hashCode()
        result = 31 * result + urbanDownloadOptions.hashCode()
        result = 31 * result + (selectedUrbanBodyType?.hashCode() ?: 0)
        result = 31 * result + (selectedUrbanDistrict?.hashCode() ?: 0)
        result = 31 * result + (selectedUrbanUlb?.hashCode() ?: 0)
        result = 31 * result + (selectedUrbanWard?.hashCode() ?: 0)
        result = 31 * result + (selectedUrbanDownloadOption?.hashCode() ?: 0)
        result = 31 * result + (captchaBytes?.contentHashCode() ?: 0)
        result = 31 * result + captchaInput.hashCode()
        return result
    }
}

sealed interface UpCurrentEvent {
    data class OpenPdf(val uri: String, val title: String) : UpCurrentEvent
}
