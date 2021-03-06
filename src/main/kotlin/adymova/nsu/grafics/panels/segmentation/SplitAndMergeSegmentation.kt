package adymova.nsu.grafics.panels.segmentation

import adymova.nsu.grafics.core.Lab
import adymova.nsu.grafics.core.rgbToLab
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
//    val file = File("/Users/nastya/lena64.png")
    val file = File("/Users/nastya/Lenna.png")
//    val file = File("/Users/nastya/parrot.png")
    val bufferedImage = ImageIO.read(file)
    val millis = measureTimeMillis {
        SplitAndMergeSegmentation().apply(BufferedImg(bufferedImage), 2.0, { lab1, lab2 -> computeCiede2000Metrics(lab1, lab2) })
    }
    println(millis)
    ImageIO.write(bufferedImage, "png", File("/Users/nastya/lena64_seg.png"))

}

class SplitAndMergeSegmentation {

    fun apply(bufferedImage: Image, maxDifference: Double, metrics: (Lab, Lab) -> Double) {
        val imageAsLabArrays = ImageAsLabArrays(bufferedImage)

        val width = bufferedImage.getWidth()
        val height = bufferedImage.getHeight()
        var currentRegionId = 1

        val root = Node(0, 0, width, height, imageAsLabArrays, null)

        val idToRegionMap = hashMapOf<Int, MutableList<Node>>()

        root.pass { it.splitIfPossible(maxDifference, metrics) }
        root.leafPass {
            it.computeMeanLab()
            fillRegionsMap(it, currentRegionId, idToRegionMap)
            currentRegionId++
        }

        //merge
        root.leafPass {
            val currentNode = it
            val neighbors = it.findNeigbors()
            val flattenNeighbors = neighbors.flat()

            flattenNeighbors.forEach {
                currentNode.mergeIfPossible(it, idToRegionMap, metrics, maxDifference)
            }
        }

        bufferedImage.applySegmentation(idToRegionMap.values)

//        val entry = idToRegionMap.values.sortedByDescending { it.size }
//        if (entry != null) {
//            println(entry.size)
//        }
    }

    private fun fillRegionsMap(it: Node, currentRegionId: Int, idToRegionMap: HashMap<Int, MutableList<Node>>) {
        idToRegionMap[currentRegionId] = arrayListOf(it)
        it.regionId = currentRegionId
    }
}


class Node(
        val xStart: Int,
        val yStart: Int,
        val xEnd: Int,
        val yEnd: Int,
        private val imageAsLabArrays: ImageAsLabArrays,
        private val parent: Node?
) {
    var regionId: Int = 0
    var children: Array<Node> = emptyArray()
    var meanLab: Lab? = null


    fun splitIfPossible(maxDifference: Double, metrics: (Lab, Lab) -> Double) {
        if (!isHomogeneous(maxDifference, metrics) && xEnd != xStart + 1 && yEnd != yStart + 1) {
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
        val src = this


        while (true) {
            when (currentNode) {
                parent.children[leftTopChildIndex] -> {
                    if (neighbors.right.isEmpty()) {
                        parent.children[rightTopChildIndex].collectChildrenInnerSide(neighbors.right, Side.LEFT, src)
                    }
                    if (neighbors.bottom.isEmpty()) {
                        parent.children[leftBottomChildIndex].collectChildrenInnerSide(neighbors.bottom, Side.TOP, src)
                    }
                }
                parent.children[rightTopChildIndex] -> {
                    if (neighbors.left.isEmpty()) {
                        parent.children[leftTopChildIndex].collectChildrenInnerSide(neighbors.left, Side.RIGHT, src)
                    }
                    if (neighbors.bottom.isEmpty()) {
                        parent.children[rightBottomChildIndex].collectChildrenInnerSide(neighbors.bottom, Side.TOP, src)
                    }
                }
                parent.children[leftBottomChildIndex] -> {
                    if (neighbors.top.isEmpty()) {
                        parent.children[leftTopChildIndex].collectChildrenInnerSide(neighbors.top, Side.TOP, src)
                    }
                    if (neighbors.right.isEmpty()) {
                        parent.children[rightBottomChildIndex].collectChildrenInnerSide(neighbors.right, Side.LEFT, src)
                    }
                }
                parent.children[rightBottomChildIndex] -> {
                    if (neighbors.top.isEmpty()) {
                        parent.children[rightTopChildIndex].collectChildrenInnerSide(neighbors.top, Side.BOTTOM, src)
                    }
                    if (neighbors.left.isEmpty()) {
                        parent.children[leftBottomChildIndex].collectChildrenInnerSide(neighbors.left, Side.RIGHT, src)
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

    private fun collectChildrenAndCheck(list: MutableSet<Node>, side: Side, src: Node) {
        val isNeighbor = when (side) {
            Side.LEFT -> src.xEnd == xStart
            Side.RIGHT -> src.xStart == xEnd
            Side.TOP -> src.yEnd == yStart
            Side.BOTTOM -> src.yStart == yEnd
        }
        if (isNeighbor) {
            collectChildrenInnerSide(list, side, src)
        }
    }

    private fun collectChildrenInnerSide(list: MutableSet<Node>, side: Side, src: Node) {
        if (isLeaf()) {
            list.add(this)
        } else {
            when {
                side == Side.LEFT -> {
                    children[leftTopChildIndex].collectChildrenAndCheck(list, side, src)
                    children[leftBottomChildIndex].collectChildrenAndCheck(list, side, src)
                }
                side == Side.TOP -> {
                    children[leftTopChildIndex].collectChildrenAndCheck(list, side, src)
                    children[rightTopChildIndex].collectChildrenAndCheck(list, side, src)
                }
                side == Side.RIGHT -> {
                    children[rightTopChildIndex].collectChildrenAndCheck(list, side, src)
                    children[rightBottomChildIndex].collectChildrenAndCheck(list, side, src)
                }
                side == Side.BOTTOM -> {
                    children[leftBottomChildIndex].collectChildrenAndCheck(list, side, src)
                    children[rightBottomChildIndex].collectChildrenAndCheck(list, side, src)
                }

            }
        }
    }

    fun mergeIfPossible(neighbor: Node, idToRegionMap: HashMap<Int, MutableList<Node>>, metrics: (Lab, Lab) -> Double, maxDifference: Double) {
        if (neighbor.regionId != regionId && notBreakHomogeneity(neighbor, metrics, maxDifference)) {
            merge(idToRegionMap, neighbor)
        }
    }

    private fun merge(idToRegionMap: HashMap<Int, MutableList<Node>>, neighbor: Node) {
        //поменять всем регионам id
        val neighborOldRegionId = neighbor.regionId
        val neighborIdRegions = idToRegionMap[neighborOldRegionId] ?: throw IllegalStateException()
        val currentNodeIdRegions = idToRegionMap[regionId] ?: throw IllegalStateException()

        for (node in neighborIdRegions) {
            node.regionId = regionId
        }
        currentNodeIdRegions.addAll(neighborIdRegions)
        idToRegionMap.remove(neighborOldRegionId)
    }


    private fun notBreakHomogeneity(neighbor: Node, metrics: (Lab, Lab) -> Double, maxDifference: Double): Boolean {
        val neighborMeanLab = neighbor.meanLab ?: throw IllegalStateException()
        val currentMeanLab = meanLab ?: throw IllegalStateException()

        val metricsResult = metrics(currentMeanLab, neighborMeanLab)
        if (metricsResult >= maxDifference) {
            return false
        }
        return true
    }

    fun computeMeanLab() {
        val meanLab = Lab(0f, 0f, 0f)
        var count = 0

        for (x in xStart until xEnd) {
            for (y in yStart until yEnd) {
                val lab = imageAsLabArrays.getLabByXY(x, y)
                meanLab.l += lab.l
                meanLab.a += lab.a
                meanLab.b += lab.b
                count++
            }
        }
        if (count == 0) {
            println()
        }
        meanLab.l /= count
        meanLab.a /= count
        meanLab.b /= count
        this.meanLab = meanLab

    }
}

class Neighbors {
    val right = mutableSetOf<Node>()
    val left = mutableSetOf<Node>()
    val bottom = mutableSetOf<Node>()
    val top = mutableSetOf<Node>()


    fun flat(): MutableList<Node> {
        val list = mutableListOf<Node>()
        list.addAll(right)
        list.addAll(left)
        list.addAll(bottom)
        list.addAll(top)
        return list
    }
}


class ImageAsLabArrays(lValues: FloatArray, aValues: FloatArray, bValues: FloatArray, val width: Int, val height: Int) {
    private val size = height * this.width

    private val lValues = FloatArray(size)
    private val aValues = FloatArray(size)
    private val bValues = FloatArray(size)

    constructor(bufferedImage: Image) : this(FloatArray(bufferedImage.getHeight() * bufferedImage.getWidth()),
            FloatArray(bufferedImage.getHeight() * bufferedImage.getWidth()),
            FloatArray(bufferedImage.getHeight() * bufferedImage.getWidth()),
            bufferedImage.getWidth(),
            bufferedImage.getHeight()
    ) {
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

    fun copy(): ImageAsLabArrays {
        return ImageAsLabArrays(lValues, aValues, bValues, width, height)
    }

}

interface Image {
    fun getWidth(): Int
    fun getHeight(): Int

    fun getRGB(x: Int, y: Int): Int
    fun applySegmentation(values: MutableCollection<MutableList<Node>>)
    fun setRGB(x: Int, y: Int, color: Int)
}

class BufferedImg(val bufferedImage: BufferedImage) : Image {
    override fun setRGB(x: Int, y: Int, color: Int) {
        bufferedImage.setRGB(x, y, color)
    }

    override fun getWidth(): Int {
        return bufferedImage.width
    }

    override fun getHeight(): Int {
        return bufferedImage.height
    }

    override fun getRGB(x: Int, y: Int): Int {
        return bufferedImage.getRGB(x, y)
    }

    override fun applySegmentation(values: MutableCollection<MutableList<Node>>) {
        for (value in values) {
            var rgb: Int? = null
            for (node in value) {
                if (rgb == null) {
                    rgb = getRGB(node.xStart, node.yStart)
                }

                applySegmentationToNode(node, Color(rgb))
            }
            rgb = null
        }
    }

    private fun applySegmentationToNode(node: Node, color: Color) {
        for (x in node.xStart until node.xEnd) {
            for (y in node.yStart until node.yEnd) {
                bufferedImage.setRGB(x, y, color.rgb)
            }
        }
    }
}

class PseudoImage(val image: Array<IntArray>) : Image {
    override fun setRGB(x: Int, y: Int, color: Int) {
        image[x][y] = color
    }

    override fun applySegmentation(values: MutableCollection<MutableList<Node>>) {
        println("PseudoImage.applySegmentation was invoked")
    }

    override fun getWidth(): Int {
        return image[0].size
    }

    override fun getHeight(): Int {
        return image.size
    }

    override fun getRGB(x: Int, y: Int): Int {
        return image[y][x]
    }


}

enum class Side {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
}

private const val leftTopChildIndex = 0
private const val rightTopChildIndex = 1
private const val leftBottomChildIndex = 2
private const val rightBottomChildIndex = 3