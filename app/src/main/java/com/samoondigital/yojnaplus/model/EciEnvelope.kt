package com.samoondigital.yojnaplus.model

import kotlinx.serialization.Serializable

@Serializable
data class EciEnvelope<T>(
    val status: String? = null,
    val statusCode: Int? = null,
    val refId: String? = null,
    val message: String? = null,
    val payload: T? = null,
    val file: String? = null,
)

@Serializable
data class EncryptedBody(
    val encryptedPayload: String,
    val encryptedKey: String,
    val iv: String,
)

@Serializable
data class CaptchaEncryptedDto(
    val data: String,
)

@Serializable
data class CaptchaData(
    val status: String? = null,
    val statusCode: Int? = null,
    val message: String? = null,
    val captcha: String,
    val id: String,
)

@Serializable
data class StateDto(
    val stateCd: String,
    val stateName: String,
    val stateType: String? = null,
    val isActive: String? = null,
)

@Serializable
data class DistrictDto(
    val districtCd: String,
    val districtValue: String? = null,
    val districtName: String? = null,
    val state: String? = null,
    val stateCd: String? = null,
) {
    val displayName: String
        get() = districtValue ?: districtName ?: districtCd
}

@Serializable
data class AssemblyDto(
    val asmblyNo: Int,
    val asmblyName: String,
    val districtCd: String? = null,
    val stateCd: String? = null,
)

@Serializable
data class RollTypeDto(
    val id: String,
    val stateCd: String? = null,
    val year: Int? = null,
    val revisionNo: Int? = null,
    val rollType: String? = null,
    val rollTypeRefId: String,
    val pdfGenType: String,
    val displayName: String,
    val publish: String? = null,
    val publishDate: String? = null,
    val byElecAcList: List<ByElectionAcDto>? = null,
)

@Serializable
data class ByElectionAcDto(
    val acNo: Int,
    val acName: String,
    val revisionNo: Int? = null,
)

@Serializable
data class PartDto(
    val partId: Long? = null,
    val stateCd: String? = null,
    val districtCd: String? = null,
    val acNumber: Int? = null,
    val partNumber: Int,
    val partName: String,
)

@Serializable
data class AcLanguageRequest(
    val stateCd: String,
    val acNumber: Int,
    val rollTypeRefId: String,
    val pdfGenType: String,
)

@Serializable
data class PartListRequest(
    val stateCd: String,
    val acNumber: Int,
    val rollTypeRefId: String,
    val pdfGenType: String,
    val revisionNo: Int? = null,
    val year: Int,
    val misKey: String,
)

@Serializable
data class GeneratePdfRequest(
    val stateCd: String,
    val acNumber: Int,
    val partNumberList: List<Int>,
    val districtCd: String,
    val captcha: String,
    val captchaId: String,
    val langCd: String,
    val publishedRollId: String,
    val misKey: String,
)

@Serializable
data class PublishedFileDto(
    val payload: String? = null,
    val refId: String? = null,
    val status: String? = null,
    val statusCode: Int? = null,
    val message: String? = null,
)

data class RollTypeQuery(
    val encryptedState: String,
    val encryptedYear: String,
    val encryptedMisKey: String,
    val acceptYek: String,
    val acceptRotcev: String,
)
