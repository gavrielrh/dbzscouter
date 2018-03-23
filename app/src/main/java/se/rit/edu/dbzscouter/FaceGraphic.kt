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
    private val mFont: Typeface = Typeface.createFromAsset(resources.assets, "fonts/Square.ttf")
    private val TEXT_SIZE = 80.0f

    private var reticle: Bitmap
    private var hairstyles : HashMap<Int, Bitmap>
    private var rankings : HashMap<Int, String>

    private var powerLevel : PowerLevel? = null

    private val reticleYOffset = 45f
    private val rankYOffset = 55f
    private val hairYOffsetFraction = .8f
    private val hairWidthExtra = 200
    private val reticleWidthHeightExtra = 200


    init {
        val selectedColor = Color.rgb(255, 214, 10)

        mFacePositionPaint = Paint()
        mFacePositionPaint.color = selectedColor

        mIdPaint = Paint()
        mIdPaint.color = selectedColor
        mIdPaint.textSize = TEXT_SIZE
        mIdPaint.typeface = mFont

        reticle = lessResolution(resources, R.drawable.faceoverlay)
        hairstyles = hashMapOf(
                12 to lessResolution(resources, R.drawable.ssn),
                13 to lessResolution(resources, R.drawable.ss1),
                14 to lessResolution(resources, R.drawable.ss2),
                15 to lessResolution(resources, R.drawable.ss3),
                16 to lessResolution(resources, R.drawable.ss4),
                17 to lessResolution(resources, R.drawable.ssr),
                18 to lessResolution(resources, R.drawable.ssb),
                19 to lessResolution(resources, R.drawable.ssro),
                20 to lessResolution(resources, R.drawable.ssg)
        )
        rankings = hashMapOf(
                0 to "Baby",
                1 to "Kid",
                2 to "Weakling",
                3 to "Strong",
                4 to "Fighter",
                5 to "Champion",
                6 to "Z Fighter",
                7 to "x1 Kaioken",
                8 to "x2 Kaioken",
                9 to "x3 Kaioken",
                10 to "x4 Kaioken",
                11 to "x10 Kaioken",
                12 to "x20 Kaioken",
                13 to "Super Saiyan 1",
                14 to "Super Saiyan 2",
                15 to "Super Saiyan 3",
                16 to "Super Saiyan 4",
                17 to "Super Saiyan God",
                18 to "Super Saiyan Blue",
                19 to "Super Saiyan Rose",
                20 to "Legendary Super Saiyan"
        )
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
        val faceCenterX = translateX(face.position.x + face.width / 2)
        val faceCenterY = translateY(face.position.y + face.height / 2)

        val scaledReticle = Bitmap.createScaledBitmap(reticle, face.height.toInt() + reticleWidthHeightExtra, face.height.toInt() + reticleWidthHeightExtra, false)
        val reticleX = faceCenterX - scaledReticle.width / 2
        val reticleY = faceCenterY + reticleYOffset - scaledReticle.height / 2

        powerLevel?.setFaceGraphic(this)
        val powerLevelNumber = powerLevel!!.calculatePowerLevel()
        val powerLevelRank = rankings[powerLevelNumber / (powerLevel!!.getBasePowerLevel() / (rankings.size - 1))]
        val hair = hairstyles[powerLevelNumber / (powerLevel!!.getBasePowerLevel() / (rankings.size - 1))]

        //Draw all bitmaps
        if (hair != null) {
            val hairWidth = scaledReticle.width + hairWidthExtra
            val hairHeight = (hair.height * hairWidth) / hair.width
            val scaledHairGraphic = Bitmap.createScaledBitmap(hair, hairWidth, hairHeight, false)
            canvas.drawBitmap(scaledHairGraphic, faceCenterX - scaledHairGraphic.width / 2, faceCenterY - (scaledHairGraphic.height * hairYOffsetFraction), null)
        }
        if (powerLevelRank != null) {
            canvas.drawText(powerLevelRank, faceCenterX - mIdPaint.measureText(powerLevelRank) / 2, reticleY + scaledReticle.height + rankYOffset, mIdPaint)
        }
        canvas.drawText(String.format("%d",powerLevelNumber), faceCenterX - mIdPaint.measureText(String.format("%d",powerLevelNumber)) / 2, reticleY, mIdPaint)
        canvas.drawBitmap(scaledReticle, reticleX, reticleY, null)
    }

    fun getEmotionVal() : Double{
        val face = mFace ?: return 0.0

        return (1 - face.isSmilingProbability).toDouble()
    }

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
