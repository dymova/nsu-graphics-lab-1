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
        private val imageAsLabArrays: ImageAsLabArrays,
        val parent: Node?
) {
    var passedByMergeAlgorithm = false
    var children: Array<Node> = emptyArray()
    private val leftTopChildIndex = 0
    private val rightTopChildIndex = 1
    private val leftBottomChildIndex = 2
    private val rightBottomChildIndex = 3


    fun splitIfPossible(maxDifference: Double, metrics: (Lab, Lab) -> Double) {
        if (!isHomogeneous(maxDifference, metrics) && xEnd != xStart - 1 || yEnd == yStart - 1) {
            children = split()
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
        for (child in children) {
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
        return children.isEmpty()
    }

    fun findNeigbors(): Neighbors {
        val neighbors = Neighbors()
        var parent = parent ?: return neighbors
        var currentNode = this


        while (true) {
            when (currentNode) {
                parent.children[leftTopChildIndex] -> {
                    if (neighbors.right.isEmpty()) {
                        parent.children[rightTopChildIndex].collectLeftSideChildren(neighbors.right)
                    }
                    if (neighbors.bottom.isEmpty()) {
                        parent.children[leftBottomChildIndex].collectTopSideChildren(neighbors.bottom)
                    }
                }
                parent.children[rightTopChildIndex] -> {
                    if (neighbors.left.isEmpty()) {
                        parent.children[leftTopChildIndex].collectRightSideChildren(neighbors.left)
                    }
                    if (neighbors.bottom.isEmpty()) {
                        parent.children[rightBottomChildIndex].collectTopSideChildren(neighbors.bottom)
                    }
                }
                parent.children[leftBottomChildIndex] -> {
                    if (neighbors.top.isEmpty()) {
                        parent.children[leftTopChildIndex].collectBottomSideChildren(neighbors.top)
                    }
                    if (neighbors.right.isEmpty()) {
                        parent.children[rightBottomChildIndex].collectLeftSideChildren(neighbors.right)
                    }
                }
                parent.children[rightBottomChildIndex] -> {
                    if (neighbors.top.isEmpty()) {
                        parent.children[rightTopChildIndex].collectBottomSideChildren(neighbors.top)
                    }
                    if (neighbors.left.isEmpty()) {
                        parent.children[leftBottomChildIndex].collectRightSideChildren(neighbors.left)
                    }
                }
            }

            if ((parent.xStart == 0 || neighbors.left.isNotEmpty()) &&
                    (parent.yStart == 0 || neighbors.top.isNotEmpty()) &&
                    (parent.xEnd == imageAsLabArrays.width || neighbors.right.isNotEmpty()) &&
                    (parent.yEnd == imageAsLabArrays.height || neighbors.bottom.isNotEmpty())
            ) break
            currentNode = parent
            parent = parent.parent ?: break
        }
        return neighbors
    }

    private fun collectLeftSideChildren(list: MutableSet<Node>) {
        if (isLeaf()) {
            list.add(this)
        } else {
            children[leftTopChildIndex].collectLeftSideChildren(list)
            children[leftBottomChildIndex].collectLeftSideChildren(list)
        }
    }

    private fun collectRightSideChildren(list: MutableSet<Node>) {
        if (isLeaf()) {
            list.add(this)
        } else {
            children[rightTopChildIndex].collectRightSideChildren(list)
            children[rightBottomChildIndex].collectRightSideChildren(list)
        }
    }

    private fun collectTopSideChildren(list: MutableSet<Node>) {
        if (isLeaf()) {
            list.add(this)
        } else {
            children[leftTopChildIndex].collectTopSideChildren(list)
            children[rightTopChildIndex].collectTopSideChildren(list)
        }
    }

    private fun collectBottomSideChildren(list: MutableSet<Node>) {
        if (isLeaf()) {
            list.add(this)
        } else {
            children[leftBottomChildIndex].collectBottomSideChildren(list)
            children[rightBottomChildIndex].collectBottomSideChildren(list)
        }
    }

}

class Neighbors {
    val right = mutableSetOf<Node>()
    val left = mutableSetOf<Node>()
    val bottom = mutableSetOf<Node>()
    val top = mutableSetOf<Node>()
}


class ImageAsLabArrays(bufferedImage: Image) {
    val width = bufferedImage.getWidth()
    val height = bufferedImage.getHeight()
    private val size = height * width

    private val lValues = FloatArray(size)
    private val aValues = FloatArray(size)
    private val bValues = FloatArray(size)

    init {
        for (y in 0 until height) {
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