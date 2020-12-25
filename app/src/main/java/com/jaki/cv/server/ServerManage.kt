package com.jaki.cv.server

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class ServerManage : DefaultLifecycleObserver, LocaSurfaceView.LocalSurfaceFrameListener,
    ServerMediaCodecListener {

    var mediaCodec: ServerMediaCodec? = null
    private val socketLive = ServerSocketLive()

    fun start(width: Int, height: Int) {
        mediaCodec = ServerMediaCodecH265(width, height, this)
        mediaCodec?.start()
        socketLive.start()
    }

    override fun onPreviewFrame(data: ByteArray) {
        mediaCodec?.encodeData(data)
    }

    override fun onMediaCodec(data: ByteArray) {
        socketLive.send(data)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        mediaCodec?.stop()
        mediaCodec = null
        socketLive.stop()
    }
}