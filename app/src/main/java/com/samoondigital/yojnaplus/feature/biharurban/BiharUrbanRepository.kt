package com.samoondigital.yojnaplus.feature.biharurban

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import java.io.IOException
import java.net.URLDecoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiharUrbanRepository @Inject constructor(
    client: OkHttpClient,
) {
    private val cookieJar = MemoryCookieJar()
    private val httpClient = client.newBuilder()
        .cookieJar(cookieJar)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .callTimeout(120, TimeUnit.SECONDS)
        .apply {
            interceptors().clear()
            networkInterceptors().clear()
        }
        .build()

    private var hiddenFields: Map<String, String> = emptyMap()

    fun resetSession() {
        cookieJar.clear()
        hiddenFields = emptyMap()
    }

    suspend fun loadDistricts(): List<BiharUrbanOption> = withContext(Dispatchers.IO) {
        val html = getHtml()
        rememberHiddenFields(html)
        parseOptions(html, DistrictSelectId)
    }

    suspend fun loadSubdivisions(district: BiharUrbanOption): List<BiharUrbanOption> = withContext(Dispatchers.IO) {
        val html = postForm(
            eventTarget = DistrictField,
            district = district.value,
            subdivision = InvalidValue,
            municipality = InvalidValue,
            extraFields = emptyList(),
        )
        rememberHiddenFields(html)
        parseOptions(html, SubdivisionSelectId)
    }

    suspend fun loadMunicipalities(
        district: BiharUrbanOption,
        subdivision: BiharUrbanOption,
    ): List<BiharUrbanOption> = withContext(Dispatchers.IO) {
        val html = postForm(
            eventTarget = SubdivisionField,
            district = district.value,
            subdivision = subdivision.value,
            municipality = InvalidValue,
            extraFields = emptyList(),
        )
        rememberHiddenFields(html)
        parseOptions(html, MunicipalitySelectId)
    }

    suspend fun loadPdfLinks(
        district: BiharUrbanOption,
        subdivision: BiharUrbanOption,
        municipality: BiharUrbanOption,
    ): List<BiharUrbanPdfLink> = withContext(Dispatchers.IO) {
        val html = postForm(
            eventTarget = "",
            district = district.value,
            subdivision = subdivision.value,
            municipality = municipality.value,
            extraFields = listOf(ShowButtonField to "Show"),
        )
        rememberHiddenFields(html)
        parsePdfLinks(html)
    }

    private fun getHtml(): String {
        val request = Request.Builder()
            .url(PageUrl)
            .header("User-Agent", UserAgent)
            .build()
        return httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Bihar urban page failed: HTTP ${response.code}")
            response.bodyOrThrow().string()
        }
    }

    private fun postForm(
        eventTarget: String,
        district: String,
        subdivision: String,
        municipality: String,
        extraFields: List<Pair<String, String>>,
    ): String {
        val builder = FormBody.Builder()
        val fields = hiddenFields.toMutableMap()
        fields["__EVENTTARGET"] = eventTarget
        fields["__EVENTARGUMENT"] = fields["__EVENTARGUMENT"].orEmpty()
        fields["__LASTFOCUS"] = fields["__LASTFOCUS"].orEmpty()
        fields[DistrictField] = district
        fields[SubdivisionField] = subdivision
        fields[MunicipalityField] = municipality
        extraFields.forEach { (name, value) -> fields[name] = value }
        fields.forEach { (name, value) -> builder.add(name, value) }

        val request = Request.Builder()
            .url(PageUrl)
            .post(builder.build())
            .header("Referer", PageUrl)
            .header("Origin", BaseUrl)
            .header("User-Agent", UserAgent)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .build()
        return httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Bihar urban postback failed: HTTP ${response.code}")
            response.bodyOrThrow().string()
        }
    }

    private fun rememberHiddenFields(html: String) {
        hiddenFields = Jsoup.parse(html)
            .select("input[type=hidden]")
            .associate { input -> input.attr("name") to input.attr("value") }
    }

    private fun parseOptions(html: String, selectId: String): List<BiharUrbanOption> =
        Jsoup.parse(html)
            .select("select#$selectId option")
            .map { BiharUrbanOption(value = it.attr("value"), label = it.text().trim()) }
            .filter { it.value != InvalidValue && it.label.isNotBlank() }

    private fun parsePdfLinks(html: String): List<BiharUrbanPdfLink> {
        val document = Jsoup.parse(html, BaseUrl)
        return document.select("a[href*=.pdf], a[href*=.PDF]")
            .mapNotNull { link ->
                val url = link.absUrl("href").ifBlank { link.attr("href") }.trim()
                if (url.isBlank()) return@mapNotNull null
                val label = link.text().trim().ifBlank { url.substringAfterLast('/').substringBefore('?') }
                BiharUrbanPdfLink(
                    url = url,
                    label = label,
                    fileName = url.originalPdfFileName(label),
                )
            }
            .distinctBy { it.url }
    }

    private fun Response.bodyOrThrow(): ResponseBody =
        body ?: throw IOException("Empty response body")

    private fun String.originalPdfFileName(label: String): String {
        val raw = substringAfterLast('/').substringBefore('?').urlDecode().ifBlank { "$label.pdf" }
        val clean = raw
            .trim()
            .trim('"', '\'', ' ')
            .replace(Regex("[\\x00-\\x1F<>:\"/\\\\|?*]+"), "_")
            .replace(Regex("\\s+"), " ")
            .trim('.', ' ')
        val baseName = if (clean.endsWith(".pdf", ignoreCase = true)) {
            clean.dropLast(4).trim('.', ' ')
        } else {
            clean
        }.take(116).ifBlank {
            label.replace(Regex("[\\x00-\\x1F<>:\"/\\\\|?*]+"), "_").take(80).ifBlank { "bihar_urban_voter_list" }
        }
        return "$baseName.pdf"
    }

    private fun String.urlDecode(): String =
        runCatching { URLDecoder.decode(this, "UTF-8") }.getOrDefault(this)

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
        const val BaseUrl = "https://sec.bihar.gov.in"
        const val PageUrl = "$BaseUrl/ForPublic/RollPrint2026U1.aspx"
        const val UserAgent =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Mobile Safari/537.36"

        const val DistrictSelectId = "ctl00_ContentPlaceHolderMain_ddl_district"
        const val SubdivisionSelectId = "ctl00_ContentPlaceHolderMain_ddl_SubDiv"
        const val MunicipalitySelectId = "ctl00_ContentPlaceHolderMain_ddl_NagarNikay"

        const val DistrictField = "ctl00\$ContentPlaceHolderMain\$ddl_district"
        const val SubdivisionField = "ctl00\$ContentPlaceHolderMain\$ddl_SubDiv"
        const val MunicipalityField = "ctl00\$ContentPlaceHolderMain\$ddl_NagarNikay"
        const val ShowButtonField = "ctl00\$ContentPlaceHolderMain\$btnShow"
        const val InvalidValue = "0"
    }
}
