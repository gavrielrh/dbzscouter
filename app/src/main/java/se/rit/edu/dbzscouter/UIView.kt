package se.rit.edu.dbzscouter

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import java.io.ByteArrayOutputStream

class UIView(context: Context) : android.support.v7.widget.AppCompatImageView(context) {
    private var scouterOverlay: Bitmap = lessResolution(context, resources, R.drawable.scouteroverlay)
    private var topPosition = (context.resources.displayMetrics.heightPixels / 2f) - (scouterOverlay.height / 2f)

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(scouterOverlay, 0f, topPosition, null)
    }

    companion object {
        fun lessResolution(context : Context, res : Resources, resId : Int) : Bitmap{
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(res, resId, options)

            var width = context.resources.displayMetrics.widthPixels
            var height = (options.outHeight * width) / options.outWidth

            options.inSampleSize = calculateInSampleSize(options, width, height)
            options.inJustDecodeBounds = false

            //return BitmapFactory.decodeResource(res, resId, options)
            return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, resId, options), width, height, false)
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
