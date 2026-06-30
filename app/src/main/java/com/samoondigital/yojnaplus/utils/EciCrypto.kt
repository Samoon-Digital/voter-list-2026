package com.samoondigital.yojnaplus.utils

import android.util.Base64
import com.samoondigital.yojnaplus.model.CaptchaData
import com.samoondigital.yojnaplus.model.EncryptedBody
import com.samoondigital.yojnaplus.model.GeneratePdfRequest
import com.samoondigital.yojnaplus.model.PartListRequest
import com.samoondigital.yojnaplus.model.RollTypeQuery
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EciCrypto @Inject constructor(
    private val json: Json,
) {
    private val random = SecureRandom()

    fun encryptPartListRequest(payload: PartListRequest): EncryptedBody =
        encryptJson(json.encodeToString(payload))

    fun encryptGeneratePdfRequest(payload: GeneratePdfRequest): EncryptedBody =
        encryptJson(json.encodeToString(payload))

    fun encryptRollTypeQuery(stateCd: String, year: Int, misKey: String): RollTypeQuery {
        val encrypted = encryptValues(
            values = listOf(
                json.encodeToString(stateCd),
                json.encodeToString(year),
                json.encodeToString(misKey),
            ),
            urlSafe = true,
        )
        return RollTypeQuery(
            acceptYek = encrypted.encryptedKey,
            acceptRotcev = encrypted.iv,
            encryptedState = encrypted.encryptedValues[0],
            encryptedYear = encrypted.encryptedValues[1],
            encryptedMisKey = encrypted.encryptedValues[2],
        )
    }

    fun decryptCaptcha(encryptedData: String): CaptchaData {
        val raw = Base64.decode(encryptedData, Base64.DEFAULT)
        val iv = raw.copyOfRange(0, GCM_IV_SIZE)
        val cipherText = raw.copyOfRange(GCM_IV_SIZE, raw.size)
        val key = Base64.decode(CAPTCHA_SEED.substring(15, 59), Base64.DEFAULT)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
        val decrypted = cipher.doFinal(cipherText).decodeToString()
        return json.decodeFromString(CaptchaData.serializer(), decrypted)
    }

    private fun encryptJson(jsonPayload: String): EncryptedBody {
        val encrypted = encryptValues(values = listOf(jsonPayload), urlSafe = false)
        return EncryptedBody(
            encryptedPayload = encrypted.encryptedValues.first(),
            encryptedKey = encrypted.encryptedKey,
            iv = encrypted.iv,
        )
    }

    private fun encryptValues(values: List<String>, urlSafe: Boolean): EncryptedPayload {
        val aesKey = ByteArray(32).also(random::nextBytes)
        val iv = ByteArray(GCM_IV_SIZE).also(random::nextBytes)
        val encryptedValues = values.map { value ->
            encryptAes(value.toByteArray(Charsets.UTF_8), aesKey, iv).encode(urlSafe)
        }
        return EncryptedPayload(
            encryptedKey = encryptRsa(aesKey).encode(urlSafe),
            iv = iv.encode(urlSafe),
            encryptedValues = encryptedValues,
        )
    }

    private fun encryptAes(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
        return cipher.doFinal(data)
    }

    private fun encryptRsa(data: ByteArray): ByteArray {
        val keyBytes = Base64.decode(PUBLIC_KEY_BASE64, Base64.DEFAULT)
        val publicKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(keyBytes))
        val cipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
        val oaep = OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT,
        )
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaep)
        return cipher.doFinal(data)
    }

    private fun ByteArray.encode(urlSafe: Boolean): String {
        val flags = if (urlSafe) {
            Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
        } else {
            Base64.NO_WRAP
        }
        return Base64.encodeToString(this, flags)
    }

    private data class EncryptedPayload(
        val encryptedKey: String,
        val iv: String,
        val encryptedValues: List<String>,
    )

    companion object {
        const val MIS_KEY = "EROLLA32DVI09AJH"
        private const val GCM_IV_SIZE = 12
        private const val PUBLIC_KEY_BASE64 =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArb7++BxL/YN8OIln+6FL9Gnw5DNmQ/VFZXss+J+TuQyJc891JbqbijxYQNEin2c2u+CnpXpoGQ/1gUSzDMJeNS3sNSlIUykp2dt7xIm/cmV4sZ/c769vCxVRosMfRaZJnBAah+m1X26lEhnOo0wpAB9Txr8RIyBe6h7PiQWykeJeh6UacOBBX28kgkq7+vJhW8HgB38lt32XRocznRYwS9LqR7ZweFmQhTr1+EGrqiEKCOCxMYgHR2SQckb96hZ9kWzfzeun4bUO5oXKJciLkiS1IgKieADEvYLgu129ZIpn1H+8H+8ikNNVETqEDDMtqcQcQmWppJvcWHaXAs+f8QIDAQAB"
        private const val CAPTCHA_SEED =
            "SFfIO0YsOlOKawZe855n97lc4tcPkj7WWsi38yNWpalLBLZzQdkqHWYbZ0=GhSJk2raUo"
    }
}
