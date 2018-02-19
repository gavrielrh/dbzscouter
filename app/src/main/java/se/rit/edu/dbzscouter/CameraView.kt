@file:Suppress("DEPRECATION")

package se.rit.edu.dbzscouter

import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView

import java.io.IOException

class CameraView : SurfaceView, SurfaceHolder.Callback {
    private var camera: Camera? = null
    private var myHolder: SurfaceHolder? = null
    private val TAG = "CameraView"

    constructor(context: Context) : super(context) {}

    constructor(context: Context, camera: Camera) : super(context) {

        this.camera = camera
        this.myHolder = holder
        this.myHolder
        this.myHolder?.addCallback(this)
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        try {
            camera?.setPreviewDisplay(myHolder)
            camera?.startPreview()
        } catch (e: IOException) {
            Log.d(TAG, "Error setting camera view: " + e.message)
        }

    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
        try {
            camera?.stopPreview()
        } catch (ignored: Exception) {

        }

        StartPreview()
    }

    fun StartPreview() {
        try {
            camera?.setPreviewDisplay(myHolder)
            camera?.startPreview()
        } catch (e: Exception) {
            Log.d(TAG, "Error starting camera view: " + e.message)
        }

    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        try {
            camera?.stopPreview()
        } catch (ignored: Exception) {

        }

        camera?.release()
    }
}
