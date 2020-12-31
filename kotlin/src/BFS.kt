@file:Suppress("NOTHING_TO_INLINE")

import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.ArrayList

class Point(val x: Int, val y: Int)

private const val DEFAULT_DEPTH: Short = -1
private const val DEFAULT_QUEUE: Short = 0

class BFS(val width: Int, val height: Int) {
    private val walls = BooleanArray(width * height) { false }

    val cachedPoints = Array(width * height) { Point(it % width, it / width) }

    val depth = ShortArray(width * height) { DEFAULT_DEPTH }
    val queue = ShortArray(width * height) { DEFAULT_QUEUE }

    fun generateWalls() {
        for (index in 0 until width * height) {
            walls[index] = false
        }
        for (index in 0 until width) {
            walls[index] = true
            walls[index + (height - 1) * width] = true
        }
        for (index in 0 until height) {
            walls[index * width] = true
            walls[width - 1 + index * width] = true
        }

        val h = height / 10
        val w = width / 10
        for (index in 0 until height - h) {
            val x = 2 * w
            val y = index
            walls[x + y * width] = true
        }
        for (index in h until height) {
            val x = 8 * w
            val y = index
            walls[x + y * width] = true
        }
    }


    fun path(from: Point, to: Point): Array<Point>? {
        // for optimize use index not Point.
        val fromIndex = getIndex(from.x, from.y)
        val toIndex = getIndex(to.x, to.y)
        val offsets = shortArrayOf(1, -1, width.toShort(), (-width).toShort())  //performs better as local variable

        // fill use bfs
        depth.setAll(DEFAULT_DEPTH)
        depth[fromIndex] = 0

        queue.setAll(DEFAULT_QUEUE)
        queue[0] = fromIndex.toShort()
        var queueIter = 0
        var queueEnd = 1

        while (queueIter < queueEnd) {
            val index = queue[queueIter].toInt()
            if (index == toIndex) {
                break
            }
            queueIter += 1

            val nLength = (depth[index] + 1).toShort()
            for (offset in offsets) {
                val nIndex = index + offset
                if (depth[nIndex] >= 0 || walls[nIndex]) {
                    continue
                }
                depth[nIndex] = nLength
                queue[queueEnd] = nIndex.toShort()
                queueEnd += 1
            }
        }

        if (queueIter == queueEnd) { // not found
            return null
        }

        // make path

        val pathLength = depth[toIndex] + 1
        val result = Array(pathLength) { to }
        var resultIndex = pathLength - 2

        var index = toIndex
        while (index != fromIndex) {
            val nLength = (depth[index] - 1).toShort()

            for (offset in offsets) {
                val nIndex = index + offset
                if (depth[nIndex] == nLength) {
                    index = nIndex
                    result[resultIndex--] = cachedPoints[index]
                    break // push first found point.
                }
            }
        }

        return result
    }

    var openNodes = ArrayDeque<Point>()
    var cachedScore = ShortArray(width * height, { Short.MAX_VALUE })

    fun path2(from: Point, to: Point): ArrayList<Point>? {

        openNodes.clear()
        cachedScore.setAll(Short.MAX_VALUE)
        openNodes.add(from)
        cachedScore.set(getIndex(from), 0)

        while (openNodes.isNotEmpty()) {

            val currentNode = openNodes.removeFirst()
            if (currentNode == to) {
                break
            }
            val currentScore: Short = cachedScore[getIndex(currentNode.x, currentNode.y)]
            forEachSide(currentNode) { x, y ->
                val newScore: Short = (currentScore + 1).toShort()
                if (newScore < cachedScore[getIndex(x, y)]) {
                    cachedScore[getIndex(x, y)] = newScore
                    openNodes.add(getPoint(x, y))
                }
            }
        }

        if (cachedScore.get(getIndex(to)) == Short.MAX_VALUE) { // not found
            return null
        }

        // make path

        val result = ArrayList<Point>()
        var curPoint = to
        result.add(to)

        while (curPoint != from) {
            var minScore = Short.MAX_VALUE
            var nextPoint = curPoint

            forEachSide(curPoint) { x, y ->
                val newScore = cachedScore[getIndex(x, y)]
                if (minScore > newScore) {
                    minScore = newScore
                    nextPoint = cachedPoints[getIndex(x, y)]
                }
            }
            result.add(nextPoint)

            curPoint = result.last()
        }

        result.reverse()
        return result
    }

    private fun getIndex(point: Point): Int {
        return getIndex(point.x, point.y)
    }

    private inline fun forEachSide(currentNode: Point, operation: (x: Int, y: Int) -> Unit) {
        val x = currentNode.x
        val y = currentNode.y
        ifFreeCall(x + -1, y + 0, operation)
        ifFreeCall(x + 1, y + 0, operation)
        ifFreeCall(x + 0, y + 1, operation)
        ifFreeCall(x + 0, y + -1, operation)
    }

    private inline fun ifFreeCall(x: Int, y: Int, operation: (x: Int, y: Int) -> Unit) {
        if (x >= width || x < 0) {
            return
        }
        if (y >= width || y < 0) {
            return
        }

        if (walls[getIndex(x, y)]) {
            return
        }

        operation(x, y)
    }


    inline fun getPoint(x: Int, y: Int): Point {
        return cachedPoints[getIndex(x, y)]
    }

    inline fun getIndex(x: Int, y: Int) = x + y * width
}

private inline fun ShortArray.setAll(value: Short) {
    repeat(size) {
        set(it, value)
    }
}
