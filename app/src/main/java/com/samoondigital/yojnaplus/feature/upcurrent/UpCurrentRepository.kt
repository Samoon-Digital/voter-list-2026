package com.samoondigital.yojnaplus.feature.upcurrent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URLDecoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpCurrentRepository @Inject constructor(
    client: OkHttpClient,
) {
    private val ruralCookieJar = MemoryCookieJar()
    private val ruralClient = client.newBuilder()
        .cookieJar(ruralCookieJar)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .callTimeout(150, TimeUnit.SECONDS)
        .apply {
            interceptors().clear()
            networkInterceptors().clear()
        }
        .build()
    private val urbanCookieJar = MemoryCookieJar()
    private val urbanClient = client.newBuilder()
        .cookieJar(urbanCookieJar)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .callTimeout(150, TimeUnit.SECONDS)
        .apply {
            interceptors().clear()
            networkInterceptors().clear()
        }
        .build()

    private var ruralHiddenFields: Map<String, String> = emptyMap()
    private var urbanHiddenFields: Map<String, String> = emptyMap()

    fun resetRuralSession() {
        ruralCookieJar.clear()
        ruralHiddenFields = emptyMap()
    }

    fun resetUrbanSession() {
        urbanCookieJar.clear()
        urbanHiddenFields = emptyMap()
    }

    suspend fun loadRuralDistricts(): List<UpSecOption> = withContext(Dispatchers.IO) {
        val html = getHtml(PageUrl, ruralClient)
        rememberRuralHiddenFields(html)
        parseOptions(html, DistrictSelectId)
    }

    suspend fun loadRuralBlocks(district: UpSecOption): List<UpSecOption> = withContext(Dispatchers.IO) {
        val html = postRuralForm(
            eventTarget = DistrictField,
            district = district.value,
            block = "0",
            gramPanchayat = "0",
            captcha = "",
        )
        rememberRuralHiddenFields(html)
        parseOptions(html, BlockSelectId)
    }

    suspend fun loadRuralGramPanchayats(
        district: UpSecOption,
        block: UpSecOption,
    ): List<UpSecOption> = withContext(Dispatchers.IO) {
        val html = postRuralForm(
            eventTarget = BlockField,
            district = district.value,
            block = block.value,
            gramPanchayat = "0",
            captcha = "",
        )
        rememberRuralHiddenFields(html)
        parseOptions(html, GramPanchayatSelectId)
    }

    suspend fun loadRuralCaptcha(): ByteArray = loadCaptcha(PageUrl, ruralClient)

    suspend fun submitRural(
        district: UpSecOption,
        block: UpSecOption,
        gramPanchayat: UpSecOption,
        captcha: String,
        onProgress: (downloadedBytes: Long, totalBytes: Long?) -> Unit,
    ): UpSubmitResult = withContext(Dispatchers.IO) {
        val submitPage = postRuralMultipart(
            eventTarget = "",
            district = district.value,
            block = block.value,
            gramPanchayat = gramPanchayat.value,
            captcha = captcha,
            extraFields = listOf(SubmitButtonField to "Submit"),
            onProgress = null,
        ).toString(Charsets.UTF_8)

        extractAlertMessage(submitPage).takeIf { it.isNotBlank() }?.let {
            return@withContext UpSubmitResult.ServerMessage(it)
        }

        rememberRuralHiddenFields(submitPage)
        if (ruralHiddenFields[PdfNameField].isNullOrBlank()) {
            return@withContext UpSubmitResult.ServerMessage(
                parseServerMessage(submitPage).ifBlank { CaptchaError },
            )
        }

        val bytes = postRuralMultipart(
            eventTarget = DownloadButtonField,
            district = district.value,
            block = block.value,
            gramPanchayat = gramPanchayat.value,
            captcha = captcha,
            extraFields = emptyList(),
            onProgress = onProgress,
        )
        if (!bytes.startsWithPdf()) {
            return@withContext UpSubmitResult.ServerMessage(
                parseServerMessage(bytes.toString(Charsets.UTF_8)).ifBlank { DownloadError },
            )
        }
        UpSubmitResult.Pdf(UpPdfPayload(bytes, ruralHiddenFields[PdfNameField].originalPdfFileName("up_rural_voter_list.pdf")))
    }

    suspend fun loadUrbanBodyTypes(): List<UpSecOption> = withContext(Dispatchers.IO) {
        val html = getHtml(UrbanPageUrl, urbanClient)
        rememberUrbanHiddenFields(html)
        parseValidOptions(html, UrbanBodyTypeSelectId)
    }

    suspend fun loadUrbanDistricts(bodyType: UpSecOption): List<UpSecOption> = withContext(Dispatchers.IO) {
        val html = postUrbanForm(
            eventTarget = UrbanBodyTypeField,
            bodyType = bodyType.value,
            district = "-1",
            ulb = "-1",
            ward = "-1",
            captcha = "",
        )
        rememberUrbanHiddenFields(html)
        parseValidOptions(html, UrbanDistrictSelectId)
    }

    suspend fun loadUrbanUlbs(
        bodyType: UpSecOption,
        district: UpSecOption,
    ): List<UpSecOption> = withContext(Dispatchers.IO) {
        val html = postUrbanForm(
            eventTarget = UrbanDistrictField,
            bodyType = bodyType.value,
            district = district.value,
            ulb = "-1",
            ward = "-1",
            captcha = "",
        )
        rememberUrbanHiddenFields(html)
        parseValidOptions(html, UrbanUlbSelectId)
    }

    suspend fun loadUrbanWards(
        bodyType: UpSecOption,
        district: UpSecOption,
        ulb: UpSecOption,
    ): List<UpSecOption> = withContext(Dispatchers.IO) {
        val html = postUrbanForm(
            eventTarget = UrbanUlbField,
            bodyType = bodyType.value,
            district = district.value,
            ulb = ulb.value,
            ward = "-1",
            captcha = "",
        )
        rememberUrbanHiddenFields(html)
        parseValidOptions(html, UrbanWardSelectId)
    }

    suspend fun loadUrbanCaptcha(): ByteArray = loadCaptcha(UrbanPageUrl, urbanClient)

    suspend fun submitUrban(
        bodyType: UpSecOption,
        district: UpSecOption,
        ulb: UpSecOption,
        ward: UpSecOption,
        captcha: String,
    ): UpSubmitResult = withContext(Dispatchers.IO) {
        val submitBytes = postUrbanSubmitForm(
            eventTarget = "",
            bodyType = bodyType.value,
            district = district.value,
            ulb = ulb.value,
            ward = ward.value,
            captcha = captcha,
            extraFields = listOf(UrbanSubmitButtonField to "Submit"),
            onProgress = null,
        )
        if (submitBytes.startsWithPdf()) {
            return@withContext UpSubmitResult.Pdf(
                UpPdfPayload(submitBytes, urbanHiddenFields[PdfNameField].originalPdfFileName("up_urban_voter_list.pdf")),
            )
        }

        val page = submitBytes.toString(Charsets.UTF_8)
        rememberUrbanHiddenFields(page)
        val message = parseServerMessage(page)
        if (message.isNotBlank()) {
            return@withContext UpSubmitResult.ServerMessage(message)
        }

        val options = parseUrbanDownloadOptions(page)
        if (options.isEmpty() || urbanHiddenFields[PdfNameField].isNullOrBlank()) {
            return@withContext UpSubmitResult.ServerMessage(DefaultSubmitError)
        }
        UpSubmitResult.UrbanDownloadOptions(options)
    }

    suspend fun downloadUrbanPdf(
        option: UpUrbanDownloadOption,
        onProgress: (downloadedBytes: Long, totalBytes: Long?) -> Unit,
    ): UpSubmitResult = withContext(Dispatchers.IO) {
        var lastResponse: DownloadResponse? = null
        option.downloadAttempts().forEach { attempt ->
            val response = postUrbanDownloadForm(attempt, onProgress)
            if (response.bytes.startsWithPdf()) {
                return@withContext UpSubmitResult.Pdf(
                    UpPdfPayload(
                        bytes = response.bytes,
                        fileName = (response.fileName ?: urbanHiddenFields[PdfNameField])
                            .originalPdfFileName("up_urban_voter_list.pdf"),
                    ),
                )
            }
            lastResponse = response
        }

        val page = lastResponse?.bytes?.toString(Charsets.UTF_8).orEmpty()
        UpSubmitResult.ServerMessage(parseServerMessage(page).ifBlank { DefaultSubmitError })
    }

    private fun getHtml(url: String, httpClient: OkHttpClient): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", UserAgent)
            .build()
        return httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("UP voter list page failed: HTTP ${response.code}")
            response.bodyOrThrow().string()
        }
    }

    private fun postRuralForm(
        eventTarget: String,
        district: String,
        block: String,
        gramPanchayat: String,
        captcha: String,
    ): String {
        val request = Request.Builder()
            .url(PageUrl)
            .post(ruralMultipartBody(eventTarget, district, block, gramPanchayat, captcha))
            .header("Referer", PageUrl)
            .header("Origin", BaseUrl)
            .header("User-Agent", UserAgent)
            .build()
        return ruralClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("UP rural postback failed: HTTP ${response.code}")
            response.bodyOrThrow().string()
        }
    }

    private fun postRuralMultipart(
        eventTarget: String,
        district: String,
        block: String,
        gramPanchayat: String,
        captcha: String,
        extraFields: List<Pair<String, String>>,
        onProgress: ((downloadedBytes: Long, totalBytes: Long?) -> Unit)?,
    ): ByteArray {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        ruralFormFields(eventTarget, district, block, gramPanchayat, captcha)
            .forEach { (name, value) -> builder.addFormDataPart(name, value) }
        extraFields.forEach { (name, value) -> builder.addFormDataPart(name, value) }

        val request = Request.Builder()
            .url(PageUrl)
            .post(builder.build())
            .header("Referer", PageUrl)
            .header("Origin", BaseUrl)
            .header("User-Agent", UserAgent)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,application/pdf,*/*;q=0.8")
            .build()
        return ruralClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("UP rural submit failed: HTTP ${response.code}")
            response.bodyOrThrow().bytesWithProgress(onProgress)
        }
    }

    private fun ruralMultipartBody(
        eventTarget: String,
        district: String,
        block: String,
        gramPanchayat: String,
        captcha: String,
    ): MultipartBody {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        ruralFormFields(eventTarget, district, block, gramPanchayat, captcha)
            .forEach { (name, value) -> builder.addFormDataPart(name, value) }
        return builder.build()
    }

    private fun ruralFormFields(
        eventTarget: String,
        district: String,
        block: String,
        gramPanchayat: String,
        captcha: String,
    ): List<Pair<String, String>> {
        val fields = mutableListOf<Pair<String, String>>()
        ruralHiddenFields.forEach { (name, value) ->
            if (name !in CorePostbackFields) fields += name to value
        }
        fields += "__EVENTTARGET" to eventTarget
        fields += "__EVENTARGUMENT" to ruralHiddenFields["__EVENTARGUMENT"].orEmpty()
        fields += "__LASTFOCUS" to ruralHiddenFields["__LASTFOCUS"].orEmpty()
        fields += "__VIEWSTATE" to ruralHiddenFields["__VIEWSTATE"].orEmpty()
        fields += "__VIEWSTATEGENERATOR" to ruralHiddenFields["__VIEWSTATEGENERATOR"].orEmpty()
        fields += "__VIEWSTATEENCRYPTED" to ruralHiddenFields["__VIEWSTATEENCRYPTED"].orEmpty()
        fields += DistrictField to district
        fields += BlockField to block
        fields += GramPanchayatField to gramPanchayat
        fields += CaptchaField to captcha
        return fields
    }

    private fun postUrbanForm(
        eventTarget: String,
        bodyType: String,
        district: String,
        ulb: String,
        ward: String,
        captcha: String,
    ): String {
        val request = Request.Builder()
            .url(UrbanPageUrl)
            .post(urbanMultipartBody(eventTarget, bodyType, district, ulb, ward, captcha))
            .header("Referer", UrbanPageUrl)
            .header("Origin", BaseUrl)
            .header("User-Agent", UserAgent)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .build()
        return urbanClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("UP urban postback failed: HTTP ${response.code}")
            response.bodyOrThrow().string()
        }
    }

    private fun postUrbanSubmitForm(
        eventTarget: String,
        bodyType: String,
        district: String,
        ulb: String,
        ward: String,
        captcha: String,
        extraFields: List<Pair<String, String>>,
        onProgress: ((downloadedBytes: Long, totalBytes: Long?) -> Unit)?,
    ): ByteArray {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        urbanFormFields(eventTarget, bodyType, district, ulb, ward, captcha)
            .forEach { (name, value) -> builder.addFormDataPart(name, value) }
        extraFields.forEach { (name, value) -> builder.addFormDataPart(name, value) }

        val request = Request.Builder()
            .url(UrbanPageUrl)
            .post(builder.build())
            .header("Referer", UrbanPageUrl)
            .header("Origin", BaseUrl)
            .header("User-Agent", UserAgent)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,application/pdf,*/*;q=0.8")
            .build()
        return urbanClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("UP urban submit failed: HTTP ${response.code}")
            response.bodyOrThrow().bytesWithProgress(onProgress)
        }
    }

    private fun postUrbanDownloadForm(
        option: UpUrbanDownloadOption,
        onProgress: (downloadedBytes: Long, totalBytes: Long?) -> Unit,
    ): DownloadResponse {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        val fields = urbanHiddenFields.toMutableMap()
        fields.putAll(option.hiddenFieldOverrides)
        fields.forEach { (name, value) -> builder.addFormDataPart(name, value) }
        builder.addFormDataPart(option.name, option.value)

        val request = Request.Builder()
            .url(UrbanPageUrl)
            .post(builder.build())
            .header("Referer", UrbanPageUrl)
            .header("Origin", BaseUrl)
            .header("User-Agent", UserAgent)
            .header("Accept", "application/pdf,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .build()
        return urbanClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("UP urban PDF download failed: HTTP ${response.code}")
            DownloadResponse(
                bytes = response.bodyOrThrow().bytesWithProgress(onProgress),
                fileName = response.responseFileName(),
            )
        }
    }

    private fun urbanMultipartBody(
        eventTarget: String,
        bodyType: String,
        district: String,
        ulb: String,
        ward: String,
        captcha: String,
    ): MultipartBody {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        urbanFormFields(eventTarget, bodyType, district, ulb, ward, captcha)
            .forEach { (name, value) -> builder.addFormDataPart(name, value) }
        return builder.build()
    }

    private fun urbanFormFields(
        eventTarget: String,
        bodyType: String,
        district: String,
        ulb: String,
        ward: String,
        captcha: String,
    ): List<Pair<String, String>> {
        val fields = mutableListOf<Pair<String, String>>()
        urbanHiddenFields.forEach { (name, value) ->
            if (name !in CorePostbackFields) fields += name to value
        }
        fields += "__EVENTTARGET" to eventTarget
        fields += "__EVENTARGUMENT" to urbanHiddenFields["__EVENTARGUMENT"].orEmpty()
        fields += "__LASTFOCUS" to urbanHiddenFields["__LASTFOCUS"].orEmpty()
        fields += "__VIEWSTATE" to urbanHiddenFields["__VIEWSTATE"].orEmpty()
        fields += "__VIEWSTATEGENERATOR" to urbanHiddenFields["__VIEWSTATEGENERATOR"].orEmpty()
        fields += "__VIEWSTATEENCRYPTED" to urbanHiddenFields["__VIEWSTATEENCRYPTED"].orEmpty()
        fields += UrbanBodyTypeField to bodyType
        fields += UrbanDistrictField to district
        fields += UrbanUlbField to ulb
        fields += UrbanWardField to ward
        fields += UrbanCaptchaField to captcha
        return fields
    }

    private suspend fun loadCaptcha(
        referer: String,
        httpClient: OkHttpClient,
    ): ByteArray = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(CaptchaUrl)
            .header("Referer", referer)
            .header("Cache-Control", "no-cache, no-store, max-age=0")
            .header("Pragma", "no-cache")
            .header("User-Agent", UserAgent)
            .header("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Captcha load failed: HTTP ${response.code}")
            response.bodyOrThrow().bytes()
        }
    }

    private fun Response.bodyOrThrow(): ResponseBody =
        body ?: throw IOException("Empty response body")

    private fun rememberRuralHiddenFields(html: String) {
        ruralHiddenFields = Jsoup.parse(html)
            .select("input[type=hidden]")
            .associate { input -> input.attr("name") to input.attr("value") }
    }

    private fun rememberUrbanHiddenFields(html: String) {
        urbanHiddenFields = Jsoup.parse(html)
            .select("input[type=hidden]")
            .associate { input -> input.attr("name") to input.attr("value") }
    }

    private fun parseOptions(html: String, selectId: String): List<UpSecOption> =
        Jsoup.parse(html)
            .select("select#$selectId option")
            .map { UpSecOption(value = it.attr("value"), label = it.text().trim()) }
            .filter { it.value != "0" && it.label.isNotBlank() }

    private fun parseValidOptions(html: String, selectId: String): List<UpSecOption> =
        Jsoup.parse(html)
            .select("select#$selectId option")
            .map { UpSecOption(value = it.attr("value"), label = it.text().trim()) }
            .filter { it.value !in InvalidSelectValues && it.label.isNotBlank() }

    private fun parseServerMessage(html: String): String {
        val document = Jsoup.parse(html)
        return document.select("#ctl00_ContentPlaceHolder1_lblMessage").text()
            .ifBlank { document.select("#ctl00_ContentPlaceHolder1_lblmessage").text() }
            .ifBlank { document.select(".lblmsg").text() }
            .ifBlank { extractAlertMessage(html) }
    }

    private fun parseUrbanDownloadOptions(html: String): List<UpUrbanDownloadOption> {
        val document = Jsoup.parse(html)
        val hiddenFieldNamesById = document.select("input[type=hidden]")
            .associate { input -> input.attr("id") to input.attr("name") }
            .filterKeys { it.isNotBlank() }
        val buttons = document
            .select("input[type=submit], button[type=submit], input[type=button]")
            .mapIndexedNotNull { index, element ->
                val name = element.attr("name").trim()
                val value = element.attr("value").ifBlank { element.text() }.trim()
                if (name.isBlank() || value.isBlank()) return@mapIndexedNotNull null
                if (name == UrbanSubmitButtonField || value.equals("Submit", ignoreCase = true)) return@mapIndexedNotNull null
                if (!value.contains("pdf", ignoreCase = true) && !value.contains("download", ignoreCase = true)) {
                    return@mapIndexedNotNull null
                }
                UpUrbanDownloadOption(
                    id = "$name#$index#$value",
                    name = name,
                    value = value,
                    label = value.toUrbanDownloadLabel(),
                    hiddenFieldOverrides = element.attr("onclick").urbanHiddenFieldOverrides(hiddenFieldNamesById),
                )
            }
            .distinctBy { it.name to it.value }

        if (buttons.isNotEmpty()) return buttons

        val fallbackButton = document.selectFirst("#$UrbanDownloadButtonId") ?: return emptyList()
        val name = fallbackButton.attr("name").ifBlank { UrbanDownloadButtonField }
        val value = fallbackButton.attr("value").ifBlank { UrbanDownloadButtonValue }
        return listOf(
            UpUrbanDownloadOption(
                id = "$name#0#$value",
                name = name,
                value = value,
                label = value.toUrbanDownloadLabel(),
            ),
        )
    }

    private fun String.urbanHiddenFieldOverrides(hiddenFieldNamesById: Map<String, String>): Map<String, String> {
        if (isBlank()) return emptyMap()
        val overrides = linkedMapOf<String, String>()
        HiddenValueByIdRegex.findAll(this).forEach { match ->
            val id = match.groupValues[1]
            val name = hiddenFieldNamesById[id].orEmpty()
            if (name.isNotBlank()) overrides[name] = match.groupValues[2]
        }
        HiddenValueByNameRegex.findAll(this).forEach { match ->
            overrides[match.groupValues[1]] = match.groupValues[2]
        }
        return overrides
    }

    private fun UpUrbanDownloadOption.downloadAttempts(): List<UpUrbanDownloadOption> {
        val attempts = mutableListOf(this)
        val number = supplementaryNumber() ?: return attempts
        listOf(
            mapOf(UrbanSupplementPdfField to number),
            mapOf(UrbanHiddenField1 to number),
            mapOf(UrbanSupplementPdfField to number, UrbanHiddenField1 to number),
        ).forEach { overrides ->
            attempts += copy(hiddenFieldOverrides = hiddenFieldOverrides + overrides)
        }
        return attempts.distinctBy { option ->
            option.hiddenFieldOverrides.toSortedMap().entries.joinToString("|") { "${it.key}=${it.value}" }
        }
    }

    private fun UpUrbanDownloadOption.supplementaryNumber(): String? {
        val clean = "$label $value"
        if (!clean.contains("supp", ignoreCase = true) && !clean.contains("पूरक", ignoreCase = true)) return null
        return Regex("""\b(\d+)\b""").findAll(clean).lastOrNull()?.groupValues?.getOrNull(1)
    }

    private fun String.toUrbanDownloadLabel(): String {
        val clean = trim()
            .replace(Regex("(?i)^download\\s+"), "")
            .replace(Regex("(?i)\\s+pdf$"), " PDF")
            .replace(Regex("\\s+"), " ")
            .trim()
        return when {
            clean.contains("mother", ignoreCase = true) -> "Mother PDF"
            clean.contains("supp", ignoreCase = true) && clean.endsWith("PDF", ignoreCase = true) -> clean
            clean.contains("supp", ignoreCase = true) -> "$clean PDF"
            clean.endsWith("PDF", ignoreCase = true) -> clean
            else -> "$clean PDF"
        }
    }

    private fun extractAlertMessage(html: String): String =
        Regex("""(?i)alert\(\s*['"]([^'"]+)['"]\s*\)""")
            .find(html)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            .orEmpty()

    private fun ResponseBody.bytesWithProgress(
        onProgress: ((downloadedBytes: Long, totalBytes: Long?) -> Unit)?,
    ): ByteArray {
        if (onProgress == null) return bytes()
        val totalBytes = contentLength().takeIf { it > 0L }
        val output = ByteArrayOutputStream(totalBytes?.takeIf { it <= Int.MAX_VALUE }?.toInt() ?: DefaultDownloadBufferSize)
        val buffer = ByteArray(DefaultDownloadBufferSize)
        var downloadedBytes = 0L
        var lastPercent = -1
        var lastUnknownLengthUpdate = 0L
        onProgress(0L, totalBytes)
        byteStream().use { input ->
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                output.write(buffer, 0, read)
                downloadedBytes += read.toLong()
                if (totalBytes != null) {
                    val percent = ((downloadedBytes * 100L) / totalBytes).toInt().coerceIn(0, 100)
                    if (percent != lastPercent) {
                        lastPercent = percent
                        onProgress(downloadedBytes, totalBytes)
                    }
                } else if (downloadedBytes - lastUnknownLengthUpdate >= UnknownLengthProgressStepBytes) {
                    lastUnknownLengthUpdate = downloadedBytes
                    onProgress(downloadedBytes, null)
                }
            }
        }
        onProgress(downloadedBytes, totalBytes)
        return output.toByteArray()
    }

    private fun ByteArray.startsWithPdf(): Boolean =
        size >= 4 && this[0] == 0x25.toByte() && this[1] == 0x50.toByte() &&
            this[2] == 0x44.toByte() && this[3] == 0x46.toByte()

    private fun Response.responseFileName(): String? =
        header("Content-Disposition")?.contentDispositionFileName()

    private fun String.contentDispositionFileName(): String? {
        Regex("""(?i)filename\*=UTF-8''([^;]+)""")
            .find(this)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { return it.urlDecode().originalPdfFileNameOrNull() }
        Regex("""(?i)filename=\"?([^\";]+)\"?""")
            .find(this)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { return it.urlDecode().originalPdfFileNameOrNull() }
        return null
    }

    private fun String?.originalPdfFileName(fallback: String): String =
        originalPdfFileNameOrNull() ?: fallback.originalPdfFileNameOrNull() ?: "up_voter_list.pdf"

    private fun String?.originalPdfFileNameOrNull(): String? {
        val clean = this
            ?.trim()
            ?.trim('"', '\'', ' ')
            ?.substringAfterLast('\\')
            ?.substringAfterLast('/')
            ?.replace(Regex("[\\x00-\\x1F<>:\"/\\\\|?*]+"), "_")
            ?.replace(Regex("\\s+"), " ")
            ?.trim('.', ' ')
            .orEmpty()
        if (clean.isBlank()) return null
        val baseName = if (clean.endsWith(".pdf", ignoreCase = true)) {
            clean.dropLast(4).trim('.', ' ')
        } else {
            clean
        }.take(116).ifBlank { return null }
        return "$baseName.pdf"
    }

    private fun String.urlDecode(): String =
        runCatching { URLDecoder.decode(this, "UTF-8") }.getOrDefault(this)

    private data class DownloadResponse(val bytes: ByteArray, val fileName: String?)

    private class MemoryCookieJar : CookieJar {
        private val cookies = ConcurrentHashMap<String, MutableList<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            if (cookies.isEmpty()) return
            val existing = this.cookies[url.host].orEmpty()
            val merged = existing
                .filter { old ->
                    old.expiresAt > System.currentTimeMillis() &&
                        cookies.none { new -> new.name == old.name && new.domain == old.domain && new.path == old.path }
                }
                .toMutableList()
            merged.addAll(cookies.filter { it.expiresAt > System.currentTimeMillis() })
            this.cookies[url.host] = merged
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val now = System.currentTimeMillis()
            return cookies[url.host]?.filter { it.expiresAt > now }.orEmpty()
        }

        fun clear() {
            cookies.clear()
        }
    }

    private companion object {
        const val DefaultDownloadBufferSize = 8 * 1024
        const val UnknownLengthProgressStepBytes = 256 * 1024L
        const val BaseUrl = "https://sec.up.nic.in"
        const val PageUrl = "$BaseUrl/site/VoterList2026.aspx"
        const val UrbanPageUrl = "$BaseUrl/site/VoterListULB.aspx"
        const val CaptchaUrl = "$BaseUrl/site/Admin/CImage.aspx"
        const val UserAgent =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Mobile Safari/537.36"

        const val DistrictSelectId = "ctl00_ContentPlaceHolder1_ddlDistrict"
        const val BlockSelectId = "ctl00_ContentPlaceHolder1_ddlBlock"
        const val GramPanchayatSelectId = "ctl00_ContentPlaceHolder1_ddlGP"
        const val UrbanBodyTypeSelectId = "ctl00_ContentPlaceHolder1_ddlULBType"
        const val UrbanDistrictSelectId = "ctl00_ContentPlaceHolder1_ddlDistrictName"
        const val UrbanUlbSelectId = "ctl00_ContentPlaceHolder1_ddlULB"
        const val UrbanWardSelectId = "ctl00_ContentPlaceHolder1_ddlWard"

        const val DistrictField = "ctl00\$ContentPlaceHolder1\$ddlDistrict"
        const val BlockField = "ctl00\$ContentPlaceHolder1\$ddlBlock"
        const val GramPanchayatField = "ctl00\$ContentPlaceHolder1\$ddlGP"
        const val CaptchaField = "ctl00\$ContentPlaceHolder1\$captcha"
        const val UrbanBodyTypeField = "ctl00\$ContentPlaceHolder1\$ddlULBType"
        const val UrbanDistrictField = "ctl00\$ContentPlaceHolder1\$ddlDistrictName"
        const val UrbanUlbField = "ctl00\$ContentPlaceHolder1\$ddlULB"
        const val UrbanWardField = "ctl00\$ContentPlaceHolder1\$ddlWard"
        const val UrbanCaptchaField = "ctl00\$ContentPlaceHolder1\$captcha"
        const val UrbanSubmitButtonField = "ctl00\$ContentPlaceHolder1\$btnSubmit"
        const val UrbanDownloadButtonId = "ctl00_ContentPlaceHolder1_btnDownload"
        const val UrbanDownloadButtonField = "ctl00\$ContentPlaceHolder1\$btnDownload"
        const val UrbanDownloadButtonValue = "Download Mother pdf"
        const val UrbanSupplementPdfField = "ctl00\$ContentPlaceHolder1\$hdnPDFsupp"
        const val UrbanHiddenField1 = "ctl00\$ContentPlaceHolder1\$HiddenField1"
        const val SubmitButtonField = "ctl00\$ContentPlaceHolder1\$btnSubmit"
        const val DownloadButtonField = "ctl00\$ContentPlaceHolder1\$btnDownload"
        const val PdfNameField = "ctl00\$ContentPlaceHolder1\$hdnPDFName"
        const val CaptchaError = "Captcha match nahi hua. Image se same code dobara enter karein."
        const val DownloadError = "Final PDF response nahi mila. Dobara try karein."
        const val DefaultSubmitError =
            "Captcha match nahi hua ya final PDF response nahi mila. Image dekhkar code dobara enter karein."

        val HiddenValueByIdRegex = Regex("""getElementById\(['\"]([^'\"]+)['\"]\)\.value\s*=\s*['\"]([^'\"]*)['\"]""")
        val HiddenValueByNameRegex = Regex("""getElementsByName\(['\"]([^'\"]+)['\"]\)\[[^\]]*\]\.value\s*=\s*['\"]([^'\"]*)['\"]""")
        val InvalidSelectValues = setOf("", "0", "-1")
        val CorePostbackFields = setOf(
            "__EVENTTARGET",
            "__EVENTARGUMENT",
            "__LASTFOCUS",
            "__VIEWSTATE",
            "__VIEWSTATEGENERATOR",
            "__VIEWSTATEENCRYPTED",
        )
    }
}
