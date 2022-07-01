package com.nauval.storyapp.helper

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import com.nauval.storyapp.R
import java.io.*
import java.net.URL
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object Utils {
    fun makeTempFile(context: Context): File {
        val timeStamp = SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(System.currentTimeMillis())
        return File.createTempFile(
            timeStamp, ".jpg",
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
    }

    fun convertUriToFile(selectedImg: Uri, ctx: Context): File {
        val resolver = ctx.contentResolver
        val file = makeTempFile(ctx)

        val inputStream = resolver.openInputStream(selectedImg) as InputStream
        val outputStream: OutputStream = FileOutputStream(file)

        val buffer = ByteArray(1024)
        var length: Int

        while (inputStream.read(buffer).also { length = it } > 0)
            outputStream.write(buffer, 0, length)

        outputStream.close()
        inputStream.close()

        return file
    }

    fun transferBitmapToFile(bitmap: Bitmap, file: File): File {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
        return file
    }

    fun stabilizeRotateBitmap(bitmap: Bitmap, isBackCamera: Boolean = false): Bitmap {
        val matrix = Matrix()
        return if (isBackCamera) {
            matrix.postRotate(90f)
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width ,bitmap.height, matrix,  true)
        } else {
            matrix.postRotate(-90f)
            matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    }

    class CompressImageTask(private var file: File, private val listener: ICompressListener): Runnable {
        private val handler = Handler(Looper.getMainLooper())

        override fun run() {
            val bitmap = BitmapFactory.decodeFile(file.path)
            var bitmapStream: ByteArrayOutputStream
            var compressQuality = 100
            var streamLength: Int

            do {
                bitmapStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bitmapStream)
                streamLength = bitmapStream.toByteArray().size
                compressQuality -= 5
            } while (streamLength > 1000000)

            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))

            handler.post { listener.onComplete(file) }
        }

        interface ICompressListener {
            fun onComplete(compressedFile: File)
        }
    }

    fun convertURLToBitmap(sourceUrl: String): Bitmap? {
        return try {
            val inputStream = URL(sourceUrl).openConnection().also { it.connect() }.getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            return Bitmap.createScaledBitmap(bitmap, 300, 200, true)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun String.formatDate(): String {
        return DateTimeFormatter.ofPattern("dd MMM yyyy | HH:mm")
            .withZone(ZoneId.of(TimeZone.getDefault().id)).format(Instant.parse(this))
    }

    fun setMapTheme(ctx: Context, map: GoogleMap) {
        try {
            val isSuccess = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(ctx, R.raw.map_style))
            if (!isSuccess) Toast.makeText(ctx, ctx.resources.getString(R.string.style_failure), Toast.LENGTH_SHORT).show()
        } catch (exception: Resources.NotFoundException) {
            Toast.makeText(ctx, ctx.resources.getString(R.string.style_failure), Toast.LENGTH_SHORT).show()
        }
    }

    fun getCircularProgressDrawable(context: Context): CircularProgressDrawable {
        val temp = CircularProgressDrawable(context).apply {
            strokeWidth = 5f
            centerRadius = 25f
        }
        temp.start()
        return temp
    }

    fun readFileToString(fileName: String): String {
        try {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val builder = StringBuilder()
            InputStreamReader(context.assets.open(fileName), "UTF-8")
                .readLines().forEach {
                    builder.append(it)
                }
            return builder.toString()
        } catch (e: IOException) {
            throw e
        }
    }
}