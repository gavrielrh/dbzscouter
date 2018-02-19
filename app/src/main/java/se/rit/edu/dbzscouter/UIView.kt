package se.rit.edu.dbzscouter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import java.io.ByteArrayOutputStream


class UIView(context: Context) : android.support.v7.widget.AppCompatImageView(context) {
    private var scouter_overlay: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.scouteroverlay)
    private var top_position = 0f

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val scaledWidth = context.resources.displayMetrics.widthPixels
        val scaledHeight = (scouter_overlay.height * scaledWidth) / scouter_overlay.width

        top_position = (context.resources.displayMetrics.heightPixels / 2f) - (scaledHeight / 2f)

        scouter_overlay = Bitmap.createScaledBitmap(scouter_overlay, scaledWidth, scaledHeight, false)
        scouter_overlay.compress(Bitmap.CompressFormat.PNG, 100, ByteArrayOutputStream())

        return super.onCreateDrawableState(extraSpace)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(scouter_overlay, 0f, top_position, null)
    }
}
