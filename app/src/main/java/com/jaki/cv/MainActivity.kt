package com.jaki.cv

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import com.jaki.cv.client.ClientManage
import com.jaki.cv.server.ServerManage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var clientManage = ClientManage()
    var serverManage = ServerManage()
    var surface: Surface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycle.addObserver(serverManage)
        lifecycle.addObserver(clientManage)
        localSurfaceView.listener = serverManage
        remoteSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                surface = holder.surface
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10000) {
            localSurfaceView.startPreView()
        }
    }

    fun push(view: View) {
        localSurfaceView.size?.let {
            serverManage.start(it.width, it.height)
        }
    }

    fun connect(view: View) {
        surface?.let {
            clientManage.connect(it)
        }
    }

}