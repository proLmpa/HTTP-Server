package httpserver.util

import httpserver.http.HttpResponse
import java.io.OutputStream

object ResponseWriter {

    fun write(output: OutputStream, response: HttpResponse) {
        // 1. Status Line + Header 작성
        val sb = StringBuilder()

        sb.append("HTTP/1.1 ")
            .append(response.status.code)
            .append(' ')
            .append(response.status.reason)
            .append("\r\n")

        response.headers.forEach { (key, value) ->
            sb.append(key).append(": ").append(value).append("\r\n")
        }

        // 2. Header-Body 구분선 (\r\n) 작성
        sb.append("\r\n")

        // 3. String -> ByteArray 변환 후 한 번에 write
        output.write(sb.toString().toByteArray(Charsets.US_ASCII))

        // 4. Body는 그대로 write
        response.body?.let {
            output.write(it)
        }

        output.flush()
    }
}