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
                    Toast.makeText(context, "File hidden successfully", Toast.LENGTH_SHORT).show()

                } catch (_: Exception) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Error hiding content", Toast.LENGTH_SHORT).show()
                    }
                }
            }catch (e: Exception){
                Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
            }
        }else {
            if ((imageUri == null) && (fileUri == null))
                Toast.makeText(context, "Please select a file and an image", Toast.LENGTH_SHORT).show()
            if (imageUri != null)
                Toast.makeText(context, "Please select a file", Toast.LENGTH_SHORT).show()
            if (fileUri != null)
                Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "An error occurred while extracting file", Toast.LENGTH_SHORT).show()
        }

    }

    fun detectFileFormat(data: ByteArray): String? {
        // Helper to check magic at offset 0
        fun ByteArray.startsWith(prefix: ByteArray, offset: Int = 0): Boolean {
            if (this.size < offset + prefix.size) return false
            return this.sliceArray(offset until (offset + prefix.size)).contentEquals(prefix)
        }

        return when {
            // JPEG: FF D8 FF
            data.startsWith(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())) ->
                ".jpg"

            // PNG: 89 50 4E 47 0D 0A 1A 0A
            data.startsWith(byteArrayOf(
                0x89.toByte(), 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A
            )) ->
                ".png"

            // GIF87a / GIF89a
            data.startsWith("GIF87a".toByteArray()) ||
                    data.startsWith("GIF89a".toByteArray()) ->
                ".gif"

            // PDF: 25 50 44 46 ("%PDF")
            data.startsWith(byteArrayOf(0x25.toByte(), 0x50, 0x44, 0x46)) ->
                ".pdf"

            // ZIP (incl. docx, xlsx, jar, apk…)
            data.startsWith(byteArrayOf(0x50, 0x4B, 0x03, 0x04)) ->
                ".zip"

            // MP3, ID3v2 tag: "ID3"
            data.startsWith("ID3".toByteArray()) ->
                ".mp3"

            // MP3, MPEG Audio Frame: FF Ex where E = 0xE0 bits set
            data.size >= 2 &&
                    (data[0].toInt() and 0xFF) == 0xFF &&
                    (data[1].toInt() and 0xE0) == 0xE0 ->
                ".mp3"

            // WAV: RIFF …. WAVE
            data.startsWith("RIFF".toByteArray()) &&
                    data.size >= 12 &&
                    data.sliceArray(8 until 12).contentEquals("WAVE".toByteArray()) ->
                ".wav"

            // MPEG (Program stream) .mpg/.mpeg : 00 00 01 BA or 00 00 01 B3
            data.startsWith(byteArrayOf(0x00, 0x00, 0x01, 0xBA.toByte())) ||
                    data.startsWith(byteArrayOf(0x00, 0x00, 0x01, 0xB3.toByte())) ->
                ".mpg"

            // MP4 / MOV / 3GP family: ftyp at offset 4, then brand (isom, mp42, etc.)
            data.size >= 12 &&
                    data.startsWith(byteArrayOf(0x00, 0x00, 0x00), offset = 0) &&
                    data.startsWith("ftyp".toByteArray(), offset = 4) -> {
                // check brand
                val brand = data.sliceArray(8 until 12).toString(Charsets.US_ASCII)
                when (brand) {
                    "mp42", "isom", "M4A ", "M4V " -> ".mp4"
                    "qt  " -> ".mov"
                    "3gp4", "3gp5"         -> ".3gp"
                    else                   -> ".mp4"
                }
            }

            // AVI: RIFF …. AVI
            data.startsWith("RIFF".toByteArray()) &&
                    data.size >= 12 &&
                    data.sliceArray(8 until 12).contentEquals("AVI ".toByteArray()) ->
                ".avi"

            // MKV/WebM: EBML header 1A 45 DF A3
            data.startsWith(byteArrayOf(0x1A, 0x45.toByte(), 0xDF.toByte(), 0xA3.toByte())) ->
                ".mkv"

            // FLV: "FLV"  46 4C 56 01
            data.startsWith("FLV".toByteArray()) ->
                ".flv"

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
                Toast.makeText(context, "Error: Unable to save file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun hideText(context: Context, imageUri: Uri?, text: String?, key: String?) {
        if (imageUri != null && !text.isNullOrEmpty()) {
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
                Toast.makeText(context, "An error occurred white hiding text", Toast.LENGTH_SHORT).show()
            }
        } else {
            if ((imageUri == null && text.isNullOrEmpty()))
                Toast.makeText(context, "Please select an image and provide text", Toast.LENGTH_SHORT).show()
            if (imageUri == null)
                Toast.makeText(context, "Please enter a text to hide", Toast.LENGTH_SHORT).show()
            if (text.isNullOrEmpty())
                Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
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

                Toast.makeText(context, "Text extracted successfully", Toast.LENGTH_SHORT).show()
                return String(finalBytes, Charsets.UTF_8)

            } catch (e: Exception) {
                Toast.makeText(context, "An error occurred while hiding text", Toast.LENGTH_SHORT).show()
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
