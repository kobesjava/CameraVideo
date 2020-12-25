package com.jaki.cv.client

interface ClientMediaCodec {

    fun start()

    fun stop()

    fun decodeData(data: ByteArray)

}