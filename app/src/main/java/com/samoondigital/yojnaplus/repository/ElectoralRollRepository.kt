package com.samoondigital.yojnaplus.repository

import com.samoondigital.yojnaplus.api.EciGatewayApi
import com.samoondigital.yojnaplus.api.EciPublishedFileApi
import com.samoondigital.yojnaplus.model.AcLanguageRequest
import com.samoondigital.yojnaplus.model.AssemblyDto
import com.samoondigital.yojnaplus.model.CaptchaData
import com.samoondigital.yojnaplus.model.DistrictDto
import com.samoondigital.yojnaplus.model.GeneratePdfRequest
import com.samoondigital.yojnaplus.model.OldSirAssemblyDto
import com.samoondigital.yojnaplus.model.OldSirDistrictDto
import com.samoondigital.yojnaplus.model.OldSirPartDto
import com.samoondigital.yojnaplus.model.PartDto
import com.samoondigital.yojnaplus.model.PartListRequest
import com.samoondigital.yojnaplus.model.RollTypeDto
import com.samoondigital.yojnaplus.model.StateDto
import com.samoondigital.yojnaplus.utils.EciCrypto
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElectoralRollRepository @Inject constructor(
    private val gatewayApi: EciGatewayApi,
    private val publishedFileApi: EciPublishedFileApi,
    private val crypto: EciCrypto,
    private val json: Json,
) {
    suspend fun getStates(): List<StateDto> =
        decodeList(gatewayApi.getStates(), StateDto.serializer())
            .filter { it.isActive == null || it.isActive == "Y" }
            .sortedBy { it.stateName }

    suspend fun getDistricts(stateCd: String): List<DistrictDto> =
        decodeList(gatewayApi.getDistricts(stateCd), DistrictDto.serializer())
            .sortedBy { it.displayName }

    suspend fun getAssemblies(districtCd: String): List<AssemblyDto> =
        decodeList(gatewayApi.getAssemblies(districtCd), AssemblyDto.serializer())
            .sortedBy { it.asmblyNo }

    suspend fun getCaptcha(): CaptchaData {
        val encrypted = gatewayApi.getCaptcha("EROLL")
        return crypto.decryptCaptcha(encrypted.data)
    }

    suspend fun getRollTypes(stateCd: String, year: Int): List<RollTypeDto> {
        val encrypted = crypto.encryptRollTypeQuery(
            stateCd = stateCd,
            year = year,
            misKey = EciCrypto.MIS_KEY,
        )
        val response = gatewayApi.getPublishErollType(
            stateCd = encrypted.encryptedState,
            year = encrypted.encryptedYear,
            misKey = encrypted.encryptedMisKey,
            acceptYek = encrypted.acceptYek,
            acceptRotcev = encrypted.acceptRotcev,
        )
        return response.requirePayload()
    }

    suspend fun getLanguages(
        stateCd: String,
        acNumber: Int,
        rollType: RollTypeDto,
    ): Map<String, String> {
        val response = gatewayApi.getAcLanguages(
            AcLanguageRequest(
                stateCd = stateCd,
                acNumber = acNumber,
                rollTypeRefId = rollType.rollTypeRefId,
                pdfGenType = rollType.pdfGenType,
            ),
        )
        return response.requirePayload()
    }

    suspend fun getParts(
        stateCd: String,
        acNumber: Int,
        rollType: RollTypeDto,
        year: Int,
    ): List<PartDto> {
        val body = PartListRequest(
            stateCd = stateCd,
            acNumber = acNumber,
            rollTypeRefId = rollType.rollTypeRefId,
            pdfGenType = rollType.pdfGenType,
            revisionNo = rollType.revisionNo,
            year = year,
            misKey = EciCrypto.MIS_KEY,
        )
        val response = gatewayApi.getPublishPartList(crypto.encryptPartListRequest(body))
        return response.requirePayload().sortedBy { it.partNumber }
    }

    suspend fun generatePublishedPdfs(
        stateCd: String,
        districtCd: String,
        acNumber: Int,
        selectedParts: List<Int>,
        captcha: String,
        captchaId: String,
        languageCode: String,
        rollType: RollTypeDto,
    ): GeneratedPdfBatch {
        val body = GeneratePdfRequest(
            stateCd = stateCd,
            acNumber = acNumber,
            partNumberList = selectedParts,
            districtCd = districtCd,
            captcha = captcha,
            captchaId = captchaId,
            langCd = languageCode,
            publishedRollId = rollType.id,
            misKey = EciCrypto.MIS_KEY,
        )
        val response = gatewayApi.generatePublishedPdfs(crypto.encryptGeneratePdfRequest(body))
        if (response.statusCode != 200) {
            throw IllegalStateException(response.message ?: "PDF generation failed")
        }
        val items = response.payload.orEmpty().mapNotNull { it.asStringOrNull() }
        if (items.isEmpty()) {
            throw IllegalStateException(response.message ?: "No PDF file found")
        }
        return GeneratedPdfBatch(refId = response.refId, fileIds = items)
    }

    suspend fun getPublishedFile(fileId: String): PublishedFile {
        val response = publishedFileApi.getPublishedFile(fileId)
        val payload = response.payload ?: throw IllegalStateException(response.message ?: "File not available")
        return PublishedFile(
            base64Pdf = payload,
            fileName = response.refId?.ifBlank { null } ?: "electoral-roll-$fileId.pdf",
        )
    }

    suspend fun getOldSirDistricts(stateCd: String): List<OldSirDistrictDto> =
        gatewayApi.getOldSirDistricts(stateCd)
            .requirePayload()
            .sortedBy { it.districtNo }

    suspend fun getOldSirAssemblies(stateCd: String, districtNo: Int): List<OldSirAssemblyDto> =
        gatewayApi.getOldSirAssemblies(stateCd, districtNo)
            .requirePayload()
            .sortedBy { it.acNo }

    suspend fun getOldSirParts(stateCd: String, acNumber: Int): List<OldSirPartDto> =
        gatewayApi.getOldSirParts(stateCd, acNumber)
            .requirePayload()
            .sortedBy { it.partNumber }

    private fun <T> com.samoondigital.yojnaplus.model.EciEnvelope<T>.requirePayload(): T {
        if (statusCode != null && statusCode != 200) {
            throw IllegalStateException(message ?: "Server error ($statusCode)")
        }
        return payload ?: throw IllegalStateException(message ?: "Empty response")
    }

    private fun <T> decodeList(
        element: JsonElement,
        serializer: kotlinx.serialization.KSerializer<T>,
    ): List<T> {
        val payload = (element as? JsonObject)?.get("payload")
            ?: (element as? JsonObject)?.get("value")
            ?: element
        return json.decodeFromJsonElement(ListSerializer(serializer), payload)
    }

    private fun JsonElement.asStringOrNull(): String? =
        when (this) {
            is JsonPrimitive -> contentOrNull
            is JsonObject -> this["fileId"]?.asStringOrNull()
                ?: this["id"]?.asStringOrNull()
                ?: this["path"]?.asStringOrNull()
                ?: this["refId"]?.asStringOrNull()
            else -> null
        }
}

data class GeneratedPdfBatch(
    val refId: String?,
    val fileIds: List<String>,
) {
    val isCdn: Boolean = refId == "CDN"
}

data class PublishedFile(
    val base64Pdf: String,
    val fileName: String,
)
