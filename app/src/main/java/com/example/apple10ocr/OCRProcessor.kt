package com.example.apple10ocr

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
// [수정] import 변경
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import java.util.concurrent.TimeUnit

data class Cell(val x: Float, val y: Float, val text: String)

class OCRProcessor(ctx: Context) {
    // [수정] 한국어 인식기로 명시적으로 초기화
    private val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

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
