package httpserver.server

import httpserver.handler.ImageHandler
import httpserver.handler.TextHandler
import httpserver.handler.TimeHandler
import httpserver.http.HttpMethod
import httpserver.http.HttpResponse
import httpserver.routing.Router
import httpserver.storage.TextStore
import httpserver.util.RequestParser
import httpserver.util.ResponseWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.ServerSocket
import java.net.Socket

class TcpHttpServer(private val port: Int) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val textStore = TextStore()
    private val textHandler = TextHandler(textStore)

    private val router = Router().apply {
        register(HttpMethod.GET, "/time", TimeHandler::handle)
        register(HttpMethod.POST, "/text/{id}", textHandler::create)
        register(HttpMethod.GET, "/text/{id}", textHandler::get)
        register(HttpMethod.GET, "/textall", textHandler::getAll)
        register(HttpMethod.DELETE, "/text/{id}", textHandler::delete)
        register(HttpMethod.GET, "/image", ImageHandler::handle)
    }

    fun start() {
        val serverSocket = ServerSocket(port)
        println("Listening on $port")

        while (true) {
            val socket = serverSocket.accept()

            scope.launch {
                handleConnection(socket)
            }
        }
    }

    private fun handleConnection(socket: Socket) {
        socket.soTimeout = 1000 // 1 초

        socket.use { client ->
            try {
                val request = RequestParser.parse(client.getInputStream())
                val response = router.route(request)
                ResponseWriter.write(client.getOutputStream(), response)
            } catch (_: Exception) {
                ResponseWriter.write(
                    client.getOutputStream(),
                    HttpResponse.internalServerError()
                )
            }
        }
    }
}