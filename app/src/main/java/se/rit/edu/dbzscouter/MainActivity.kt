@file:Suppress("DEPRECATION")

package se.rit.edu.dbzscouter

import android.content.res.Configuration
import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    private lateinit var camera: Camera
    internal lateinit var layout: FrameLayout
    private lateinit var ui: ImageView
    private lateinit var cameraView: CameraView
    private lateinit var micRecorder: MicRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        camera = turnOnCamera()
        cameraView = CameraView(this, camera)

        layout = findViewById(R.id.camera_view)
        layout.addView(cameraView, 0)

        ui = findViewById(R.id.ui_view)

        ui = UIView(this)

        micRecorder = MicRecorder()
        turnOnMic()

        layout.addView(ui)

        //while(true){
        // System.err.println("Decibels: " + micRecorder.getDecibles())
        //}
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
