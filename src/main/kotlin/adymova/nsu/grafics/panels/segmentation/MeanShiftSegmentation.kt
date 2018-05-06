package adymova.nsu.grafics.panels.segmentation

import adymova.nsu.grafics.core.Lab
import adymova.nsu.grafics.core.rgbToLab
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.system.measureTimeMillis


fun main(args: Array<String>) {

    val file = File("/Users/nastya/lena64.png")
    val bufferedImage = ImageIO.read(file)
    val millis = measureTimeMillis {
        MeanShiftSegmentation().apply(BufferedImg(bufferedImage), 0.5, 4.0f)
    }
    println(millis)
    ImageIO.write(bufferedImage, "png", File("/Users/nastya/IdeaProjects/graphics-lab-1/lena64_seg.png"))

}
//todo не сходится - надо разобраться со смещением

class MeanShiftSegmentation {
    fun apply(bufferedImage: Image, maxDistance: Double, kernelWidth: Float) {
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

        for (mutableList in copy) {
            for (point in mutableList) {
                var shift: Shift
                var i = 0
                do {
                    shift = shift(point, original, { p1: Point, p2: Point -> computeCiede2000Metrics(p1.lab, p2.lab).toFloat() }, kernelWidth,
                            { distance: Float, bandwidth: Float -> kernelEpanch(distance, bandwidth) })
                    i++
                    point.lab = original[point.x.toInt()][point.y.toInt()].lab
                } while (shift.xShift > maxDistance || shift.yShift > maxDistance)
                println("$i iterations")
                i = 0
            }
        }

        val coordinateToColor = mutableMapOf<Coordinate, Int>()
        var absent = 0

        for ((originalX, row) in copy.withIndex()) {
            for ((originalY, point) in row.withIndex()) {
                val shiftedX = point.x.toInt()
                val shiftedY = point.y.toInt()
                val coordinate = Coordinate(shiftedX, shiftedY)

                var color = coordinateToColor.get(coordinate)
                if (color == null) {
                    absent++
                    color = coordinateToColor.computeIfAbsent(coordinate, {
                        bufferedImage.getRGB(shiftedX, shiftedY)
                    })
                }
//                val color = coordinateToColor.computeIfAbsent(coordinate, {
//                    bufferedImage.getRGB(shiftedX, shiftedY)
//                })
                bufferedImage.setRGB(originalX, originalY, color)
            }
        }
        println("${coordinateToColor.size} colors")
        println("${absent} absent")
    }

    fun shift(currentPoint: Point,
              originalPoints: List<MutableList<Point>>,
              distanceFunc: (Point, Point) -> Float,
              kernelWidth: Float,
              kernel: (Float, Float) -> Float
    ): Shift {
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
        val shift = Shift(abs(currentPoint.x - newX), abs(currentPoint.y - newY))
        currentPoint.x = newX
        currentPoint.y = newY
        return shift
    }

    fun kernelEpanch(distance: Float, kernelWidth: Float): Float {
        if (distance >= kernelWidth) return 0f
        return 1 - (distance / kernelWidth) * (distance / kernelWidth)
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

class Shift(
        var xShift: Float,
        var yShift: Float
)