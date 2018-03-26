@file:Suppress("DEPRECATION")

package se.rit.edu.dbzscouter

import android.Manifest
import android.graphics.*
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.support.v7.app.AppCompatActivity
import android.text.format.DateFormat
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import se.rit.edu.dbzscouter.storage.ReadingDatabase
import se.rit.edu.dbzscouter.ui.camera.CameraSourcePreview
import se.rit.edu.dbzscouter.ui.camera.GraphicOverlay
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DBZScouter"

        private const val RC_HANDLE_GMS = 9001
        // permission request codes need to be < 256
        private const val RC_HANDLE_CAMERA_PERM = 2

        const val CAMERA_CODE = 0x01
        const val AUDIO_CODE = 0x02
        const val STORAGE_CODE = 0x04
    }

    private var cameraSource: CameraSource? = null
    private var cameraPreview: CameraSourcePreview? = null
    private var faceOverlay: GraphicOverlay? = null
    private var uiView: ImageView? = null
    private var powerLevel: PowerLevel? = null
    private var canSaveScreenshot = false

    private var screenshotBitmap: Bitmap? = null

    private lateinit var micRecorder: IMicRecorder
    private lateinit var readingDatabase: ReadingDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val camView: CameraSourcePreview = findViewById(R.id.cameraPreview)
        cameraPreview = camView
        uiView = UIView(this)
        cameraPreview!!.addView(uiView)

        faceOverlay = findViewById(R.id.faceOverlay)

        // Initialize the reading database
        readingDatabase = ReadingDatabase.getInstance(this)

        // Determine our current permissions
        val canCamera = hasPermission(Manifest.permission.CAMERA)
        val canAudio = hasPermission(Manifest.permission.RECORD_AUDIO)
        val canStorage = hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        // Request permissions as necessary. For some reason requesting two permissions back-to-back
        // is buggy, and so we request all the permissions at once.
        // Also handle if they only have one or the other granted.
        val permArray = mutableListOf<String>()
        var permCode = 0

        // Load the audio system
        if (canAudio) {
            loadMicrophone()
        } else {
            micRecorder = DummyMicRecorder()
            permArray.add(Manifest.permission.RECORD_AUDIO)
            permCode = permCode or AUDIO_CODE
        }

        // Load the camera
        if (canCamera) {
            createCameraSource()
        } else {
            permArray.add(Manifest.permission.CAMERA)
            permCode = permCode or CAMERA_CODE
        }

        // Check for external storage permissions
        if (canStorage) {
            canSaveScreenshot = true
        } else {
            permArray.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permArray.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permCode = permCode or STORAGE_CODE
        }

        if (permCode != 0) {
            ActivityCompat.requestPermissions(this, permArray.toTypedArray(), permCode)
        }

        // Add tap listener so we can save a screenshot
        camView.setOnTouchListener(::tapHandler)
    }


    private fun tapHandler(view: View, event: MotionEvent): Boolean {
        // Once the user is done pressing, do something
        if (event.action == MotionEvent.ACTION_UP && canSaveScreenshot
                && screenshotBitmap == null) {

            // Get the current preview overlay
            val camView = cameraPreview ?: return false
            // create bitmap screen capture
            camView.isDrawingCacheEnabled = true
            screenshotBitmap = Bitmap.createBitmap(camView.drawingCache)
            camView.isDrawingCacheEnabled = false

            // Tell the camera to take a screen shot
            cameraSource!!.takePicture(::shutterCallback, ::pictureCallback)
        }
        return true
    }

    private fun shutterCallback() {
        Toast.makeText(this, "Saving screenshot...", Toast.LENGTH_SHORT).show()
    }

    private fun pictureCallback(jpegData: ByteArray) {
        // Grab the current overlay and then reset it back to null
        val overlayBmp = screenshotBitmap ?: return
        screenshotBitmap = null
        // Load a bitmap from the jpeg data
        val opts = BitmapFactory.Options()
        opts.inMutable = true
        val camera = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size, opts)
        // Draw the overlay on top
        val canvas = Canvas(camera)
        val srcRect = Rect(0, 0, overlayBmp.width, overlayBmp.height)
        val destRect = Rect(0, 0, camera.width, camera.height)
        canvas.drawBitmap(overlayBmp, srcRect, destRect, null)

        try {
            // Actually save the bitmap
            // image naming and path to include sd card appending name you choose for file
            val nowText = DateFormat.format("yyyy-MM-dd_hh:mm:ss", Date())
            val mPath = Environment.getExternalStorageDirectory().toString() + "/" + nowText + ".jpg"
            val outputStream = FileOutputStream(mPath)
            camera.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
        } catch (e: Throwable) {
            // Several error may come out with file handling or DOM
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {

        if (requestCode and CAMERA_CODE != 0) {
            // For some reason it sends an empty grantResults on first run
            if (isGranted(permissions, grantResults, Manifest.permission.CAMERA)) {
                createCameraSource()
                Log.i(TAG, "CAMERA permission granted")
            } else {
                Log.i(TAG, "CAMERA permission denied")
            }
        }
        if (requestCode and AUDIO_CODE != 0) {
            if (isGranted(permissions, grantResults, Manifest.permission.RECORD_AUDIO)) {
                loadMicrophone()
                Log.i(TAG, "MICROPHONE permission granted")
            } else {
                Log.i(TAG, "MICROPHONE permission denied")
            }
        }
        if (requestCode and STORAGE_CODE != 0) {
            if (isGranted(permissions, grantResults, Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                canSaveScreenshot = true
                Log.i(TAG, "EXTERNAL_STORAGE permission granted")
            } else {
                Log.i(TAG, "EXTERNAL_STORAGE permission denied")
            }
        }
    }

    /**
     * Return true if all permissions from `required` are present in `permissions`
     * and their grantResult is PERMISSION_GRANTED.
     */
    private fun isGranted(permissions: Array<out String>, grantResults: IntArray,
                          vararg required: String): Boolean {
        if (permissions.isEmpty()) {
            return false
        }
        val requiredList = required.toMutableList()
        for (i in 0 until permissions.size) {
            if (permissions[i] in requiredList && grantResults[i] != PERMISSION_GRANTED) {
                return false
            } else {
                requiredList.remove(permissions[i])
            }
        }
        return requiredList.isEmpty()
    }

    private fun hasPermission(perm: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, perm) == PERMISSION_GRANTED
    }

    /**
     * Initialize all microphone-related things. This can happen in several different places
     * in the application (onCreate, onRequestPermissionResult, etc.).
     */
    private fun loadMicrophone() {
        val rec = RealMicRecorder()
        micRecorder = rec
        // Update the micRecorder of the powerLevel
        powerLevel?.micRecorder = rec
        Thread(rec).start()
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private fun createCameraSource() {

        // Initialize the powerLevel with the current micRecorder, be it Dummy or Real
        powerLevel = PowerLevel(micRecorder)

        val context = applicationContext
        val detector = FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build()

        detector.setProcessor(
                MultiProcessor.Builder(GraphicFaceTrackerFactory(powerLevel!!))
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
}
