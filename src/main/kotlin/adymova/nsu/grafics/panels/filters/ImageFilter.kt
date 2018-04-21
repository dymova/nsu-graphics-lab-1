package adymova.nsu.grafics.panels.filters

import adymova.nsu.grafics.core.rgbToHsv
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

fun convolution(
        kernel: Array<FloatArray>,
        intermediateImage: BufferedImage,
        x: Int,
        y: Int,
        chanelType: ChanelType,
        radius: Int
): Float {
    var sum = 0f
    var kernelSum = 0f
    for (currentY in y - radius..y + radius) {
        for (currentX in x - radius..x + radius) {
            val rgb = intermediateImage.getRGB(currentX, currentY)
            val chanelValue = selectChanel(chanelType, rgb)
            val kernelX = currentX - x + radius
            val kernelY = currentY - y + radius
            val kernelVal = kernel[kernelX][kernelY]
            sum += kernelVal * chanelValue
            kernelSum += kernelVal
        }
    }

    if (kernelSum <= 0) kernelSum = 1f
    sum /= kernelSum
    return sum
}

fun gaborConvolution(
        kernel: Array<FloatArray>,
        intermediateImage: BufferedImage,
        x: Int,
        y: Int,
        radius: Int
): Float {
    var sum = 0f
    var kernelSum = 0f
    for (currentY in y - radius..y + radius) {
        for (currentX in x - radius..x + radius) {
            val rgb = intermediateImage.getRGB(currentX, currentY)
            val hsv = rgbToHsv(Color(rgb))
            val chanelValue = hsv.v
            val kernelX = currentX - x + radius
            val kernelY = currentY - y + radius
            val kernelVal = kernel[kernelX][kernelY]
            sum += kernelVal * chanelValue
            kernelSum += kernelVal
        }
    }

    if (kernelSum <= 0) kernelSum = 1f
    sum /= kernelSum
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

//fun applyResultToImageWithNormalization(resultRedArray: FloatArray, resultGreenArray: FloatArray, resultBlueArray: FloatArray, bufferedImage: BufferedImage) {
//    val maxRed = resultRedArray.max()!!
//    val maxGreen = resultGreenArray.max()!!
//    val maxBlue = resultBlueArray.max()!!
//
//    for (y in 0 until bufferedImage.height) {
//        for (x in 0 until bufferedImage.width) {
//            val valueIndex = y * bufferedImage.width + x
//            val valueR = resultRedArray[valueIndex]
//            val valueG = resultGreenArray[valueIndex]
//            val valueB = resultBlueArray[valueIndex]
//            val delta = 0.0001
//            val r = if (maxRed < delta) 0 else (valueR * 255.0f / maxRed).roundToInt()
//            val g = if (maxGreen < delta) 0 else (valueG * 255.0f / maxGreen).roundToInt()
//            val b = if (maxBlue < delta) 0 else (valueB * 255.0f / maxBlue).roundToInt()
//            try {
//                bufferedImage.setRGB(x, y, Color(r, g, b).rgb)
//            } catch (e: Exception) {
//                println(e)
//            }
//        }
//    }
//}

fun applyResultToImage(resultRedArray: FloatArray, resultGreenArray: FloatArray,
                       resultBlueArray: FloatArray, bufferedImage: BufferedImage) {
    for (y in 0 until bufferedImage.height) {
        for (x in 0 until bufferedImage.width) {
            val valueIndex = y * bufferedImage.width + x
            val valueR = resultRedArray[valueIndex]
            val valueG = resultGreenArray[valueIndex]
            val valueB = resultBlueArray[valueIndex]
            bufferedImage.setRGB(x, y, Color(valueR.roundToInt(), valueG.roundToInt(), valueB.roundToInt()).rgb)
        }
    }
}

fun applyResultToImageWithNormalization(resultRedArray: FloatArray, resultGreenArray: FloatArray,
                                        resultBlueArray: FloatArray, bufferedImage: BufferedImage) {
    val maxRed = resultRedArray.max()!!
    val minRed = resultRedArray.min()!!
    val maxGreen = resultGreenArray.max()!!
    val minGreen = resultGreenArray.min()!!
    val maxBlue = resultBlueArray.max()!!
    val minBlue = resultBlueArray.min()!!

    val deltaRed = maxRed - minRed
    val deltaGreen = maxGreen - minGreen
    val deltaBlue = maxBlue - minBlue

    var count: Int = 0
    for (y in 0 until bufferedImage.height) {
        for (x in 0 until bufferedImage.width) {
            val valueIndex = y * bufferedImage.width + x
            val valueR = resultRedArray[valueIndex]
            val valueG = resultGreenArray[valueIndex]
            val valueB = resultBlueArray[valueIndex]
            val delta = 0.0001
            val r = if (maxRed < delta) 0 else ((valueR - minRed) * 255.0f / deltaRed).roundToInt()
            val g = if (maxGreen < delta) 0 else ((valueG - minGreen) * 255.0f / deltaGreen).roundToInt()
            val b = if (maxBlue < delta) 0 else ((valueB - minBlue) * 255.0f / deltaBlue).roundToInt()
            bufferedImage.setRGB(x, y, Color(r, g, b).rgb)
        }
    }

    println("Incorrect count: $count / ${bufferedImage.height * bufferedImage.width}")
}

fun applyResultToImageWithNormalizationGabor(resultVArray: FloatArray, bufferedImage: BufferedImage) {
    val maxV = resultVArray.max()!!
    val minV = resultVArray.min()!!

    val deltaV = maxV - minV

    var count: Int = 0
    for (y in 0 until bufferedImage.height) {
        for (x in 0 until bufferedImage.width) {
            val valueIndex = y * bufferedImage.width + x
            val valueV = resultVArray[valueIndex]
            val delta = 0.0001
            val v = if (maxV < delta) 0 else ((valueV - minV) * 255.0f / deltaV).roundToInt()
            bufferedImage.setRGB(x, y, Color(v, v, v).rgb)
        }
    }

    println("Incorrect count: $count / ${bufferedImage.height * bufferedImage.width}")
}