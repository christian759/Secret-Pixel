package com.example.secretpixel

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object StegoEngine {

    fun hideFile(context: Context, imageUri: Uri?, fileUri: Uri?, key: String?) {
        if ((imageUri != null) && (fileUri != null)){
            try {
                // reading the image
                val imageInputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                val bitmap: Bitmap? = BitmapFactory.decodeStream(imageInputStream)

                // reading the file
                val fileInputStream: InputStream? = context.contentResolver.openInputStream(fileUri)
                var fileData: ByteArray? = fileInputStream?.readBytes()

                // encryption
                if ((key != null) && (fileData != null)) {
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
                    put(MediaStore.Images.Media.DISPLAY_NAME, "love.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val savedImageUri: Uri? =
                    context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                try {
                    savedImageUri?.let {
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write(combinedData)
                        }
                    }
                } catch (_: Exception) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Error hiding content", Toast.LENGTH_SHORT).show()
                    }
                }
            }catch (e: Exception){
                Toast.makeText(context, "Error ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(context, "Please select a file and an image", Toast.LENGTH_SHORT).show()
        }
    }

    // AES encryption with proper key length
    fun encryptData(data: ByteArray, key: String?): ByteArray {
        if (key != null) {
            val key = key.toByteArray(Charsets.UTF_16).copyOf(16) // 16-byte key
            val secretKeySpec = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            return cipher.doFinal(data)
        }
        return data
    }


    fun extractFile(context: Context, imageUri: Uri?, key: String?) {
        try {
            if (imageUri != null) {
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

                val extractedData =
                    combinedData.copyOfRange(combinedData.size - 8 - fileSize.toInt(), combinedData.size - 8)

                val finalData = try {
                    decryptData(extractedData, key)
                } catch (_: Exception) {
                    extractedData
                }

                val detectedFormat = detectFileFormat(finalData) ?: ".bin"
                saveHiddenFile(context, finalData, detectedFormat)
            }else{
                Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
            }
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

    fun hideText(context: Context, imageUri: Uri?, text: String?, key: String?) {
        if (imageUri != null && text != null) {
            try {
                // Read the image
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val mutableBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, true)

                if (mutableBitmap == null) {
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                    return
                }

                // If key is provided, encrypt text
                val textBytes = if (!key.isNullOrEmpty()) {
                    encryptData(text.toByteArray(Charsets.UTF_8), key)
                } else {
                    text.toByteArray(Charsets.UTF_8)
                }

                val textBits = mutableListOf<Int>()

                // 32 bits = length of data
                val length = textBytes.size
                val lengthBits = Integer.toBinaryString(length).padStart(32, '0')
                for (char in lengthBits) textBits.add(char.toString().toInt())

                // Then text bits
                for (byte in textBytes) {
                    val bits = Integer.toBinaryString(byte.toInt() and 0xFF).padStart(8, '0')
                    for (bit in bits) textBits.add(bit.toString().toInt())
                }

                var bitIndex = 0
                outer@ for (y in 0 until mutableBitmap.height) {
                    for (x in 0 until mutableBitmap.width) {
                        if (bitIndex >= textBits.size) break@outer

                        val pixel = mutableBitmap.getPixel(x, y)
                        val r = (pixel shr 16) and 0xFF
                        val g = (pixel shr 8) and 0xFF
                        val b = pixel and 0xFF

                        val newR = if (bitIndex < textBits.size) (r and 0xFE) or textBits[bitIndex++] else r
                        val newG = if (bitIndex < textBits.size) (g and 0xFE) or textBits[bitIndex++] else g
                        val newB = if (bitIndex < textBits.size) (b and 0xFE) or textBits[bitIndex++] else b

                        val newPixel = (0xFF shl 24) or (newR shl 16) or (newG shl 8) or newB
                        mutableBitmap.setPixel(x, y, newPixel)
                    }
                }

                // Save the new image
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "hidden_text.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val savedImageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                savedImageUri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        mutableBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                }

                Toast.makeText(context, "Text hidden successfully", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Please select an image and provide text", Toast.LENGTH_SHORT).show()
        }
    }


    fun extractText(context: Context, imageUri: Uri?, key: String?): String? {
        if (imageUri != null) {
            try {
                // Read the image
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                if (bitmap == null) {
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                    return null
                }

                val bits = mutableListOf<Int>()
                outer@ for (y in 0 until bitmap.height) {
                    for (x in 0 until bitmap.width) {
                        val pixel = bitmap.getPixel(x, y)

                        val r = (pixel shr 16) and 0xFF
                        val g = (pixel shr 8) and 0xFF
                        val b = pixel and 0xFF

                        bits.add(r and 1)
                        bits.add(g and 1)
                        bits.add(b and 1)

                        // Stop early if we got enough bits (we'll check length later)
                        if (bits.size >= 32) {
                            val lengthBits = bits.take(32).joinToString("")
                            val length = Integer.parseInt(lengthBits, 2)
                            if (bits.size >= (32 + length * 8)) break@outer
                        }
                    }
                }

                if (bits.size < 32) {
                    Toast.makeText(context, "No hidden text found", Toast.LENGTH_SHORT).show()
                    return null
                }

                val lengthBits = bits.take(32).joinToString("")
                val textLength = Integer.parseInt(lengthBits, 2)

                val textBits = bits.drop(32).take(textLength * 8)

                val bytes = mutableListOf<Byte>()
                for (i in textBits.indices step 8) {
                    val byteBits = textBits.subList(i, i + 8).joinToString("")
                    bytes.add(Integer.parseInt(byteBits, 2).toByte())
                }

                val textBytes = bytes.toByteArray()

                // Decrypt if needed
                val finalBytes = if (!key.isNullOrEmpty()) {
                    decryptData(textBytes, key)
                } else {
                    textBytes
                }

                return String(finalBytes, Charsets.UTF_8)

            } catch (e: Exception) {
                Toast.makeText(context, "Error extracting text: ${e.message}", Toast.LENGTH_SHORT).show()
                return null
            }
        } else {
            Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
            return null
        }
    }



    fun decryptData(data: ByteArray, key: String?): ByteArray {
        if (key != null) {
            val key = key.toByteArray(Charsets.UTF_16).copyOf(16) // Same 16-byte key
            val secretKeySpec = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            return cipher.doFinal(data)
        }
        return data
    }
}
