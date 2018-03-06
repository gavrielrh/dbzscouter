package se.rit.edu.dbzscouter

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.*
import com.google.android.gms.vision.face.Face
import se.rit.edu.dbzscouter.ui.camera.GraphicOverlay
import java.io.ByteArrayOutputStream

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
internal class FaceGraphic(overlay: GraphicOverlay?, resources: Resources) : GraphicOverlay.Graphic(overlay!!) {

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

        mCurrentColorIndex = 2//(mCurrentColorIndex + 1) % COLOR_CHOICES.size
        val selectedColor = COLOR_CHOICES[mCurrentColorIndex]

        mFacePositionPaint = Paint()
        mFacePositionPaint.color = selectedColor

        mIdPaint = Paint()
        mIdPaint.color = selectedColor
        mIdPaint.textSize = ID_TEXT_SIZE
        mIdPaint.typeface = mFont

        faceGraphic = BitmapFactory.decodeResource(resources, R.drawable.faceoverlay)
        val scaledWidth = resources.displayMetrics.widthPixels
        val scaledHeight = (faceGraphic.height * scaledWidth) / faceGraphic.width

        faceGraphic = Bitmap.createScaledBitmap(faceGraphic, scaledWidth, scaledHeight, false)
        faceGraphic.compress(Bitmap.CompressFormat.PNG, 100, ByteArrayOutputStream())
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

        val faceGraphicLeft = x + ID_X_OFFSET - faceGraphic.width / 4
        val faceGraphicTop = y + ID_Y_OFFSET - faceGraphic.height / 2
        canvas.drawBitmap(faceGraphic, faceGraphicLeft, faceGraphicTop, null)

        powerLevel?.calculatePowerLevel()

        canvas.drawText(String.format("%d", powerLevel!!.calculatePowerLevel()), x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint)
    }

    fun getEmotionVal() : Double{
        val face = mFace ?: return 0.0

        return (1 - face.isSmilingProbability).toDouble()
    }

    companion object {
        private val FACE_POSITION_RADIUS = 10.0f
        private val ID_TEXT_SIZE = 80.0f
        private val ID_Y_OFFSET = 50.0f
        private val ID_X_OFFSET = -50.0f

        private val COLOR_CHOICES = intArrayOf(Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED, Color.WHITE, Color.YELLOW)
        private var mCurrentColorIndex = 0
    }
}
