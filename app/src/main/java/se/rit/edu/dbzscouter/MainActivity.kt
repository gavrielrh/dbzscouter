@file:Suppress("DEPRECATION")

package se.rit.edu.dbzscouter

import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import se.rit.edu.dbzscouter.ui.camera.CameraSourcePreview
import se.rit.edu.dbzscouter.ui.camera.GraphicOverlay
import java.io.IOException


const val CAMERA_CODE = 0x01
const val RECORD_AUDIO_CODE = 0x02

class MainActivity : AppCompatActivity() {

    private var cameraSource: CameraSource? = null
    private var cameraPreview: CameraSourcePreview? = null
    private var faceOverlay: GraphicOverlay? = null
    private var uiView: ImageView? = null

    private lateinit var powerLevel : PowerLevel
    private lateinit var micRecorder: MicRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        cameraPreview = findViewById(R.id.cameraPreview)

        uiView = UIView(this)
        cameraPreview!!.addView(uiView)

        faceOverlay = findViewById(R.id.faceOverlay)


        if (ActivityCompat.checkSelfPermission(this, RECORD_AUDIO) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(RECORD_AUDIO), RECORD_AUDIO_CODE)
        } else {
            loadMicrophone()
        }

        powerLevel = PowerLevel(micRecorder)

        if (ActivityCompat.checkSelfPermission(this, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA), CAMERA_CODE)
        } else {
            createCameraSource()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_CODE -> {
                createCameraSource()
            }
            RECORD_AUDIO_CODE -> {
                loadMicrophone()
            }
        }
    }

    private fun loadMicrophone() {
        micRecorder = MicRecorder()
        Thread(micRecorder).start()
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private fun createCameraSource() {

        val context = applicationContext
        val detector = FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build()

        detector.setProcessor(
                MultiProcessor.Builder(GraphicFaceTrackerFactory(powerLevel))
                        .build())

        if (!detector.isOperational) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.")
        }

        val w = windowManager
        val d = w.defaultDisplay
        val width = d.width
        val height = d.height

        cameraSource = CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(height, width)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .build()
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {

        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                applicationContext)
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }

        if (cameraSource != null) {
            try {
                cameraPreview!!.start(cameraSource!!, faceOverlay!!)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource!!.release()
                cameraSource = null
            }

        }
    }

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private inner class GraphicFaceTrackerFactory internal constructor(private val powerLevel: PowerLevel) : MultiProcessor.Factory<Face> {

        override fun create(face: Face): Tracker<Face> {
            val mGraphic = GraphicFaceTracker(faceOverlay)
            mGraphic.setFaceGraphicPowerLevel(powerLevel)
            return mGraphic
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private inner class GraphicFaceTracker internal constructor(private val mOverlay: GraphicOverlay?) : Tracker<Face>() {
        private val mFaceGraphic: FaceGraphic = FaceGraphic(mOverlay, resources)

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        override fun onNewItem(faceId: Int, item: Face?) {
            mFaceGraphic.setId(faceId)
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        override fun onUpdate(detectionResults: Detector.Detections<Face>?, face: Face?) {
            mOverlay?.add(mFaceGraphic)
            if (face != null) {
                mFaceGraphic.updateFace(face)
            }
        }

        fun setFaceGraphicPowerLevel(powerLevel: PowerLevel){
            mFaceGraphic.setPowerLevel(powerLevel)
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        override fun onMissing(detectionResults: Detector.Detections<Face>?) {
            mOverlay?.remove(mFaceGraphic)
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        override fun onDone() {
            mOverlay?.remove(mFaceGraphic)
        }
    }

    override fun onPause() {
        super.onPause()
        cameraPreview!!.stop()
        micRecorder.running = false
    }

    override fun onResume() {
        super.onResume()
        startCameraSource()
        Thread(micRecorder).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cameraSource != null) {
            cameraSource!!.release()
        }
    }

    companion object {
        private const val TAG = "FaceTracker"

        private const val RC_HANDLE_GMS = 9001
        // permission request codes need to be < 256
        private const val RC_HANDLE_CAMERA_PERM = 2
    }
}
