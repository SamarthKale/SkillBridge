package com.skillbridge.app.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.core.content.FileProvider
import com.skillbridge.app.data.ResumeFile
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Cloud Storage needs the paid Blaze plan, so small files live inside Firestore as base64:
 *  - profile photo: ~256px JPEG, stored in `profiles/{uid}.photoUrl` as a `data:image/jpeg;base64,` URI
 *  - resume: a PDF/Word file up to [MAX_RESUME_BYTES], stored in `resumes/{uid}`
 */
object FileUtils {

    const val MAX_RESUME_BYTES = 500 * 1024
    private const val PHOTO_MAX_PX = 256
    private const val DATA_URI_PREFIX = "data:image/jpeg;base64,"

    val RESUME_MIME_TYPES = arrayOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )

    // ---- Photo -----------------------------------------------------------

    /** Reads an image the user picked, shrinks it to [PHOTO_MAX_PX], returns a data URI (or null). */
    fun encodePhoto(context: Context, uri: Uri): String? = try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= PHOTO_MAX_PX && bounds.outHeight / (sample * 2) >= PHOTO_MAX_PX) sample *= 2
        val decoded = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        }
        decoded?.let { bmp ->
            // Centre-crop to a square so it fills the round avatar.
            val side = minOf(bmp.width, bmp.height)
            val square = Bitmap.createBitmap(bmp, (bmp.width - side) / 2, (bmp.height - side) / 2, side, side)
            val out = Bitmap.createScaledBitmap(square, minOf(side, PHOTO_MAX_PX), minOf(side, PHOTO_MAX_PX), true)
            val bytes = ByteArrayOutputStream().also { out.compress(Bitmap.CompressFormat.JPEG, 80, it) }.toByteArray()
            DATA_URI_PREFIX + Base64.encodeToString(bytes, Base64.NO_WRAP)
        }
    } catch (_: Exception) {
        null
    }

    /** A fresh cache file URI for the camera app to write a full-size photo into. */
    fun newCameraUri(context: Context): Uri {
        val dir = File(context.cacheDir, "photos").apply { mkdirs() }
        val file = File(dir, "camera.jpg").apply { delete() }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /** Decodes a `data:image/...;base64,` photo value; null for blank values or plain URLs. */
    fun decodePhoto(photo: String): Bitmap? {
        if (!photo.startsWith("data:image")) return null
        return try {
            val bytes = Base64.decode(photo.substringAfter("base64,"), Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }

    // ---- Resume ----------------------------------------------------------

    sealed class ResumeRead {
        data class Ok(val file: ResumeFile) : ResumeRead()
        data class Error(val message: String) : ResumeRead()
    }

    /** Reads a picked document into a [ResumeFile], rejecting anything over [MAX_RESUME_BYTES]. */
    fun readResume(context: Context, uri: Uri, uid: String): ResumeRead {
        return try {
            val resolver = context.contentResolver
            val mime = resolver.getType(uri).orEmpty()
            var name = "resume"
            resolver.query(uri, null, null, null, null)?.use { c ->
                val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (i >= 0 && c.moveToFirst()) name = c.getString(i) ?: name
            }
            val bytes = resolver.openInputStream(uri)?.use { input ->
                val buf = ByteArrayOutputStream()
                val chunk = ByteArray(16 * 1024)
                while (true) {
                    val n = input.read(chunk)
                    if (n < 0) break
                    buf.write(chunk, 0, n)
                    if (buf.size() > MAX_RESUME_BYTES) return ResumeRead.Error("File is too large. Maximum size is ${MAX_RESUME_BYTES / 1024} KB.")
                }
                buf.toByteArray()
            } ?: return ResumeRead.Error("Could not read that file.")
            ResumeRead.Ok(
                ResumeFile(
                    uid = uid, fileName = name, mimeType = mime.ifBlank { "application/pdf" },
                    sizeBytes = bytes.size.toLong(),
                    dataBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
                    updatedAt = System.currentTimeMillis()
                )
            )
        } catch (_: Exception) {
            ResumeRead.Error("Could not read that file.")
        }
    }

    /** Writes the resume to the app cache and hands it to whatever app can view it. */
    fun openResume(context: Context, r: ResumeFile): Boolean {
        return try {
            val dir = File(context.cacheDir, "resumes").apply { mkdirs() }
            val safeName = r.fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
            val file = File(dir, safeName)
            file.writeBytes(Base64.decode(r.dataBase64, Base64.DEFAULT))
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            context.startActivity(
                Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, r.mimeType)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    fun formatSize(bytes: Long): String =
        if (bytes >= 1024 * 1024) "%.1f MB".format(bytes / 1024.0 / 1024.0) else "${maxOf(1L, bytes / 1024)} KB"
}
