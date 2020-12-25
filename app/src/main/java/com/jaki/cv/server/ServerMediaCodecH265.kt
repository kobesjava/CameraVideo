package com.jaki.cv.server

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import com.jaki.cv.YuvUtils
import java.nio.ByteBuffer

class ServerMediaCodecH265(
    val width: Int,
    val height: Int,
    val listener: ServerMediaCodecListener
) :
    ServerMediaCodec {

    var mediaCodec: MediaCodec? = null
    private val nalI = 19
    private val nalVps = 32
    var vpsSpsPpsBuf: ByteArray? = null
    var nv12: ByteArray? = null
    var yuv: ByteArray? = null
    var frameIndex = 0L

    override fun start() {
        try {
            val mediaFormat =
                MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, height, width)
            mediaFormat.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
            )
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height)
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)//1秒一个I帧
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
            mediaCodec?.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mediaCodec?.start()
            //nv12 = ByteArray(width * height * 3 / 2)
            yuv = ByteArray(width * height * 3 / 2)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stop() {

    }

    override fun encodeData(data: ByteArray) {
        //nv21 转 nv12
        nv12 = YuvUtils.nv21toNV12(data)
        //旋转90度
        YuvUtils.portraitData2Raw(nv12, yuv, width, height)

        mediaCodec?.let {
            val inputBufferIndex = it.dequeueInputBuffer(10000)
            if (inputBufferIndex >= 0) {
                val inputBuffer = it.getInputBuffer(inputBufferIndex)
                inputBuffer?.clear()
                inputBuffer?.put(yuv)
                val presentationTimeUs: Long = computePresentationTime(frameIndex)
                it.queueInputBuffer(
                    inputBufferIndex,
                    0,
                    yuv?.size ?: 0,
                    presentationTimeUs,
                    0
                )
                frameIndex++
            }
            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = it.dequeueOutputBuffer(bufferInfo, 10000)
            while (outputBufferIndex >= 0) {
                it.getOutputBuffer(outputBufferIndex)?.let {
                    dealFrame(it, bufferInfo)
                }
                it.releaseOutputBuffer(outputBufferIndex, false)
                outputBufferIndex = it.dequeueOutputBuffer(bufferInfo, 0)
            }
        }
    }

    private fun computePresentationTime(frameIndex: Long): Long {
        return 132 + frameIndex * 1000000 / 15
    }

    private fun dealFrame(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        var offset = 4 //默认分隔符是 00 00 00 01
        if (byteBuffer.get(2).toInt() == 0x01) { //分隔符是 00 00 01
            offset = 3
        }
        val type = (byteBuffer.get(offset).toInt() and 0x7E) shr 1
        if (type == nalVps) {
            vpsSpsPpsBuf = ByteArray(bufferInfo.size)
            byteBuffer.get(vpsSpsPpsBuf)
        } else if (type == nalI) {
            val bytes = ByteArray(bufferInfo.size)
            byteBuffer.get(bytes)
            val newBUf = ByteArray((vpsSpsPpsBuf?.size ?: 0) + bytes.size)
            System.arraycopy(vpsSpsPpsBuf, 0, newBUf, 0, vpsSpsPpsBuf?.size ?: 0)
            System.arraycopy(bytes, 0, newBUf, vpsSpsPpsBuf?.size ?: 0, bytes.size)
            listener.onMediaCodec(newBUf)
        } else {
            val bytes = ByteArray(bufferInfo.size)
            byteBuffer.get(bytes)
            listener.onMediaCodec(bytes)
        }
    }
}