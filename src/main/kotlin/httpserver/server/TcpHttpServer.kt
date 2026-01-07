package httpserver.server

import httpserver.handler.ImageHandler
import httpserver.handler.TextHandler
import httpserver.handler.TimeHandler
import httpserver.http.HttpMethod
import httpserver.routing.Router
import httpserver.storage.TextStore
import httpserver.util.RequestParser
import httpserver.util.ResponseWriter
import java.net.ServerSocket
import java.net.Socket

class TcpHttpServer(private val port: Int) {
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
            Thread {
                handleConnection(socket)
            }.start()
        }
    }

    private fun handleConnection(socket: Socket) {
        socket.soTimeout = 500 // 0.5 초

        socket.use { client ->
            val request = RequestParser.parse(client.getInputStream())
            val response = router.route(request)
            ResponseWriter.write(client.getOutputStream(), response)
        }
    }
}