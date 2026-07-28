package com.samoondigital.yojnaplus.feature.jharkhand

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.URLDecoder
import javax.inject.Inject

class JharkhandRepository @Inject constructor(
    client: OkHttpClient,
) {
    private val cookieStore = LinkedHashMap<String, MutableList<Cookie>>()
    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            if (cookies.isEmpty()) return
            val hostCookies = cookieStore.getOrPut(url.host) { mutableListOf() }
            cookies.forEach { cookie ->
                hostCookies.removeAll { it.name == cookie.name && it.domain == cookie.domain && it.path == cookie.path }
                hostCookies.add(cookie)
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> = cookieStore.values
            .flatten()
            .filter { it.matches(url) }
    }
    private val webClient = client.newBuilder().apply {
        interceptors().clear()
        networkInterceptors().clear()
        cookieJar(cookieJar)
    }.build()

    private var currentHtml: String = ""

    suspend fun getDistricts(): List<JharkhandDistrict> = withContext(Dispatchers.IO) {
        resetSession()
        currentHtml = fetchPage()
        parseOptions(currentHtml, DISTRICT_SELECT_ID)
            .filterValid()
            .map { JharkhandDistrict(id = it.value, name = it.text) }
    }

    suspend fun getAssemblies(district: JharkhandDistrict): List<JharkhandAssembly> = withContext(Dispatchers.IO) {
        ensurePage()
        currentHtml = postForm(district = district.id, assembly = null, part = null, captcha = null, button = null)
        parseOptions(currentHtml, ASSEMBLY_SELECT_ID)
            .filterValid()
            .map { JharkhandAssembly(id = it.value, name = it.text) }
    }

    suspend fun getParts(
        district: JharkhandDistrict,
        assembly: JharkhandAssembly,
    ): List<JharkhandPart> = withContext(Dispatchers.IO) {
        ensurePage()
        currentHtml = postForm(district = district.id, assembly = assembly.id, part = null, captcha = null, button = null)
        parseOptions(currentHtml, PART_SELECT_ID)
            .filterValid()
            .map { JharkhandPart(id = it.value, name = it.text) }
    }

    suspend fun prepareCaptcha(
        district: JharkhandDistrict,
        assembly: JharkhandAssembly,
        part: JharkhandPart,
    ): JharkhandCaptcha = withContext(Dispatchers.IO) {
        ensurePage()
        currentHtml = postForm(
            district = district.id,
            assembly = assembly.id,
            part = part.id,
            captcha = null,
            button = null,
        )
        loadCaptcha(currentHtml)
    }

    suspend fun refreshCaptcha(
        district: JharkhandDistrict,
        assembly: JharkhandAssembly,
        part: JharkhandPart,
    ): JharkhandCaptcha = withContext(Dispatchers.IO) {
        ensurePage()
        currentHtml = postForm(
            district = district.id,
            assembly = assembly.id,
            part = part.id,
            captcha = null,
            button = REFRESH_BUTTON_FIELD,
        )
        loadCaptcha(currentHtml)
    }

    suspend fun downloadPdf(
        district: JharkhandDistrict,
        assembly: JharkhandAssembly,
        part: JharkhandPart,
        captcha: String,
    ): JharkhandPdf = withContext(Dispatchers.IO) {
        ensurePage()
        val response = postFormResponse(
            district = district.id,
            assembly = assembly.id,
            part = part.id,
            captcha = captcha,
            button = OK_BUTTON_FIELD,
        )
        response.use { result ->
            if (!result.isSuccessful) throw IllegalStateException("Unable to submit captcha")
            val bytes = result.body?.bytes() ?: ByteArray(0)
            val contentType = result.header("Content-Type").orEmpty()
            val isPdf = contentType.contains("pdf", ignoreCase = true) || bytes.startsWithPdf()
            if (isPdf) {
                return@withContext JharkhandPdf(
                    bytes = bytes,
                    fileName = result.fileName() ?: "jharkhand-2003-ac-${assembly.id}-part-${part.id}.pdf",
                )
            }
            val html = bytes.toString(Charsets.UTF_8)
            currentHtml = html
            throw IllegalStateException(parseError(html) ?: "Captcha verification failed")
        }
    }

    private fun resetSession() {
        cookieStore.clear()
        currentHtml = ""
    }

    private fun ensurePage() {
        if (currentHtml.isBlank()) currentHtml = fetchPage()
    }

    private fun fetchPage(): String {
        val request = Request.Builder()
            .url(BASE_URL)
            .jharkhandHeaders()
            .get()
            .build()
        return webClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("Unable to load Jharkhand voter list")
            response.body?.string().orEmpty()
        }
    }

    private fun postForm(
        district: String,
        assembly: String?,
        part: String?,
        captcha: String?,
        button: String?,
    ): String = postFormResponse(district, assembly, part, captcha, button).use { response ->
        if (!response.isSuccessful) throw IllegalStateException("Unable to load Jharkhand voter list")
        response.body?.string().orEmpty()
    }

    private fun postFormResponse(
        district: String,
        assembly: String?,
        part: String?,
        captcha: String?,
        button: String?,
    ): Response {
        val form = FormBody.Builder()
            .add("__VIEWSTATE", hiddenValue(currentHtml, "__VIEWSTATE"))
            .add("__VIEWSTATEGENERATOR", hiddenValue(currentHtml, "__VIEWSTATEGENERATOR"))
            .add("__EVENTVALIDATION", hiddenValue(currentHtml, "__EVENTVALIDATION"))
            .add(ROLL_TYPE_FIELD, ROLL_TYPE_VALUE)
            .add(DISTRICT_FIELD, district)
            .add(CAPTCHA_FIELD, captcha.orEmpty())
            .apply {
                if (!assembly.isNullOrBlank()) add(ASSEMBLY_FIELD, assembly)
                if (!part.isNullOrBlank()) add(PART_FIELD, part)
                if (!button.isNullOrBlank()) add(button, if (button == OK_BUTTON_FIELD) "Ok" else "Refresh")
            }
            .build()
        val request = Request.Builder()
            .url(BASE_URL)
            .jharkhandHeaders()
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(form)
            .build()
        return webClient.newCall(request).execute()
    }

    private fun loadCaptcha(html: String): JharkhandCaptcha {
        val src = Regex("<img[^>]*id=\"imgCaptcha\"[^>]*src=\"([^\"]+)\"", RegexOption.IGNORE_CASE)
            .find(html)
            ?.groupValues
            ?.get(1)
            ?.let(::decodeHtml)
            ?: throw IllegalStateException("Captcha is not ready")
        val captchaUrl = if (src.startsWith("http", ignoreCase = true)) src else "$ROLL_ROOT/${src.trimStart('/')}"
        val request = Request.Builder()
            .url(captchaUrl)
            .jharkhandHeaders()
            .header("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
            .build()
        val bytes = webClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("Unable to load captcha")
            response.body?.bytes() ?: ByteArray(0)
        }
        return JharkhandCaptcha(Base64.encodeToString(bytes, Base64.NO_WRAP))
    }

    private fun parseOptions(html: String, selectId: String): List<Option> {
        val selectHtml = Regex("<select[^>]*id=\"$selectId\"[\\s\\S]*?</select>", RegexOption.IGNORE_CASE)
            .find(html)
            ?.value
            .orEmpty()
        return OPTION_REGEX.findAll(selectHtml).map { match ->
            Option(
                value = decodeHtml(match.groupValues[1]).trim(),
                text = decodeHtml(stripTags(match.groupValues[2])).trim(),
            )
        }.toList()
    }

    private fun List<Option>.filterValid(): List<Option> = filter {
        it.value.isNotBlank() && it.value != "0" && !it.text.contains("Select", ignoreCase = true)
    }

    private fun hiddenValue(html: String, id: String): String = Regex(
        "name=\"$id\" id=\"$id\" value=\"([^\"]*)\"",
        RegexOption.IGNORE_CASE,
    ).find(html)?.groupValues?.get(1)?.let(::decodeHtml)
        ?: throw IllegalStateException("Jharkhand form is not ready")

    private fun Request.Builder.jharkhandHeaders(): Request.Builder =
        header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Origin", HOST_ROOT)
            .header("Referer", BASE_URL)
            .header("User-Agent", USER_AGENT)

    private fun parseError(html: String): String? {
        val message = Regex("<span[^>]*id=\"lblMsgForm\"[^>]*>([\\s\\S]*?)</span>", RegexOption.IGNORE_CASE)
            .find(html)
            ?.groupValues
            ?.get(1)
            ?.let(::stripTags)
            ?.let(::decodeHtml)
            ?.trim()
        return message?.takeIf { it.isNotBlank() } ?: "Incorrect captcha. Try again."
    }

    private fun Response.fileName(): String? {
        val disposition = header("Content-Disposition").orEmpty()
        val raw = Regex("filename\\*?=([^;]+)", RegexOption.IGNORE_CASE)
            .find(disposition)
            ?.groupValues
            ?.get(1)
            ?.trim(' ', '"')
            ?: return null
        return runCatching { URLDecoder.decode(raw.substringAfter("''"), "UTF-8") }.getOrDefault(raw)
    }

    private fun ByteArray.startsWithPdf(): Boolean = size >= 4 &&
        this[0] == 0x25.toByte() && this[1] == 0x50.toByte() && this[2] == 0x44.toByte() && this[3] == 0x46.toByte()

    private fun stripTags(value: String): String =
        value.replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ")

    private fun decodeHtml(value: String): String = value
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&amp;", "&")
        .replace("&nbsp;", " ")

    private data class Option(val value: String, val text: String)

    private companion object {
        const val HOST_ROOT = "https://ceojh.jharkhand.gov.in"
        const val ROLL_ROOT = "$HOST_ROOT/mrollpdf1"
        const val BASE_URL = "$ROLL_ROOT/aceng.aspx"
        const val ROLL_TYPE_FIELD = "ddlRollType"
        const val ROLL_TYPE_VALUE = "Electoral Roll 2003"
        const val DISTRICT_FIELD = "ddlDistrict"
        const val ASSEMBLY_FIELD = "ddlAC"
        const val PART_FIELD = "ddlPart"
        const val CAPTCHA_FIELD = "txtCaptcha"
        const val REFRESH_BUTTON_FIELD = "btnRefresh"
        const val OK_BUTTON_FIELD = "Button3"
        const val DISTRICT_SELECT_ID = "ddlDistrict"
        const val ASSEMBLY_SELECT_ID = "ddlAC"
        const val PART_SELECT_ID = "ddlPart"
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/126 Mobile Safari/537.36"
        val OPTION_REGEX = Regex("<option[^>]*value=\"([^\"]*)\"[^>]*>([\\s\\S]*?)</option>", RegexOption.IGNORE_CASE)
    }
}
