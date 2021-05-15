package com.prog.communityaid.utils

import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Filesystem {

    companion object {
        @Throws(IOException::class)
        fun createImageFile(app: AppCompatActivity): File {
            // Create an image file name
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir: File = app.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

            return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            ).apply {
                // Save a file: path for use with ACTION_VIEW intents

            }
        }

        @Throws(IOException::class)
        fun createVideoFile(app: AppCompatActivity): File {
            // Create an image file name
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir: File = app.getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!
            return File.createTempFile(
                "MP4_${timeStamp}_", /* prefix */
                ".mp4", /* suffix */
                storageDir /* directory */
            ).apply {
                // Save a file: path for use with ACTION_VIEW intents
            }
        }
    }

}