package httpserver.util

import httpserver.http.HttpMethod
import httpserver.http.HttpRequest
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object RequestParser {

    fun parse(input: InputStream): HttpRequest {
        // 1. 요청 읽기 후 파싱
        val reader = BufferedReader(InputStreamReader(input, Charsets.UTF_8))

        val requestLine = reader.readLine() ?: throw IllegalArgumentException("Empty Request")

        val (method, path, version) = parseRequestLine(requestLine)

        // 2. Headers 읽기
        val headers = mutableMapOf<String, String>()
        while(true) {
            val line = reader.readLine() ?: break
            if (line.isEmpty()) break

            val (key, value) = parseHeader(line)
            headers[key.lowercase()] = value
        }


        // 3. Body 처리
        val body = parseBody(headers, input)

        return HttpRequest(
            method = HttpMethod.valueOf(method),
            path = path,
            version = version,
            headers = headers,
            body = body
        )
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

        val body = ByteArray(length)
        var read = 0
        while (read < length) {
            val r = input.read(body, read, length-read)
            if (r == -1) break
            read += r
        }

        return body
    }
}