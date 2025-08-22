
package com.example.apple10ocr

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.TimeUnit

data class Cell(val x: Float, val y: Float, val text: String)

class OCRProcessor(ctx: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun process(bitmap: Bitmap): List<Cell> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val task = recognizer.process(image)
        val result = Tasks.await(task, 1500, TimeUnit.MILLISECONDS)
        return toCells(result)
    }

    private fun toCells(t: Text): List<Cell> {
        val out = mutableListOf<Cell>();
        for (b in t.textBlocks) for (l in b.lines) for (e in l.elements) {
            val digits = e.text.filter { it.isDigit() }
            if (digits.isNotEmpty()) {
                val box = e.boundingBox ?: continue
                out.add(Cell(box.centerX().toFloat(), box.centerY().toFloat(), digits))
            }
        }
        return out
    }
}
