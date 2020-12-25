package com.jaki.cv.server

interface ServerMediaCodec {

    fun start()

    fun stop()

    fun encodeData(data: ByteArray)
}