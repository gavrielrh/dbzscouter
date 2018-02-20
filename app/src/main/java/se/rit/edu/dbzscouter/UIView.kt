package se.rit.edu.dbzscouter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import java.io.ByteArrayOutputStream


class UIView(context: Context) : android.support.v7.widget.AppCompatImageView(context) {
    private var scouterOverlay: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.scouteroverlay)
    private var topPosition = 0f

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val scaledWidth = context.resources.displayMetrics.widthPixels
        val scaledHeight = (scouterOverlay.height * scaledWidth) / scouterOverlay.width

        topPosition = (context.resources.displayMetrics.heightPixels / 2f) - (scaledHeight / 2f)

        scouterOverlay = Bitmap.createScaledBitmap(scouterOverlay, scaledWidth, scaledHeight, false)
        scouterOverlay.compress(Bitmap.CompressFormat.PNG, 100, ByteArrayOutputStream())

        return super.onCreateDrawableState(extraSpace)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(scouterOverlay, 0f, topPosition, null)
    }
}
