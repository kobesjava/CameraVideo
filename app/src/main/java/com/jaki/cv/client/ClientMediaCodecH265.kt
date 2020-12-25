package com.jaki.cv.client

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface

class ClientMediaCodecH265(private val surface: Surface) : ClientMediaCodec {

    private var mediaCodec: MediaCodec? = null

    override fun start() {
        try {
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, 1536, 2048)
            format.setInteger(MediaFormat.KEY_BIT_RATE, 1536 * 2048)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            mediaCodec?.configure(format, surface, null, 0)
            mediaCodec?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stop() {
        mediaCodec?.stop()
    }

    override fun decodeData(data: ByteArray) {
        mediaCodec?.let {
            val inputIndex = it.dequeueInputBuffer(10000)
            if (inputIndex >= 0) {
                val inputBuffer = it.getInputBuffer(inputIndex)
                inputBuffer?.let {
                    it.clear()
                    it.put(data, 0, data.size)
                    mediaCodec?.queueInputBuffer(
                        inputIndex,
                        0,
                        data.size,
                        System.currentTimeMillis(),
                        0
                    )
                }
            }
            val bufferInfo = MediaCodec.BufferInfo()
            var outPutIndex = it.dequeueOutputBuffer(bufferInfo, 10000)
            while (outPutIndex >= 0) {
                it.releaseOutputBuffer(outPutIndex, true)
                outPutIndex = it.dequeueOutputBuffer(bufferInfo, 10000)
            }
        }
    }
}