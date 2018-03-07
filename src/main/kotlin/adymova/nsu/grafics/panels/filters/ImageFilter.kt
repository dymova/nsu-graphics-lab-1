package adymova.nsu.grafics.panels.filters

import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.roundToInt


inline fun convolution(
        matrix: Array<FloatArray>,
        img: BufferedImage,
        x: Int,
        y: Int,
        chanelType: ChanelType,
        radius: Int
): Float {
    var sum = 0f
    for (currentY in y - radius until y + radius) {
        for (currentX in x - radius until x + radius) {
            val rgb = img.getRGB(currentX, currentY)
            val chanelValue = selectChanel(chanelType, rgb)
            val matrixX = currentX - x + radius
            val matrixY = currentY - y + radius
            try {
                val kernelVal = matrix[matrixX][matrixY]
                sum += kernelVal * chanelValue
            } catch (e: Exception) {
                println()
            }
        }
    }
    return sum
}

inline fun selectChanel(chanelType: ChanelType, rgb: Int) = when (chanelType) {
    ChanelType.R -> getRed(rgb)
    ChanelType.G -> getGreen(rgb)
    ChanelType.B -> getBlue(rgb)
}

enum class ChanelType {
    R,
    G,
    B
}


fun createIntermediateImage(bufferedImage: BufferedImage, kernelRadius: Int): BufferedImage {
    val newImage = BufferedImage(bufferedImage.width + kernelRadius * 2,
            bufferedImage.height + kernelRadius * 2,
            BufferedImage.TYPE_INT_RGB)

    fillEdges(newImage, bufferedImage, kernelRadius)
    return newImage
}

fun getRedValue(bufferedImage: BufferedImage, x: Int, y: Int): Int {
    val rgb = bufferedImage.getRGB(x, y)
    return getRed(rgb)
}

fun getGreenValue(bufferedImage: BufferedImage, x: Int, y: Int): Int {
    val rgb = bufferedImage.getRGB(x, y)
    return getGreen(rgb)
}

fun getBlueValue(bufferedImage: BufferedImage, x: Int, y: Int): Int {
    val rgb = bufferedImage.getRGB(x, y)
    return getBlue(rgb)
}

inline fun getRed(rgb: Int): Int {
    return rgb shr 16 and 0xFF
}

inline fun getGreen(rgb: Int): Int {
    return rgb shr 8 and 0xFF
}

inline fun getBlue(rgb: Int): Int {
    return rgb shr 0 and 0xFF
}


fun fillEdges(newImage: BufferedImage, bufferedImage: BufferedImage, kernelRadius: Int) {

    for (x in kernelRadius until newImage.width - kernelRadius) {
        //top
        for (y in 0 until kernelRadius) {
            newImage.setRGB(x, y, bufferedImage.getRGB(x - kernelRadius, 0))
        }
        //bottom
        for (y in newImage.height - kernelRadius until newImage.height) {
            val value = bufferedImage.getRGB(x - kernelRadius, bufferedImage.height - 1)
            newImage.setRGB(x, y, value)
        }
        //center
        for (y in kernelRadius until newImage.height - kernelRadius) {
            newImage.setRGB(x, y, bufferedImage.getRGB(x - kernelRadius, y - kernelRadius))
        }
    }

    for (y in kernelRadius until newImage.height - kernelRadius) {
        //left
        for (x in 0 until kernelRadius) {
            newImage.setRGB(x, y, bufferedImage.getRGB(0, y - kernelRadius))
        }
        //right
        for (x in newImage.width - kernelRadius until newImage.width) {
            newImage.setRGB(x, y, bufferedImage.getRGB(bufferedImage.width - 1, y - kernelRadius))
        }
    }

    for (x in 0 until kernelRadius) {
        for (y in 0 until kernelRadius) {
            newImage.setRGB(x, y, bufferedImage.getRGB(0, 0))
            newImage.setRGB(x + bufferedImage.width + kernelRadius, y, bufferedImage.getRGB(bufferedImage.width - 1, 0))
            newImage.setRGB(x, y + bufferedImage.height + kernelRadius, bufferedImage.getRGB(0, bufferedImage.height - 1))
            newImage.setRGB(x + bufferedImage.width + kernelRadius, y + bufferedImage.height + kernelRadius, bufferedImage.getRGB(bufferedImage.width - 1, bufferedImage.height - 1))
        }
    }
}

fun applyResultToImageWithNormalization(resultRedArray: FloatArray, resultGreenArray: FloatArray, resultBlueArray: FloatArray, bufferedImage: BufferedImage) {
    val maxRed = resultRedArray.max()!!
    val maxGreen = resultGreenArray.max()!!
    val maxBlue = resultBlueArray.max()!!

    for (y in 0 until bufferedImage.height) {
        for (x in 0 until bufferedImage.width) {
            val valueR = resultRedArray[y * bufferedImage.width + x]
            val valueG = resultGreenArray[y * bufferedImage.width + x]
            val valueB = resultBlueArray[y * bufferedImage.width + x]
            val delta = 0.0001
            val r = if (maxRed < delta) 0 else (valueR * 255.0f / maxRed).roundToInt()
            val g = if (maxGreen < delta) 0 else (valueG * 255.0f / maxGreen).roundToInt()
            val b = if (maxBlue < delta) 0 else (valueB * 255.0f / maxBlue).roundToInt()
            bufferedImage.setRGB(x, y, Color(r, g, b).rgb)
        }
    }
}