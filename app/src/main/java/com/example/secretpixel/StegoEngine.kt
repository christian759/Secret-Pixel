package com.example.secretpixel

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


object StegoEngine {

    fun hideFileInImage(context: Context, imageUri: Uri, fileUri: Uri): File {
        val imageStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(imageStream)!!

        val fileStream = context.contentResolver.openInputStream(fileUri)
        val fileData = fileStream!!.readBytes()
        val fileSizeBytes = ByteBuffer.allocate(8).putLong(fileData.size.toLong()).array()

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val imageData = byteArrayOutputStream.toByteArray()

        val combinedData = imageData + fileData + fileSizeBytes

        val outputFile = File(context.cacheDir, "stego_output_${System.currentTimeMillis()}.png")
        outputFile.writeBytes(combinedData)

        return outputFile
    }

    fun extractFileFromImage(context: Context, imageUri: Uri): File {
        val imageData = context.contentResolver.openInputStream(imageUri)!!.readBytes()

        val fileSizeBytes = imageData.copyOfRange(imageData.size - 8, imageData.size)
        val fileSize = ByteBuffer.wrap(fileSizeBytes).long

        if (fileSize <= 0 || fileSize > imageData.size - 8) {
            throw IllegalArgumentException("Invalid embedded file size.")
        }

        val fileData = imageData.copyOfRange(imageData.size - 8 - fileSize.toInt(), imageData.size - 8)

        val outputFile = File(context.cacheDir, "extracted_file_${System.currentTimeMillis()}")
        outputFile.writeBytes(fileData)

        return outputFile
    }
}
