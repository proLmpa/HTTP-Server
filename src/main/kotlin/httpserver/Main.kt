package httpserver

import httpserver.server.TcpHttpServer

fun main() {
    TcpHttpServer(port = 8080).start()
}