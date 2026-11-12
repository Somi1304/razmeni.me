package me.razmeni.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.razmeni.CLOUDINARY_CLOUD_NAME
import me.razmeni.CLOUDINARY_UPLOAD_PRESET
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

object ImageUploader {
    private const val MAX_SIZE = 800
    private val client = OkHttpClient()

    suspend fun upload(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val bytes = ByteArrayOutputStream().use { out ->
            scaleDown(bitmap).compress(Bitmap.CompressFormat.JPEG, 80, out)
            out.toByteArray()
        }
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "photo.jpg", bytes.toRequestBody("image/jpeg".toMediaType()))
            .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
            .build()
        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Cloudinary je vratio ${response.code}")
            JSONObject(response.body?.string().orEmpty()).getString("secure_url")
        }
    }

    fun decode(context: Context, uri: Uri): Bitmap? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val longer = maxOf(info.size.width, info.size.height)
                if (longer > MAX_SIZE) {
                    val ratio = MAX_SIZE.toFloat() / longer
                    decoder.setTargetSize((info.size.width * ratio).toInt(), (info.size.height * ratio).toInt())
                }
            }
        } else {
            context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) }
        }
    } catch (e: Exception) {
        Log.e("ImageUploader", "decode", e)
        null
    }

    private fun scaleDown(bitmap: Bitmap): Bitmap {
        val longer = maxOf(bitmap.width, bitmap.height)
        if (longer <= MAX_SIZE) return bitmap
        val ratio = MAX_SIZE.toFloat() / longer
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt(),
            (bitmap.height * ratio).toInt(),
            true
        )
    }
}
