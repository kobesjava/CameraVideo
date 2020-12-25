package com.jaki.cv.server

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView

class LocaSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback, Camera.PreviewCallback {

    var camera: Camera? = null
    var size: Camera.Size? = null
    var buffer: ByteArray? = null
    var listener: LocalSurfaceFrameListener? = null

    init {
        holder.addCallback(this)
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            (context as Activity).requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA
                ), 10000
            )
        } else {
            startPreView()
        }
    }

    fun startPreView() {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
        val paramters = camera?.parameters
        size = paramters?.previewSize
        size?.let {
            layoutParams.width = it.height / 4
            layoutParams.height = it.width / 4
            requestLayout()
        }
        Log.i("Jaki", "preview size width=${size?.width}  height=${size?.height}")
        camera?.setPreviewDisplay(holder)
        camera?.setDisplayOrientation(90)
        buffer = ByteArray((size?.width ?: 0) * (size?.height ?: 0) * 3 / 2)
        camera?.addCallbackBuffer(buffer)
        camera?.setPreviewCallbackWithBuffer(this)
        camera?.startPreview()
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        data?.let {
            listener?.onPreviewFrame(it)
        }
        camera?.addCallbackBuffer(data)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        checkPermission()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    interface LocalSurfaceFrameListener {
        fun onPreviewFrame(data: ByteArray)
    }
}