package com.samoondigital.yojnaplus.feature.up2003

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpRollRepository @Inject constructor(
    private val client: OkHttpClient,
) {
    private val upClient = client.newBuilder().apply {
        interceptors().clear()
        networkInterceptors().clear()
    }.build()

    suspend fun getDistricts(): List<UpDistrict> = withContext(Dispatchers.IO) {
        parseOptions(fetchPage(), DISTRICT_SELECT_ID)
            .filterNot { it.id.equals("Select District", ignoreCase = true) }
    }

    suspend fun getAssemblies(districtId: String): List<UpAssembly> = withContext(Dispatchers.IO) {
        val html = postDistrict(fetchPage(), districtId)
        parseOptions(html, ASSEMBLY_SELECT_ID)
            .filterNot { it.id.equals("Select AC", ignoreCase = true) }
            .mapNotNull { option ->
                option.id.toIntOrNull()?.let { acNumber ->
                    UpAssembly(acNumber = acNumber, name = option.name)
                }
            }
    }

    suspend fun getPollingStations(
        districtId: String,
        acNumber: Int,
    ): List<UpPollingStation> = withContext(Dispatchers.IO) {
        val assemblyPage = postDistrict(fetchPage(), districtId)
        val html = postShow(assemblyPage, districtId, acNumber)
        parsePollingStations(html)
    }

    private fun fetchPage(): String {
        val request = Request.Builder()
            .url(BASE_URL)
            .upHeaders()
            .get()
            .build()
        return upClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("Unable to load UP voter list")
            response.body?.string().orEmpty()
        }
    }

    private fun postDistrict(html: String, districtId: String): String {
        val form = baseForm(html)
            .add("__EVENTTARGET", DISTRICT_FIELD)
            .add(DISTRICT_FIELD, districtId)
            .add(ASSEMBLY_FIELD, "")
            .build()
        return postForm(form)
    }

    private fun postShow(html: String, districtId: String, acNumber: Int): String {
        val form = baseForm(html)
            .add("__EVENTTARGET", "")
            .add(DISTRICT_FIELD, districtId)
            .add(ASSEMBLY_FIELD, acNumber.toString())
            .add("ctl00\$ContentPlaceHolder1\$Button1", "Show")
            .build()
        return postForm(form)
    }

    private fun baseForm(html: String): FormBody.Builder =
        FormBody.Builder()
            .add("__EVENTARGUMENT", "")
            .add("__LASTFOCUS", "")
            .add("__VIEWSTATE", hiddenValue(html, "__VIEWSTATE"))
            .add("__VIEWSTATEGENERATOR", hiddenValue(html, "__VIEWSTATEGENERATOR"))
            .add("__EVENTVALIDATION", hiddenValue(html, "__EVENTVALIDATION"))

    private fun postForm(form: FormBody): String {
        val request = Request.Builder()
            .url(BASE_URL)
            .upHeaders()
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(form)
            .build()
        return upClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("Unable to load UP voter list")
            response.body?.string().orEmpty()
        }
    }

    private fun parseOptions(html: String, selectId: String): List<UpDistrict> {
        val select = SELECT_REGEX.find(html)
            ?.takeIf { it.value.contains("id=\"$selectId\"") }
            ?: Regex("<select[^>]*id=\"$selectId\"[\\s\\S]*?</select>", RegexOption.IGNORE_CASE).find(html)
        val selectHtml = select?.value.orEmpty()
        return OPTION_REGEX.findAll(selectHtml).map { match ->
            UpDistrict(
                id = decodeHtml(match.groupValues[1]).trim(),
                name = decodeHtml(stripTags(match.groupValues[2])).trim(),
            )
        }.filter { it.id.isNotBlank() && it.name.isNotBlank() }.toList()
    }

    private fun parsePollingStations(html: String): List<UpPollingStation> =
        ROW_REGEX.findAll(html).mapNotNull { rowMatch ->
            val cells = CELL_REGEX.findAll(rowMatch.value).map { cell ->
                decodeHtml(stripTags(cell.groupValues[1])).trim()
            }.toList()
            val relativeUrl = PDF_URL_REGEX.find(rowMatch.value)?.groupValues?.get(1)
            val acNumber = cells.getOrNull(0)?.toIntOrNull()
            val partNumber = cells.getOrNull(1)?.toIntOrNull()
            val name = cells.getOrNull(2)
            if (acNumber == null || partNumber == null || name.isNullOrBlank() || relativeUrl.isNullOrBlank()) {
                null
            } else {
                UpPollingStation(
                    acNumber = acNumber,
                    partNumber = partNumber,
                    name = name,
                    pdfUrl = "$ROLL_ROOT/${relativeUrl.trimStart('/')}",
                )
            }
        }.toList()

    private fun hiddenValue(html: String, id: String): String =
        Regex("id=\"$id\"\\s+value=\"([^\"]*)\"", RegexOption.IGNORE_CASE)
            .find(html)
            ?.groupValues
            ?.get(1)
            ?.let(::decodeHtml)
            ?: throw IllegalStateException("UP voter list form is not ready")

    private fun Request.Builder.upHeaders(): Request.Builder =
        header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Origin", HOST_ROOT)
            .header("Referer", BASE_URL)
            .header("User-Agent", USER_AGENT)

    private fun stripTags(value: String): String =
        value.replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ")

    private fun decodeHtml(value: String): String =
        value.replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace("&nbsp;", " ")

    private companion object {
        const val ROLL_ROOT = "https://ceouttarpradesh.nic.in/rollpdf"
        const val HOST_ROOT = "https://ceouttarpradesh.nic.in"
        const val BASE_URL = "$ROLL_ROOT/rollpdf.aspx"
        const val DISTRICT_SELECT_ID = "ctl00_ContentPlaceHolder1_DDLDistrict"
        const val ASSEMBLY_SELECT_ID = "ctl00_ContentPlaceHolder1_DDL_AC"
        const val DISTRICT_FIELD = "ctl00\$ContentPlaceHolder1\$DDLDistrict"
        const val ASSEMBLY_FIELD = "ctl00\$ContentPlaceHolder1\$DDL_AC"
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/126 Mobile Safari/537.36"
        val SELECT_REGEX = Regex("<select[\\s\\S]*?</select>", RegexOption.IGNORE_CASE)
        val OPTION_REGEX = Regex("<option[^>]*value=\"([^\"]*)\"[^>]*>([\\s\\S]*?)</option>", RegexOption.IGNORE_CASE)
        val ROW_REGEX = Regex("<tr[^>]*>[\\s\\S]*?</tr>", RegexOption.IGNORE_CASE)
        val CELL_REGEX = Regex("<td[^>]*>([\\s\\S]*?)</td>", RegexOption.IGNORE_CASE)
        val PDF_URL_REGEX = Regex("showCaptchaPopup\\(&quot;([^&]+)&quot;", RegexOption.IGNORE_CASE)
    }
}
