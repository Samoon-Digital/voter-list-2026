package com.samoondigital.yojnaplus.data.remote.dto

import com.samoondigital.yojnaplus.domain.model.Voter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Captcha ────────────────────────────────────────────────────────────────

// ECI getCaptcha returns {data:"sessionToken"} since mid-2026 API change.
// The token is the session ID; no image is embedded — handled in repo layer.
@Serializable
data class CaptchaResponse(
    @SerialName("data") val data: String = "",
)

// ─── OTP ─────────────────────────────────────────────────────────────────────

@Serializable
data class SendOtpRequest(
    @SerialName("mobNo") val mobNo: String,
    @SerialName("stateCd") val stateCd: String = "",
    @SerialName("captchaId") val captchaId: String,
    @SerialName("captchaData") val captchaData: String,
    @SerialName("securityKey") val securityKey: String = "na",
)

@Serializable
data class SendOtpResponse(
    @SerialName("statusCode") val statusCode: Int = 0,
    @SerialName("showNextFlow") val showNextFlow: Boolean = false,
    @SerialName("message") val message: String = "",
)

// ─── Mobile Search ───────────────────────────────────────────────────────────

@Serializable
data class MobileSearchRequest(
    @SerialName("otp") val otp: String,
    @SerialName("mobileNumber") val mobileNumber: String,
    @SerialName("stateCd") val stateCd: String? = null,
)

// ─── EPIC Search ─────────────────────────────────────────────────────────────

@Serializable
data class EpicSearchRequest(
    @SerialName("epicNumber") val epicNumber: String,
    @SerialName("isPortal") val isPortal: Boolean = true,
    @SerialName("captchaId") val captchaId: String,
    @SerialName("captchaData") val captchaData: String,
    @SerialName("securityKey") val securityKey: String = "na",
    @SerialName("stateCd") val stateCd: String? = null,
)

// ─── Name / DOB Search ───────────────────────────────────────────────────────

@Serializable
data class DetailsSearchRequest(
    @SerialName("stateCd") val stateCd: String,
    @SerialName("firstName") val firstName: String,
    @SerialName("lastName") val lastName: String? = null,
    @SerialName("relationName") val relationName: String,
    @SerialName("relationLastName") val relationLastName: String? = null,
    @SerialName("dob") val dob: String? = null,
    @SerialName("gender") val gender: String,
    @SerialName("captchaId") val captchaId: String,
    @SerialName("captchaData") val captchaData: String,
    @SerialName("securityKey") val securityKey: String = "na",
)

// ─── Voter Response ──────────────────────────────────────────────────────────

@Serializable
data class EciVoterDto(
    @SerialName("epicNumber") val epicNumber: String = "",
    @SerialName("applicantFirstName") val firstName: String = "",
    @SerialName("applicantLastName") val lastName: String = "",
    @SerialName("relationName") val relationName: String = "",
    @SerialName("relationLName") val relationLName: String = "",
    @SerialName("age") val age: Int = 0,
    @SerialName("gender") val gender: String = "",
    @SerialName("asmblyName") val assembly: String = "",
    @SerialName("acNumber") val assemblyNo: String = "",
    @SerialName("partNumber") val partNumber: String = "",
    @SerialName("partSerialNumber") val serialNumber: String = "",
    @SerialName("stateName") val stateName: String = "",
    @SerialName("psbuildingName") val pollingStation: String = "",
)

fun EciVoterDto.toDomain(): Voter = Voter(
    epicNumber = epicNumber,
    name = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" "),
    relativeName = listOf(relationName, relationLName).filter { it.isNotBlank() }.joinToString(" "),
    age = age,
    gender = when (gender.uppercase()) {
        "M" -> "Male"
        "F" -> "Female"
        "T", "O" -> "Third Gender"
        else -> gender
    },
    assembly = assembly,
    partNumber = partNumber,
    serialNumber = serialNumber,
    stateName = stateName,
    pollingStation = pollingStation,
)
