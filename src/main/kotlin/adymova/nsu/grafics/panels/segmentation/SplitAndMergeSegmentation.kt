package adymova.nsu.grafics.panels.segmentation

import adymova.nsu.grafics.core.Lab
import adymova.nsu.grafics.core.rgbToLab
import java.awt.Color
import java.awt.image.BufferedImage

class SplitAndMergeSegmentation {

    fun apply(bufferedImage: Image, maxDifference: Double, metrics: (Lab, Lab) -> Double) {
        val imageAsLabArrays = ImageAsLabArrays(bufferedImage)

        val width = bufferedImage.getWidth()
        val height = bufferedImage.getHeight()
        val regionsMap = IntArray(width * height)
        var currentRegionId = 1

        val root = Node(0, 0, width, height, imageAsLabArrays, null)

        root.pass { it.splitIfPossible(maxDifference, metrics) }
        root.leafPass {
            fillRegionsMap(it, regionsMap, width, currentRegionId)
            currentRegionId++
        }
        println()

    }

    private fun fillRegionsMap(it: Node, regionsMap: IntArray, width: Int, currentRegionId: Int) {
        for (x in it.xStart until it.xEnd) {
            for (y in it.yEnd until it.yEnd) {
                regionsMap[y * width + y] = currentRegionId
            }
        }
    }
}


class Node(
        val xStart: Int,
        private val yStart: Int,
        val xEnd: Int,
        val yEnd: Int,
        val imageAsLabArrays: ImageAsLabArrays,
        val parent: Node?
) {
    var passedByMergeAlgorithm = false
    var childArray: Array<Node> = emptyArray()


    fun splitIfPossible(maxDifference: Double, metrics: (Lab, Lab) -> Double) {
        if (!isHomogeneous(maxDifference, metrics) && xEnd != xStart - 1 || yEnd == yStart - 1) {
            childArray = split()
        }

    }

    private fun split(): Array<Node> {
        val xMid = (xStart + xEnd) / 2
        val yMid = (yStart + yEnd) / 2
        return arrayOf(
                Node(xStart, yStart, xMid, yMid, imageAsLabArrays, this), // left top
                Node(xMid, yStart, xEnd, yMid, imageAsLabArrays, this), // right top
                Node(xStart, yMid, xMid, yEnd, imageAsLabArrays, this), // left down
                Node(xMid, yMid, xEnd, yEnd, imageAsLabArrays, this) // right down
        )
    }

    private fun isHomogeneous(maxDifference: Double, metrics: (Lab, Lab) -> Double): Boolean {
        for (x1 in xStart until xEnd) {
            for (y1 in yStart until yEnd) {
                for (x2 in xStart until xEnd) {
                    for (y2 in yStart until yEnd) {

                        val lab1 = imageAsLabArrays.getLabByXY(x1, y1)
                        val lab2 = imageAsLabArrays.getLabByXY(x2, y2)
                        if (metrics(lab1, lab2) >= maxDifference) {
                            return false
                        }
                    }
                }
            }
        }
        return true
    }

    fun pass(action: (Node) -> Unit) {
        action(this)
        for (child in childArray) {
            child.pass(action)
        }
    }

    fun leafPass(action: (Node) -> Unit) {
        pass {
            if (it.isLeaf()) {
                action(it)
            }
        }
    }

    private fun isLeaf(): Boolean {
        return childArray.isEmpty()
    }

}


class ImageAsLabArrays(bufferedImage: Image) {
    private val width = bufferedImage.getWidth()
    private val size = bufferedImage.getHeight() * width

    private val lValues = FloatArray(size)
    private val aValues = FloatArray(size)
    private val bValues = FloatArray(size)

    init {
        for (y in 0 until bufferedImage.getHeight()) {
            for (x in 0 until width) {
                val lab = rgbToLab(Color(bufferedImage.getRGB(x, y)))
                val currentIndex = y * width + x
                lValues[currentIndex] = lab.l
                aValues[currentIndex] = lab.a
                bValues[currentIndex] = lab.b
            }
        }
    }


    fun getLabByXY(x: Int, y: Int): Lab {
        val index = y * width + x
        return Lab(lValues[index], aValues[index], bValues[index])
    }

}

interface Image {
    fun getWidth(): Int
    fun getHeight(): Int

    fun getRGB(x: Int, y: Int): Int
}

class BufferedImg(val bufferedImage: BufferedImage) : Image {
    override fun getWidth(): Int {
        return bufferedImage.width
    }

    override fun getHeight(): Int {
        return bufferedImage.height
    }

    override fun getRGB(x: Int, y: Int): Int {
        return bufferedImage.getRGB(x, y)
    }
}

class PseudoImage(val image: Array<IntArray>) : Image {
    override fun getWidth(): Int {
        return image[0].size
    }

    override fun getHeight(): Int {
        return image.size
    }

    override fun getRGB(x: Int, y: Int): Int {
        return image[x][y]
    }


}