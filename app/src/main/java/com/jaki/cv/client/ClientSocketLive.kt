package com.jaki.cv.client

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer

class ClientSocketLive(val listener: ClientSocketCallback) {

    private var myWebSocketClient: MyWebSocketClient? = null

    fun start() {
        try {
            val url = URI("ws://${ClientConfig.address}:${ClientConfig.port}")
            myWebSocketClient = MyWebSocketClient(url)
            myWebSocketClient?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        myWebSocketClient?.close()
    }

    private inner class MyWebSocketClient(serverURI: URI) : WebSocketClient(serverURI) {

        override fun onOpen(serverHandshake: ServerHandshake) {
            Log.i("Jaki", "ReciveSocketLive 打开 socket  onOpen: ")
        }

        override fun onMessage(message: String?) {

        }

        override fun onMessage(bytes: ByteBuffer) {
            Log.i("Jaki", "ReciveSocketLive 消息长度  : " + bytes.remaining())
            val buf = ByteArray(bytes.remaining())
            bytes.get(buf)
            listener.onReciveData(buf)
        }

        override fun onClose(i: Int, s: String, b: Boolean) {
            Log.i("Jaki", "ReciveSocketLive onClose  i=$i  s=$s  b=$b")
        }

        override fun onError(e: Exception) {
            Log.i("Jaki", "ReciveSocketLive onError: $e")
        }
    }

    interface ClientSocketCallback {
        fun onReciveData(data: ByteArray)
    }

}