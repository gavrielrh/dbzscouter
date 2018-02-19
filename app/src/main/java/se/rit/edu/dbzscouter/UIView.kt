package se.rit.edu.dbzscouter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas

class UIView(context: Context) : android.support.v7.widget.AppCompatImageView(context) {
    private val bmp: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.scouteroverlay)

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bmp, 0f, 0f, null)
    }
}
