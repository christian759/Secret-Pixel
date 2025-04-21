package com.example.secretpixel

import android.content.*
import android.graphics.*
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.widget.Toast
import java.io.*
import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


object StegoEngine {
    fun hideFile(context: Context, imageUri: Uri, fileUri: Uri, outputName: String, encrypt: Boolean, key: String) {
        // reading the image
        val imageInputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val bitmap: Bitmap? = BitmapFactory.decodeStream(imageInputStream)

        // reading the file
        val fileInputStream: InputStream? = context.contentResolver.openInputStream(fileUri)
        var fileData: ByteArray? = fileInputStream?.readBytes()

        // encryption
        if (encrypt && fileData != null) {
            fileData = encryptData(fileData, key)
        }

        val fileSizeBytes = ByteBuffer.allocate(8).putLong(fileData?.size?.toLong() ?: 0).array()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val imageData = byteArrayOutputStream.toByteArray()

        val combinedData = ByteArray(imageData.size + (fileData?.size ?: 0) + 8)
        System.arraycopy(imageData, 0, combinedData, 0, imageData.size)
        if (fileData != null) {
            System.arraycopy(fileData, 0, combinedData, imageData.size, fileData.size)
            System.arraycopy(fileSizeBytes, 0, combinedData, imageData.size + fileData.size, 8)
        }

        // saving the image
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$outputName.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val savedImageUri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        try {
            savedImageUri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(combinedData)
                }
            }
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Error hiding content", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // AES encryption with proper key length
    fun encryptData(data: ByteArray, key: String): ByteArray {
        val key = key.toByteArray(Charsets.UTF_8) // 16-byte key
        val secretKeySpec = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        return cipher.doFinal(data)
    }


    fun extractFile(context: Context, imageUri: Uri, key: String) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val combinedData = inputStream?.readBytes()

            if (combinedData == null || combinedData.size < 8) {
                Toast.makeText(context, "Corrupted image: No hidden data found", Toast.LENGTH_SHORT).show()
                return
            }

            val fileSizeBytes = combinedData.takeLast(8).toByteArray()
            val fileSize = ByteBuffer.wrap(fileSizeBytes).long

            if (fileSize <= 0 || fileSize > combinedData.size - 8) {
                Toast.makeText(context, "Image doesn't contain any content", Toast.LENGTH_SHORT).show()
                return
            }

            val extractedData = combinedData.copyOfRange(combinedData.size - 8 - fileSize.toInt(), combinedData.size - 8)

            val finalData = try{ decryptData(extractedData, key) }catch(_: Exception){ extractedData }

            val detectedFormat = detectFileFormat(finalData) ?: ".bin"
            saveHiddenFile(context, finalData, detectedFormat)

        } catch (e: Exception) {
            Toast.makeText(context, "Error extracting content: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun detectFileFormat(data: ByteArray): String? {
        fun ByteArray.startsWith(prefix: ByteArray): Boolean {
            if (this.size < prefix.size) return false
            return this.sliceArray(0 until prefix.size).contentEquals(prefix)
        }

        return when {
            data.startsWith(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())) -> ".jpg"
            data.startsWith(byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte())) -> ".png"
            data.startsWith(byteArrayOf(0x25.toByte(), 0x50.toByte(), 0x44.toByte(), 0x46.toByte())) -> ".pdf"
            data.startsWith(byteArrayOf(0x50.toByte(), 0x4B.toByte(), 0x03.toByte(), 0x04.toByte())) -> ".zip"
            data.startsWith(byteArrayOf(0x49, 0x44, 0x33)) -> ".mp3" // MP3
            data.startsWith(byteArrayOf(0x52, 0x49, 0x46, 0x46)) -> ".wav" // WAV
            data.startsWith(byteArrayOf(0x00, 0x00, 0x01, 0xBA.toByte())) -> ".mpg" // MPEG Video
            data.startsWith(byteArrayOf(0x00, 0x00, 0x01, 0xB3.toByte())) -> ".mpg"
            data.startsWith(byteArrayOf(0x66, 0x74, 0x79, 0x70)) -> ".mp4" // MP4
            data.startsWith(byteArrayOf(0x52, 0x49, 0x46, 0x46, 0xE2.toByte(), 0x28, 0xD3.toByte(), 0x11)) -> ".avi" // AVI
            else -> null
        }
    }


    fun saveHiddenFile(context: Context, fileData: ByteArray, format: String) {
        try {
            val fileName = "hidden_file_${System.currentTimeMillis()}$format"

            val values = ContentValues().apply {
                put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
                put(MediaStore.Files.FileColumns.MIME_TYPE, format)
                put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            }

            val fileUri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
            fileUri?.let {
                context.contentResolver.openOutputStream(it)?.use { out ->
                    out.write(fileData)
                }
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "File extracted successfully!", Toast.LENGTH_SHORT).show()
                }
            } ?: Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun decryptData(data: ByteArray, key: String): ByteArray {
        val key = key.toByteArray(Charsets.UTF_8) // Same 16-byte key
        val secretKeySpec = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        return cipher.doFinal(data)
    }

}
