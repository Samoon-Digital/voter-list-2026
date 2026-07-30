package com.samoondigital.yojnaplus.feature.westbengal

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WestBengalRepository @Inject constructor(
    client: OkHttpClient,
) {
    private val webClient = client.newBuilder().apply {
        interceptors().clear()
        networkInterceptors().clear()
    }.build()

    suspend fun getDistricts(): List<WestBengalDistrict> = withContext(Dispatchers.IO) {
        val html = fetch("$HOST_ROOT/roll_dist", "$HOST_ROOT/")
        DISTRICT_LINK_REGEX.findAll(html).map { match ->
            WestBengalDistrict(
                id = match.groupValues[1],
                name = decodeHtml(stripTags(match.groupValues[2])).trim(),
            )
        }.filter { it.id.isNotBlank() && it.name.isNotBlank() }.distinctBy { it.id }.toList()
    }

    suspend fun getAssemblies(district: WestBengalDistrict): List<WestBengalAssembly> =
        withContext(Dispatchers.IO) {
            val html = fetch("$HOST_ROOT/Roll_ac/${district.id}", "$HOST_ROOT/roll_dist")
            ROW_REGEX.findAll(html).mapNotNull { row ->
                val cells = CELL_REGEX.findAll(row.value).map { cell ->
                    decodeHtml(stripTags(cell.groupValues[1])).trim()
                }.toList()
                val hrefId = ASSEMBLY_LINK_REGEX.find(row.value)?.groupValues?.get(1)
                val name = cells.getOrNull(1).orEmpty()
                if (hrefId.isNullOrBlank() || name.isBlank()) {
                    null
                } else {
                    WestBengalAssembly(
                        id = hrefId,
                        number = cells.getOrNull(0)?.toIntOrNull(),
                        name = name,
                    )
                }
            }.distinctBy { it.id }.toList()
        }

    suspend fun getParts(assembly: WestBengalAssembly): List<WestBengalPart> =
        withContext(Dispatchers.IO) {
            val html = fetch("$HOST_ROOT/Roll_ps/${assembly.id}", "$HOST_ROOT/Roll_ac/${assembly.id}")
            ROW_REGEX.findAll(html).mapNotNull { row ->
                val cells = CELL_REGEX.findAll(row.value).map { cell ->
                    decodeHtml(stripTags(cell.groupValues[1])).trim()
                }.toList()
                val psNumber = cells.getOrNull(0)?.toIntOrNull()
                val stationName = cells.getOrNull(1).orEmpty()
                val link = FINAL_ROLL_REGEX.find(row.value)
                val acId = link?.groupValues?.get(1).orEmpty()
                val fileName = link?.groupValues?.get(2).orEmpty()
                if (psNumber == null || stationName.isBlank() || acId.isBlank() || fileName.isBlank()) {
                    null
                } else {
                    WestBengalPart(
                        psNumber = psNumber,
                        pollingStationName = stationName,
                        acId = acId,
                        pdfFileName = fileName,
                    )
                }
            }.distinctBy { it.psNumber }.toList()
        }

    fun pdfUrl(part: WestBengalPart): String {
        val key = Base64.encodeToString(part.pdfFileName.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "$HOST_ROOT/RollPDF/GetDraft?acId=${part.acId}&key=$key"
    }

    private fun fetch(url: String, referer: String): String {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Referer", referer)
            .header("User-Agent", USER_AGENT)
            .get()
            .build()
        return webClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("Unable to load West Bengal voter list")
            response.body?.string().orEmpty()
        }
    }

    private fun stripTags(value: String): String =
        value.replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ")

    private fun decodeHtml(value: String): String =
        value.replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace("&nbsp;", " ")

    private companion object {
        const val HOST_ROOT = "https://ceowestbengal.wb.gov.in"
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/126 Mobile Safari/537.36"
        val DISTRICT_LINK_REGEX = Regex(
            """<a[^>]+href=["'][\\/]+Roll_ac[\\/]+(\d+)["'][^>]*>([\s\S]*?)</a>""",
            RegexOption.IGNORE_CASE,
        )
        val ASSEMBLY_LINK_REGEX = Regex(
            """<a[^>]+href=["'][\\/]+Roll_ps[\\/]+(\d+)["']""",
            RegexOption.IGNORE_CASE,
        )
        val FINAL_ROLL_REGEX = Regex(
            """openWithImageCaptcha\('([^']+)'\s*,\s*'([^']+)'\)""",
            RegexOption.IGNORE_CASE,
        )
        val ROW_REGEX = Regex("""<tr[^>]*>[\s\S]*?</tr>""", RegexOption.IGNORE_CASE)
        val CELL_REGEX = Regex("""<td[^>]*>([\s\S]*?)</td>""", RegexOption.IGNORE_CASE)
    }
}
