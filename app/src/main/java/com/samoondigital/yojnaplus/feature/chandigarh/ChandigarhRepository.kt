package com.samoondigital.yojnaplus.feature.chandigarh

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChandigarhRepository @Inject constructor(
    client: OkHttpClient,
) {
    private val webClient = client.newBuilder().apply {
        interceptors().clear()
        networkInterceptors().clear()
    }.build()

    suspend fun getAreas(): List<ChandigarhArea> = withContext(Dispatchers.IO) {
        val html = fetchPage()
        val selectHtml = Regex("<select[^>]*id=\"intensid\"[\\s\\S]*?</select>", RegexOption.IGNORE_CASE)
            .find(html)
            ?.value
            .orEmpty()
        OPTION_REGEX.findAll(selectHtml).map { match ->
            ChandigarhArea(
                id = decodeHtml(match.groupValues[1]).trim(),
                name = decodeHtml(stripTags(match.groupValues[2])).trim(),
            )
        }.filter { it.id.isNotBlank() && it.name.isNotBlank() && !it.id.startsWith("---") }.toList()
    }

    suspend fun getPollingStations(area: ChandigarhArea): List<ChandigarhPollingStation> =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(INTENSIVE_DATA_URL)
                .chandigarhHeaders()
                .post(FormBody.Builder().add("area", area.id).build())
                .build()
            val html = webClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("Unable to load Chandigarh polling stations")
                response.body?.string().orEmpty()
            }
            parseStations(html)
        }

    private fun fetchPage(): String {
        val request = Request.Builder()
            .url(INTENSIVE_URL)
            .chandigarhHeaders()
            .get()
            .build()
        return webClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("Unable to load Chandigarh areas")
            response.body?.string().orEmpty()
        }
    }

    private fun parseStations(html: String): List<ChandigarhPollingStation> =
        ROW_REGEX.findAll(html).mapNotNull { row ->
            val cells = CELL_REGEX.findAll(row.value).map { cell ->
                decodeHtml(stripTags(cell.groupValues[1])).trim()
            }.toList()
            val psNumber = cells.getOrNull(0)?.toIntOrNull()
            val area = cells.getOrNull(1).orEmpty()
            val name = cells.getOrNull(2).orEmpty()
            val pdfUrl = HREF_REGEX.find(row.value)?.groupValues?.get(1)?.let(::normalizeUrl)
            if (psNumber == null || name.isBlank() || pdfUrl.isNullOrBlank()) {
                null
            } else {
                ChandigarhPollingStation(
                    psNumber = psNumber,
                    area = area,
                    name = name,
                    pdfUrl = pdfUrl,
                )
            }
        }.toList()

    private fun Request.Builder.chandigarhHeaders(): Request.Builder =
        header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Origin", HOST_ROOT)
            .header("Referer", INTENSIVE_URL)
            .header("X-Requested-With", "XMLHttpRequest")
            .header("User-Agent", USER_AGENT)

    private fun normalizeUrl(url: String): String =
        decodeHtml(url).replace("$HOST_ROOT//", "$HOST_ROOT/")

    private fun stripTags(value: String): String =
        value.replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ")

    private fun decodeHtml(value: String): String =
        value.replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace("&nbsp;", " ")

    private companion object {
        const val HOST_ROOT = "https://ceochandigarh.gov.in"
        const val INTENSIVE_URL = "$HOST_ROOT/pages/intensive"
        const val INTENSIVE_DATA_URL = "$HOST_ROOT/pages/intensivedata"
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/126 Mobile Safari/537.36"
        val OPTION_REGEX = Regex("<option[^>]*value=\"([^\"]*)\"[^>]*>([\\s\\S]*?)</option>", RegexOption.IGNORE_CASE)
        val ROW_REGEX = Regex("<tr[^>]*>[\\s\\S]*?</tr>", RegexOption.IGNORE_CASE)
        val CELL_REGEX = Regex("<td[^>]*>([\\s\\S]*?)</td>", RegexOption.IGNORE_CASE)
        val HREF_REGEX = Regex("<a[^>]*href=\"([^\"]+)\"", RegexOption.IGNORE_CASE)
    }
}