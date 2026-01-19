package httpserver.util

import httpserver.http.HttpMethod
import httpserver.http.HttpRequest
import java.io.ByteArrayOutputStream
import java.io.InputStream

object RequestParser {

    private const val MAX_HEADER_SIZE = 8 * 1024
    private const val MAX_BODY_SIZE = 1 * 1024 * 1024

    fun parse(input: InputStream): HttpRequest? {
        val raw = readUntilDoubleCRLF(input) ?: return null
        val lines = raw.split("\r\n")

        // 2. Request line
        val (method, path, version) = parseRequestLine(lines.first())

        // 3. Headers
        val headers = mutableMapOf<String, String>()
        for (i in 1 until lines.size) {
            if (lines[i].isEmpty()) break
            val (k, v) = parseHeader(lines[i])
            headers[k.lowercase()] = v
        }

        // 4. Body (Content-Length 기준)
        val body = parseBody(headers, input)

        return HttpRequest(
            method = HttpMethod.valueOf(method),
            path = path,
            version = version,
            headers = headers,
            body = body
        )
    }

    private fun readUntilDoubleCRLF(input: InputStream): String? {
        val buffer = ByteArrayOutputStream()
        var prev = 0
        var curr: Int

        while (true) {
            curr = input.read()
            if (curr == -1) {
                return if (buffer.size() == 0) null else buffer.toString(Charsets.UTF_8)
            }
            buffer.write(curr)

            if (prev == '\r'.code && curr == '\n'.code) {
                val bytes = buffer.toByteArray()
                if (bytes.takeLast(4).toByteArray()
                        .contentEquals("\r\n\r\n".toByteArray())
                ) break
            }
            prev = curr

            if (buffer.size() > MAX_HEADER_SIZE)
                throw IllegalArgumentException("Header too large")
        }

        return buffer.toString(Charsets.UTF_8)
    }

    private fun parseRequestLine(line: String): Triple<String, String, String> {
        val parts = line.split(" ")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid Request Line: $line")
        }

        return Triple(parts[0], parts[1], parts[2])
    }

    private fun parseHeader(line: String): Pair<String, String> {
        val idx = line.indexOf(":")
        if (idx <= 0) {
            throw IllegalArgumentException("Invalid Header: $line")
        }

        val key = line.take(idx)
        val value = line.substring(idx + 1).trim()

        return key to value
    }

    private fun parseBody(headers: Map<String, String>, input: InputStream) : ByteArray? {
        val length = headers["content-length"]?.toIntOrNull() ?: return null

        require(length <= MAX_BODY_SIZE)

        return input.readNBytes(length)
    }
}