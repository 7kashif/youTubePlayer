package com.example.videoplayer_app

import android.graphics.Bitmap

import android.app.Activity

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import java.io.InputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


// this class was copied from an answer by mdsabir shaik at
// https://stackoverflow.com/questions/7324759/how-to-display-thumbnail-of-youtube-videos-in-android
class ThumbnailHandler {

    private fun removeBlackBar(bitmap: Bitmap?): Bitmap? {
        return if (bitmap!!.width == 480 && bitmap.height == 360) {
            val pixels = IntArray(bitmap.width * bitmap.height)
            val pixelsOut = IntArray(bitmap.width * bitmap.height)

            // get pixel array from source
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            for (y in 0..269) {
                for (x in 0 until bitmap.width) {
                    pixelsOut[y * bitmap.width + x] = pixels[(y + 45) * bitmap.width + x]
                }
            }
            val bmOut = Bitmap.createBitmap(bitmap.width, 270, bitmap.config)
            bmOut.setPixels(pixelsOut, 0, bitmap.width, 0, 0, bitmap.width, 270)
            bmOut
        } else {
            bitmap
        }
    }

    private fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e("shabir", "err: $e")
            e.printStackTrace()
            null
        }
    }

    fun getCroppedThumbnail(url: String?, target: ImageView, activity: Activity) {
        Thread {
            val bitmap = getBitmapFromURL(url)
            val finalBitmap = removeBlackBar(bitmap)
            activity.runOnUiThread { target.setImageBitmap(finalBitmap) }
        }.start()
    }

}