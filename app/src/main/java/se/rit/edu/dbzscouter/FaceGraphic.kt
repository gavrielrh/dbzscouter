package se.rit.edu.dbzscouter

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.*
import android.os.SystemClock
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
    private var hairstyles : HashMap<Int, Int>
    private var rankings : HashMap<Int, String>

    private var powerLevel : PowerLevel? = null

    private val reticleYOffset = 45f
    private val rankYOffset = 55f
    private val hairYOffsetFraction = .8f
    private val hairWidthExtra = 200
    private val reticleWidthHeightExtra = 200
    private val res = resources
    private var lastTimeChanged : Long = 0
    private val MILISECS_BETWEEN_READS = 2000
    private var powerLevelRank : String? = null
    private var powerLevelNumber : Int? = null
    private var hairId : Int? = null

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
                12 to R.drawable.ssn,
                13 to R.drawable.ss1,
                14 to R.drawable.ss2,
                15 to R.drawable.ss3,
                16 to R.drawable.ss4,
                17 to R.drawable.ssr,
                18 to R.drawable.ssb,
                19 to R.drawable.ssro,
                20 to R.drawable.ssg
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

        val reticleWidthHeight = face.height.toInt() + reticleWidthHeightExtra
        val reticleX = faceCenterX - reticleWidthHeight / 2
        val reticleY = faceCenterY + reticleYOffset - reticleWidthHeight / 2
        val scaledReticle = Rect(reticleX.toInt(), reticleY.toInt(), reticleX.toInt() + reticleWidthHeight, reticleY.toInt() + reticleWidthHeight)

        if(SystemClock.uptimeMillis() - lastTimeChanged > MILISECS_BETWEEN_READS) {
            powerLevel?.setFaceGraphic(this)
            powerLevelNumber = powerLevel!!.calculatePowerLevel()
            val rankNum = (rankings.size * powerLevel!!.getPowerLevelPercentage()).toInt()
            System.err.println("RankNum: " + rankNum)
            powerLevelRank = rankings[rankNum]
            hairId = hairstyles[rankNum]
            lastTimeChanged = SystemClock.uptimeMillis()
        }

        //Draw all bitmaps
        if (hairId != null) {
            val hair = lessResolution(res, hairId!!)
            val hairWidth = scaledReticle.width() + hairWidthExtra
            val hairHeight = (hair.height * hairWidth) / hair.width
            val hairX = faceCenterX - hairWidth / 2
            val hairY = faceCenterY - hairHeight * hairYOffsetFraction
            val scaledHair= Rect(hairX.toInt(), hairY.toInt(), hairX.toInt() + hairWidth, hairY.toInt() + hairHeight)
            canvas.drawBitmap(hair, Rect(0, 0, hair.width, hair.height), scaledHair, null)
        }
        if (powerLevelRank != null) {
            canvas.drawText(powerLevelRank, faceCenterX - mIdPaint.measureText(powerLevelRank) / 2, reticleY + scaledReticle.height() + rankYOffset, mIdPaint)
        }
        if(powerLevelNumber != null) {
            canvas.drawText(String.format("%d", powerLevelNumber), faceCenterX - mIdPaint.measureText(String.format("%d", powerLevelNumber)) / 2, reticleY, mIdPaint)
        }
        canvas.drawBitmap(reticle, Rect(0, 0, reticle.width, reticle.height), scaledReticle, null)
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
