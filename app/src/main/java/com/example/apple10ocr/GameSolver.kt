
package com.example.apple10ocr

import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.roundToInt

object GameSolver {
    // Map OCR points to nearest cell; find path (4-neigh) whose sum == 10
    fun solve(board: RectF, cols: Int, rows: Int, cells: List<Cell>): List<PointF> {
        if (cells.isEmpty()) return emptyList()
        val dx = board.width() / cols
        val dy = board.height() / rows

        data class G(val c: Int, val r: Int, val v: Int)
        val mapped = cells.mapNotNull { c ->
            val col = (((c.x - board.left) / dx).toFloat()).roundToInt().coerceIn(0, cols - 1)
            val row = (((c.y - board.top) / dy).toFloat()).roundToInt().coerceIn(0, rows - 1)
            val v = c.text.toIntOrNull() ?: return@mapNotNull null
            if (v in 0..10) G(col, row, v) else null
        }
        val grid = HashMap<Pair<Int,Int>, Int>()
        mapped.forEach { g ->
            val key = g.c to g.r
            val old = grid[key]
            if (old == null || g.v > old) grid[key] = g.v
        }

        val dirs = arrayOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
        fun center(c: Int, r: Int) = PointF(board.left + (c + 0.5f) * dx, board.top + (r + 0.5f) * dy)

        fun dfs(path: MutableList<Pair<Int,Int>>, sum: Int, depth: Int): List<Pair<Int,Int>>? {
            val cur = path.last()
            if (sum == 10 && path.size >= 2) return path.toList()
            if (sum >= 10 || depth >= 6) return null
            for ((dxi, dyi) in dirs) {
                val nx = cur.first + dxi; val ny = cur.second + dyi
                val key = nx to ny
                val v = grid[key] ?: continue
                if (path.contains(key)) continue
                path.add(key)
                val got = dfs(path, sum + v, depth + 1)
                if (got != null) return got
                path.removeAt(path.size - 1)
            }
            return null
        }

        for ((key, v) in grid) {
            val res = dfs(mutableListOf(key), v, 1)
            if (res != null) return res.map { (c, r) -> center(c, r) }
        }
        return emptyList()
    }
}
