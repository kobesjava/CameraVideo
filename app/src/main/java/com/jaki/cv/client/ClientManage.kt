package com.jaki.cv.client

import android.view.Surface
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class ClientManage : DefaultLifecycleObserver, ClientSocketLive.ClientSocketCallback {

    var mediaCodec: ClientMediaCodec? = null
    var socket: ClientSocketLive = ClientSocketLive(this)

    fun connect(surface: Surface) {
        mediaCodec = ClientMediaCodecH265(surface)
        mediaCodec?.start()
        socket.start()
    }

    override fun onReciveData(data: ByteArray) {
        mediaCodec?.decodeData(data)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        mediaCodec?.stop()
        socket.stop()
    }

}