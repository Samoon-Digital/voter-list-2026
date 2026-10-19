package com.samoondigital.yojnaplus.core.analytics

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.*
import org.junit.Test
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AppscopeHttpSenderTest {
    private val visit = AppscopeVisit("test-app", "test-installation", "test-event", "2026-10-19T10:00:00Z")

    @Test fun successfulAndDuplicateResponsesAreAcceptedWithCorrectPayloadAndKeyHeader() {
        for (status in listOf(200, 201)) {
            withServer(status) { sender, captured ->
                assertEquals(AppscopeDelivery.Accepted, sender.send(visit))
                val (headers, body) = captured()
                assertTrue(headers.any { it.equals("X-Tracking-Key: test-key", ignoreCase = true) })
                val fields = Json.parseToJsonElement(body).jsonObject
                assertEquals(setOf("app_id", "installation_id", "event_id", "occurred_at"), fields.keys)
                assertEquals(visit.eventId, fields.getValue("event_id").jsonPrimitive.content)
                assertEquals(visit.occurredAt, fields.getValue("occurred_at").jsonPrimitive.content)
            }
        }
    }

    @Test fun rateLimitHonorsRetryAfterWhileServerErrorsRemainRetryable() {
        withServer(429, "Retry-After: 300\r\n") { sender, captured ->
            val before = System.currentTimeMillis()
            val result = sender.send(visit) as AppscopeDelivery.Retry
            assertTrue(result.notBefore >= before + 300_000)
            captured()
        }
        withServer(503) { sender, captured ->
            assertTrue(sender.send(visit) is AppscopeDelivery.Retry)
            captured()
        }
    }

    @Test fun invalidPayloadOrKeyIsPermanentAndRedirectsAreNotFollowed() {
        for (status in listOf(400, 401, 413, 415, 422)) {
            withServer(status) { sender, captured ->
                assertEquals(AppscopeDelivery.Rejected(status), sender.send(visit))
                captured()
            }
        }
        withServer(302, "Location: https://example.com/\r\n") { sender, captured ->
            assertTrue(sender.send(visit) is AppscopeDelivery.Retry)
            captured()
        }
    }

    private fun withServer(
        status: Int,
        extraHeaders: String = "",
        test: (AppscopeHttpSender, () -> Pair<List<String>, String>) -> Unit,
    ) {
        val executor = Executors.newSingleThreadExecutor()
        ServerSocket().use { server ->
            server.bind(InetSocketAddress("127.0.0.1", 0))
            server.soTimeout = 5000
            val captured = executor.submit<Pair<List<String>, String>> {
                server.accept().use { socket ->
                    socket.soTimeout = 5000
                    val reader = socket.getInputStream().bufferedReader()
                    val headers = mutableListOf<String>()
                    while (true) {
                        val line = reader.readLine() ?: break
                        if (line.isEmpty()) break
                        headers += line
                    }
                    val length = headers.first { it.startsWith("Content-Length:", true) }.substringAfter(':').trim().toInt()
                    val buffer = CharArray(length)
                    var offset = 0
                    while (offset < length) {
                        val count = reader.read(buffer, offset, length - offset)
                        check(count > 0)
                        offset += count
                    }
                    socket.getOutputStream().write(("HTTP/1.1 $status Test\r\nContent-Length: 2\r\nConnection: close\r\n$extraHeaders\r\n{}").toByteArray())
                    headers to String(buffer)
                }
            }
            try {
                test(AppscopeHttpSender("http://127.0.0.1:${server.localPort}/api/track", "test-key")) {
                    captured.get(5, TimeUnit.SECONDS)
                }
            } finally {
                executor.shutdownNow()
            }
        }
    }
}
