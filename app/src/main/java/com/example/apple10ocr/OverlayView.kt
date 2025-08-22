
package com.example.apple10ocr

import android.content.Context
import android.graphics.*
import android.view.View

class OverlayView(context: Context) : View(context) {
    private val paintGrid = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        alpha = 160
    }
    private val paintPath = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val paintCorner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        style = Paint.Style.FILL
    }
    private val paintInfo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
    }

    private var board = RectF(200f, 400f, 900f, 1800f)
    private val corners = arrayOf(
        PointF(board.left, board.top),
        PointF(board.right, board.top),
        PointF(board.right, board.bottom),
        PointF(board.left, board.bottom)
    )

    private var pathPoints: List<PointF> = emptyList()

    fun updateFrame(w: Int, h: Int, cells: List<Cell>) {
        val path = GameSolver.solve(board, 9, 18, cells)
        pathPoints = path
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(0x55000000)
        val rect = RectF(
            minOf(corners[0].x, corners[3].x, corners[1].x, corners[2].x),
            minOf(corners[0].y, corners[1].y, corners[2].y, corners[3].y),
            maxOf(corners[0].x, corners[1].x, corners[2].x, corners[3].x),
            maxOf(corners[0].y, corners[1].y, corners[2].y, corners[3].y)
        )
        board = rect
        canvas.drawRect(board, paintGrid)

        val cols = 9; val rows = 18
        val dx = board.width() / cols
        val dy = board.height() / rows
        for (i in 1 until cols) {
            val x = board.left + i * dx
            canvas.drawLine(x, board.top, x, board.bottom, paintGrid)
        }
        for (j in 1 until rows) {
            val y = board.top + j * dy
            canvas.drawLine(board.left, y, board.right, y, paintGrid)
        }

        if (pathPoints.isNotEmpty()) {
            val p = Path()
            p.moveTo(pathPoints[0].x, pathPoints[0].y)
            for (k in 1 until pathPoints.size) p.lineTo(pathPoints[k].x, pathPoints[k].y)
            canvas.drawPath(p, paintPath)
        }

        for (c in corners) canvas.drawCircle(c.x, c.y, 16f, paintCorner)
        canvas.drawText("9x18 보드에 모서리를 맞추세요.", board.left, board.top - 24f, paintInfo)
    }
}
