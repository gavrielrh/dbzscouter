package se.rit.edu.dbzscouter

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.*
import com.google.android.gms.vision.face.Face
import se.rit.edu.dbzscouter.ui.camera.GraphicOverlay

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic(overlay: GraphicOverlay?, resources: Resources) : GraphicOverlay.Graphic(overlay!!) {

    private val mFacePositionPaint: Paint
    private val mIdPaint: Paint

    @Volatile
    private var mFace: Face? = null
    private var mFaceId: Int = 0
    private val mFaceHappiness: Float = 0.toFloat()
    private val mFont: Typeface = Typeface.createFromAsset(resources.assets, "fonts/Square.ttf")
    private var faceGraphic: Bitmap
    private var powerLevel : PowerLevel?  = null

    init {
        val selectedColor = Color.rgb(255, 214, 10)

        mFacePositionPaint = Paint()
        mFacePositionPaint.color = selectedColor

        mIdPaint = Paint()
        mIdPaint.color = selectedColor
        mIdPaint.textSize = ID_TEXT_SIZE
        mIdPaint.typeface = mFont

        faceGraphic = lessResolution(resources, R.drawable.faceoverlay)
    }

    fun setId(id: Int) {
        mFaceId = id
    }

    fun setPowerLevel(powerLevel: PowerLevel){
        this.powerLevel = powerLevel
        powerLevel.setFaceGraphic(this)
    }

    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    fun updateFace(face: Face) {
        mFace = face
        postInvalidate()
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @SuppressLint("DefaultLocale")
    override fun draw(canvas: Canvas) {
        val face = mFace ?: return

        // Draws a circle at the position of the detected face, with the face's track id below.
        val x = translateX(face.position.x + face.width / 2)
        val y = translateY(face.position.y + face.height / 2)

        var scaledFaceGraphic = Bitmap.createScaledBitmap(faceGraphic, face.height.toInt() + 200, face.height.toInt() + 200, false)

        val faceGraphicLeft = x + ID_X_OFFSET - scaledFaceGraphic.width / 2
        val faceGraphicTop = y + ID_Y_OFFSET - scaledFaceGraphic.height / 2

        canvas.drawBitmap(scaledFaceGraphic, faceGraphicLeft, faceGraphicTop, null)

        powerLevel?.calculatePowerLevel()

        canvas.drawText(String.format("%d", powerLevel!!.calculatePowerLevel()), x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint)
    }

    fun getEmotionVal() : Double{
        val face = mFace ?: return 0.0

        return (1 - face.isSmilingProbability).toDouble()
    }

    companion object {
        private val ID_TEXT_SIZE = 80.0f
        private val ID_Y_OFFSET = 45f
        private val ID_X_OFFSET = 0f

        fun lessResolution(res : Resources, resId : Int) : Bitmap{
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(res, resId, options)

            var width = 500
            var height = 500

            options.inSampleSize = calculateInSampleSize(options, width, height)
            options.inJustDecodeBounds = false

            return BitmapFactory.decodeResource(res, resId, options)
        }

        fun calculateInSampleSize(options : BitmapFactory.Options, reqWidth : Int, reqHeight : Int) : Int {
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {

                val halfHeight = height / 2
                val halfWidth = width / 2

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }
    }
}
