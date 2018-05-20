package adymova.nsu.grafics.panels.segmentation

import adymova.nsu.grafics.core.Lab
import adymova.nsu.grafics.core.rgbToLab
import java.awt.Color
import java.io.File
import java.lang.Math.*
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis


fun main(args: Array<String>) {

    val file = File("/Users/nastya/lena64.png")
//        val file = File("/Users/nastya/parrot.png")
    val bufferedImage = ImageIO.read(file)
    val millis = measureTimeMillis {
        MeanShiftSegmentation().apply(BufferedImg(bufferedImage), 0.01, 20.0001f)
    }
    println(millis)
    ImageIO.write(bufferedImage, "png", File("/Users/nastya/IdeaProjects/graphics-lab-1/lena64_seg.png"))

}

class MeanShiftSegmentation {
    fun apply(bufferedImage: Image, maxDistance: Double, kernelBandWidth: Float) {
        val original = mutableListOf<MutableList<Point>>()
        val copy = mutableListOf<MutableList<Point>>()
        for (x in 0 until bufferedImage.getWidth()) {
            original.add(mutableListOf())
            copy.add(mutableListOf())
            for (y in 0 until bufferedImage.getHeight()) {
                val lab = rgbToLab(Color(bufferedImage.getRGB(x, y)))
                original[x].add(Point(x.toFloat(), y.toFloat(), lab))
                copy[x].add(Point(x.toFloat(), y.toFloat(), lab))
            }
        }

        for (copyList in copy) {
            for (point in copyList) {
                var iterationNumber = 0
                do {
                    val oldValue = point
                    shift(point,
                        original,
                        { p1: Point, p2: Point -> computeCiede2000Metrics(p1.lab, p2.lab).toFloat() },
                        kernelBandWidth,
                        { distance: Float, bandwidth: Float -> gaussianKernel(distance, bandwidth).toFloat() })
                    iterationNumber++
                    val distance = computeCiede2000Metrics(oldValue.lab, point.lab)
                } while (distance > maxDistance)
//                println("$iterationNumber iterations")
                iterationNumber = 0
            }
        }

        val coordinateToColor = mutableMapOf<Coordinate, Int>()
        var absent = 0

        for ((originalX, row) in copy.withIndex()) {
            for ((originalY, point) in row.withIndex()) {
                val shiftedX = point.x.toInt()
                val shiftedY = point.y.toInt()
                val coordinate = Coordinate(shiftedX, shiftedY)

                var color = coordinateToColor[coordinate]
                if (color == null) {
                    absent++
                    color = coordinateToColor.computeIfAbsent(coordinate, {
                        bufferedImage.getRGB(originalX, originalY)
//                        bufferedImage.getRGB(shiftedX, shiftedY)
                    })
                }
                bufferedImage.setRGB(originalX, originalY, color)
            }
        }
        println("${coordinateToColor.size} colors")
//        PointGrouper().groupPoints(copy, 0.5f, bufferedImage)
    }

    private fun shift(
        currentPoint: Point,
        originalPoints: List<MutableList<Point>>,
        distanceFunc: (Point, Point) -> Float,
        kernelWidth: Float,
        kernel: (Float, Float) -> Float
    ) {
        var shiftX = 0f
        var shiftY = 0f
        var scaleFactor = 0f
        for (row in originalPoints) {
            for (point in row) {
                val distance = distanceFunc(currentPoint, point)
                val weight = kernel(distance, kernelWidth)
                shiftX += point.x * weight
                shiftY += point.y * weight
                scaleFactor += weight
            }
        }
        val newX = shiftX / scaleFactor
        val newY = shiftY / scaleFactor
        currentPoint.x = newX
        currentPoint.y = newY
        currentPoint.lab = originalPoints[newX.toInt()][newY.toInt()].lab
    }

    fun gaussianKernel(distance: Float, kernelWidth: Float): Double {
        return (1 / (kernelWidth * sqrt(2 * PI))) * exp(-0.5 * pow(distance / kernelWidth.toDouble(), 2.0))
    }

}

data class Coordinate(
    val x: Int,
    val y: Int
)

class Point(
    var x: Float,
    var y: Float,
    var lab: Lab
)


fun euclideanDistance(p1: Point, p2: Point): Double {
    return sqrt(pow(p1.x - p1.y.toDouble(), 2.0) + pow(p2.x - p2.y.toDouble(), 2.0))
}


class PointGrouper {
    fun groupPoints(points: List<MutableList<Point>>, groupMaxDistance: Float, bufferedImage: Image) {
        val groups = mutableListOf<MutableList<Point>>()
        var currentGroupIndex = 0
        val groupToColor = mutableListOf<Int>()
        for ((originalX, row) in points.withIndex()) {
            for ((originalY, point) in row.withIndex()) {
                var nearestGroupIndex = determineNearestGroup(point, groups, groupMaxDistance)
                if (nearestGroupIndex == null) {
                    groups.add(mutableListOf(point))
                    nearestGroupIndex = currentGroupIndex
                    currentGroupIndex += 1
                    groupToColor.add(bufferedImage.getRGB(point.x.toInt(), point.y.toInt()))
                } else {
                    groups[nearestGroupIndex].add(point)
                }
                bufferedImage.setRGB(originalX, originalY, groupToColor[nearestGroupIndex])
            }
        }
        println("${groupToColor.size} colors")
    }

    private fun determineNearestGroup(
        point: Point,
        groups: MutableList<MutableList<Point>>,
        groupMaxDistance: Float
    ): Int? {
        var nearestGroupIndex: Int? = null
        var index = 0
        for (group in groups) {
            val distanceToGroup = distanceToGroup(point, group)
            if (distanceToGroup < groupMaxDistance)
                nearestGroupIndex = index
            index += 1
        }
        return nearestGroupIndex
    }

    private fun distanceToGroup(currentPoint: Point, group: MutableList<Point>): Float {
        var minDistance = Float.MAX_VALUE
        for (point in group) {
            val dist = computeCiede2000Metrics(currentPoint.lab, point.lab).toFloat()
            if (dist < minDistance)
                minDistance = dist
        }

        return minDistance
    }


}