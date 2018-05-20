package adymova.nsu.grafics.panels.segmentation

import adymova.nsu.grafics.core.Lab
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.eigen.Eigen.symmetricGeneralizedEigenvalues
import org.nd4j.linalg.factory.Nd4j
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.exp
import kotlin.system.measureTimeMillis


class NormalizedCutSegmentation {
    fun apply(bufferedImage: Image, metrics: (Lab, Lab) -> Double) {
        val width = bufferedImage.getWidth()
        val height = bufferedImage.getHeight()
        val vertexCount = height * width

        val imageAsLabArrays = ImageAsLabArrays(bufferedImage)
        val weightMatrix = buildWeightsMatrix(vertexCount, height, width, metrics, imageAsLabArrays)

        val indices = IntArray(vertexCount)
        for (index in (0 until vertexCount)) {
            indices[index] = index
        }
        val diagonalMatrix = buildDiagonalMatrix(vertexCount, weightMatrix)

        val stack: Deque<Partition> = ArrayDeque<Partition>()
        stack.push(Partition(weightMatrix, diagonalMatrix, indices))
        val resultSegments = mutableListOf<IntArray>()
        while (stack.isNotEmpty()) {
            val partitionInfo = stack.pop()
            if (isHomogenous(partitionInfo.indices, imageAsLabArrays, metricFunc = metrics)) {
                resultSegments.add(partitionInfo.indices)
                continue
            }
            val partition = cut(partitionInfo.weightMatrix, partitionInfo.diagMatrix, partitionInfo.indices)
            val weights = partitionInfo.weightMatrix.dup()
            val diags = partitionInfo.diagMatrix.dup()
            stack.push(Partition(weights, diags, partition.firstPartIndices))
            stack.push(Partition(weights, diags, partition.secondPartIndices))
        }

        applySegmentationToImage(resultSegments, bufferedImage, width)
    }

    private fun applySegmentationToImage(
        resultSegments: MutableList<IntArray>,
        bufferedImage: Image,
        width: Int
    ) {
        for (segment in resultSegments) {
            val color = bufferedImage.getRGB(segment[0] % width, segment[0] / width)
            for (index in segment) {
                val y = index / width
                val x = index % width
                bufferedImage.setRGB(x, y, color)
            }
        }
    }

    private fun buildDiagonalMatrix(
        vertexCount: Int,
        weightMatrix: INDArray
    ): INDArray {
        val diagonalMatrix = Nd4j.zeros(vertexCount, vertexCount)
        for (y in (0 until vertexCount)) {
            var sum = 0.0
            for (x in (0 until vertexCount)) {
                sum += weightMatrix.getDouble(y, x)
            }
            diagonalMatrix.put(y, y, sum)
        }
        return diagonalMatrix
    }

    class Partition(
        val weightMatrix: INDArray,
        val diagMatrix: INDArray,
        val indices: IntArray
    )

    private fun isHomogenous(
        indexArr: IntArray,
        image: ImageAsLabArrays,
        threshold: Double = 2.0,
        metricFunc: (Lab, Lab) -> Double = ::computeCiede2000Metrics
    ): Boolean {
        val width = image.width
        for (index in indexArr) {
            val y1 = index / width
            val x1 = index % width
            for (index2 in indexArr) {
                val y2 = index2 / width
                val x2 = index2 % width
                if (metricFunc(image.getLabByXY(x1, y1), image.getLabByXY(x2, y2)) > threshold) return false
            }
        }
        return true
    }

    fun buildWeightsMatrix(
        vertexCount: Int,
        height: Int,
        width: Int,
        metrics: (Lab, Lab) -> Double,
        labArrays: ImageAsLabArrays
    ): INDArray {
        val weights = Nd4j.zeros(vertexCount, vertexCount)
        for (y in 0 until height) {
            for (x in 0 until width) {
                for ((xOffset, yOffset) in neighborOffsets) {
                    val xNeighborIndex = x + xOffset
                    val yNeighborIndex = y + yOffset
                    if (xNeighborIndex < 0 || xNeighborIndex >= width || yNeighborIndex < 0 || yNeighborIndex >= height) {
                        continue
                    }
                    val neighborIndex = getIndexInGraphMatrix(xNeighborIndex, yNeighborIndex, width)
                    val originalIndex = getIndexInGraphMatrix(x, y, width)
                    if (weights.getDouble(neighborIndex, originalIndex) == 0.0) {
                        val weight = metrics(
                            labArrays.getLabByXY(x, y),
                            labArrays.getLabByXY(xNeighborIndex, yNeighborIndex)
                        )
                        weights.put(neighborIndex, originalIndex, weight)
                        weights.put(originalIndex, neighborIndex, weight)
                    }
                }
            }
        }
        return weights
    }


}

fun weightMetrics(lab1: Lab, lab2: Lab): Double {
    return exp(-computeCiede2000Metrics(lab1, lab2))
}

val neighborOffsets = arrayOf(
    -1 to 1,
    -1 to 0,
    -1 to -1,
    0 to 1,
    0 to -1,
    1 to -1,
    1 to 0,
    1 to 1
)

data class IntPair(val x: Int, val y: Int)

infix fun Int.to(y: Int) = IntPair(this, y)

fun getIndexInGraphMatrix(x: Int, y: Int, width: Int) = y * width + x

class CutResult(
    val firstPartIndices: IntArray,
    val secondPartIndices: IntArray
)

fun cut(weightMatrix: INDArray, diagMatrix: INDArray, indexArray: IntArray): CutResult {
    val diagMinusWeight = diagMatrix.sub(weightMatrix)
    val eigenvalues =
        symmetricGeneralizedEigenvalues(diagMinusWeight, diagMatrix, true).data().asDouble() as DoubleArray
    val eigenvalue = eigenvalues.sorted()[1]
    val indexOfSecondSmallestEigenvalue = eigenvalues.indexOf(eigenvalue)
    if (indexOfSecondSmallestEigenvalue == -1) throw IllegalStateException()
    val targetEigenvector = diagMatrix.getColumn(indexOfSecondSmallestEigenvalue)
    val partitionArray = targetEigenvector.data().asDouble() as DoubleArray
    val average = partitionArray.average()
    val firstPartitionSize = partitionArray.count { it > average }
//    val firstPartitionSize = partitionArray.count { it > 0 }
    val secondPartitionSize = partitionArray.size - firstPartitionSize
    val firstPartition = IntArray(firstPartitionSize)
    val secondPartition = IntArray(secondPartitionSize)
    var firstPartIndex = 0
    var secondPartIndex = 0
    for ((index, value) in partitionArray.withIndex()) {
        if (value > 0) {
            firstPartition[firstPartIndex] = indexArray[index]
            firstPartIndex++
        } else {
            secondPartition[secondPartIndex] = indexArray[index]
            secondPartIndex++
        }
    }
    return CutResult(firstPartition, secondPartition)
}

fun main(args: Array<String>) {

//    val file = File("/Users/nastya/picolena.png")
    val file = File("/Users/nastya/IdeaProjects/graphics-lab-1/src/main/resources/picolena.png")
    val bufferedImage = ImageIO.read(file)
    val millis = measureTimeMillis {
        NormalizedCutSegmentation().apply(
            BufferedImg(bufferedImage),
            { lab1, lab2 -> weightMetrics(lab1, lab2) })
    }
    println(millis)
    ImageIO.write(bufferedImage, "png", File("/Users/nastya/IdeaProjects/graphics-lab-1/ncut.png"))

}