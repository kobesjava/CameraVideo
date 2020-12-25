package com.jaki.cv.server

import android.util.Log
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

class ServerSocketLive {

    var webSockets = mutableListOf<WebSocket>()

    private val webSocketServer = object : WebSocketServer(InetSocketAddress(ServerConfig.port)) {
        override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
            conn?.let {
                webSockets.add(conn)
            }
        }

        override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
            webSockets.remove(conn)
            Log.i("Jaki", "ServerSocketLive onClose onClose code=$code  reason=$reason")
        }

        override fun onMessage(conn: WebSocket?, message: String?) {
        }

        override fun onError(conn: WebSocket?, ex: Exception?) {
            Log.i("Jaki", "ServerSocketLive onError onError Exception=$ex")
        }

        override fun onStart() {
            Log.i("Jaki", "ServerSocketLive onStart")
        }
    }

    fun start() {
        webSocketServer.start()
    }

    fun send(byteArr: ByteArray) {
        Log.i("Jaki", "ServerSocketLive 发送消息 ${byteArr.size}")
        webSockets.forEach {
            if (it.isOpen) {
                it.send(byteArr)
            }
        }
    }

    fun stop() {
        webSockets.forEach {
            it.close()
        }
        webSockets.clear()
        webSocketServer.stop()
    }

}