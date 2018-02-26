@file:Suppress("DEPRECATION")

package se.rit.edu.dbzscouter

import android.Manifest
import android.content.res.Configuration
import android.hardware.Camera
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import android.widget.ImageView
import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED


const val CAMERA_CODE = 0x01
const val RECORD_AUDIO_CODE = 0x02

class MainActivity : AppCompatActivity() {

    private lateinit var camera: Camera
    internal lateinit var layout: FrameLayout
    private lateinit var ui: ImageView
    private lateinit var cameraView: CameraView
    private lateinit var micRecorder: MicRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(this, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA), CAMERA_CODE)
        } else {
            loadCamera()
        }
        if (ActivityCompat.checkSelfPermission(this, RECORD_AUDIO) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(RECORD_AUDIO), RECORD_AUDIO_CODE)
        } else {
            loadMicrophone()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_CODE -> {
                loadCamera()
            }
            RECORD_AUDIO_CODE -> {
                loadMicrophone()
            }
        }
    }

    private fun loadCamera() {
        camera = turnOnCamera()
        cameraView = CameraView(this, camera)

        layout = findViewById(R.id.camera_view)
        layout.addView(cameraView, 0)

        ui = findViewById(R.id.ui_view)
        ui = UIView(this)

        layout.addView(ui)
    }

    private fun loadMicrophone() {
        micRecorder = MicRecorder()
        turnOnMic()
    }


    /**
     * Opens up the camera and sets its orientation based on the phone's orientation
     *
     * @return the newly created camera
     */
    private fun turnOnCamera(): Camera {
        val camera = Camera.open()

        val parameters = camera.parameters

        if (this.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            parameters.set("orientation", "portrait")
            camera.setDisplayOrientation(90)
            parameters.setRotation(90)
        } else {
            parameters.set("orientation", "landscape")
            camera.setDisplayOrientation(0)
            parameters.setRotation(0)
        }

        return camera
    }

    private fun turnOnMic() {
         micRecorder.startRecording()
    }

    private fun turnOffMic(){
        micRecorder.close()
    }
}
