package adymova.nsu.grafics.panels.filters

import adymova.nsu.grafics.core.ImageContext
import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import kotlin.math.roundToInt
import kotlin.math.sqrt

class SobelFilterPanel(val imageContext: ImageContext) : JPanel() {
    private val sobelFilter = SobelFilter()

    init {
        border = BorderFactory.createTitledBorder("Sobel")

        val applyButton = JButton("Apply")
//        val cancelButton = JButton("Cancel")
        applyButton.addActionListener {
            val image = imageContext.changedImage ?: return@addActionListener
            sobelFilter.apply(image)
            imageContext.notifyImageUpdateListeners()
        }
//        add(cancelButton)
        add(applyButton)
    }

}


class SobelFilter {
    fun apply(bufferedImage: BufferedImage) {
        val sobelY = arrayOf(
                intArrayOf(-1, -2, -1),
                intArrayOf(0, 0, 0),
                intArrayOf(1, 2, 1)
        )

        val sobelX = arrayOf(
                intArrayOf(-1, 0, 1),
                intArrayOf(-2, 0, 2),
                intArrayOf(-1, 0, 1)
        )

        val kernelRadius = 1

        val newImage = createIntermediateImage(bufferedImage, kernelRadius)


        var redXValue: Int
        var greenXValue: Int
        var blueXValue: Int
        var redYValue: Int
        var greenYValue: Int
        var blueYValue: Int

        var resultRed: Float
        var resultGreen: Float
        var resultBlue: Float

        val resultRedArray = FloatArray(bufferedImage.height * bufferedImage.width)
        val resultGreenArray = FloatArray(bufferedImage.height * bufferedImage.width)
        val resultBlueArray = FloatArray(bufferedImage.height * bufferedImage.width)
        for (y in kernelRadius until newImage.height - kernelRadius) {
            for (x in kernelRadius until newImage.width - kernelRadius) {

                redXValue = (sobelX[0][0] * getRedValue(newImage, x - 1, y - 1)) + (sobelX[0][1] * getRedValue(newImage, x, y - 1)) + (sobelX[0][2] * getRedValue(newImage, x + 1, y - 1)) +
                        (sobelX[1][0] * getRedValue(newImage, x - 1, y)) + (sobelX[1][1] * getRedValue(newImage, x, y)) + (sobelX[1][2] * getRedValue(newImage, x + 1, y)) +
                        (sobelX[2][0] * getRedValue(newImage, x - 1, y + 1)) + (sobelX[2][1] * getRedValue(newImage, x, y + 1)) + (sobelX[2][2] * getRedValue(newImage, x + 1, y + 1))

                greenXValue = (sobelX[0][0] * getGreenValue(newImage, x - 1, y - 1)) + (sobelX[0][1] * getGreenValue(newImage, x, y - 1)) + (sobelX[0][2] * getGreenValue(newImage, x + 1, y - 1)) +
                        (sobelX[1][0] * getGreenValue(newImage, x - 1, y)) + (sobelX[1][1] * getGreenValue(newImage, x, y)) + (sobelX[1][2] * getGreenValue(newImage, x + 1, y)) +
                        (sobelX[2][0] * getGreenValue(newImage, x - 1, y + 1)) + (sobelX[2][1] * getGreenValue(newImage, x, y + 1)) + (sobelX[2][2] * getGreenValue(newImage, x + 1, y + 1))

                blueXValue = (sobelX[0][0] * getBlueValue(newImage, x - 1, y - 1)) + (sobelX[0][1] * getBlueValue(newImage, x, y - 1)) + (sobelX[0][2] * getBlueValue(newImage, x + 1, y - 1)) +
                        (sobelX[1][0] * getBlueValue(newImage, x - 1, y)) + (sobelX[1][1] * getBlueValue(newImage, x, y)) + (sobelX[1][2] * getBlueValue(newImage, x + 1, y)) +
                        (sobelX[2][0] * getBlueValue(newImage, x - 1, y + 1)) + (sobelX[2][1] * getBlueValue(newImage, x, y + 1)) + (sobelX[2][2] * getBlueValue(newImage, x + 1, y + 1))

                redYValue = (sobelY[0][0] * getRedValue(newImage, x - 1, y - 1)) + (sobelY[0][1] * getRedValue(newImage, x, y - 1)) + (sobelY[0][2] * getRedValue(newImage, x + 1, y - 1)) +
                        (sobelY[1][0] * getRedValue(newImage, x - 1, y)) + (sobelY[1][1] * getRedValue(newImage, x, y)) + (sobelY[1][2] * getRedValue(newImage, x + 1, y)) +
                        (sobelY[2][0] * getRedValue(newImage, x - 1, y + 1)) + (sobelY[2][1] * getRedValue(newImage, x, y + 1)) + (sobelY[2][2] * getRedValue(newImage, x + 1, y + 1))

                greenYValue = (sobelY[0][0] * getGreenValue(newImage, x - 1, y - 1)) + (sobelY[0][1] * getGreenValue(newImage, x, y - 1)) + (sobelY[0][2] * getGreenValue(newImage, x + 1, y - 1)) +
                        (sobelY[1][0] * getGreenValue(newImage, x - 1, y)) + (sobelY[1][1] * getGreenValue(newImage, x, y)) + (sobelY[1][2] * getGreenValue(newImage, x + 1, y)) +
                        (sobelY[2][0] * getGreenValue(newImage, x - 1, y + 1)) + (sobelY[2][1] * getGreenValue(newImage, x, y + 1)) + (sobelY[2][2] * getGreenValue(newImage, x + 1, y + 1))

                blueYValue = (sobelY[0][0] * getBlueValue(newImage, x - 1, y - 1)) + (sobelY[0][1] * getBlueValue(newImage, x, y - 1)) + (sobelY[0][2] * getBlueValue(newImage, x + 1, y - 1)) +
                        (sobelY[1][0] * getBlueValue(newImage, x - 1, y)) + (sobelY[1][1] * getBlueValue(newImage, x, y)) + (sobelY[1][2] * getBlueValue(newImage, x + 1, y)) +
                        (sobelY[2][0] * getBlueValue(newImage, x - 1, y + 1)) + (sobelY[2][1] * getBlueValue(newImage, x, y + 1)) + (sobelY[2][2] * getBlueValue(newImage, x + 1, y + 1))


                resultRed = sqrt(redXValue * redXValue + redYValue * redYValue.toFloat())
                resultGreen = sqrt(greenXValue * greenXValue + greenYValue * greenYValue.toFloat())
                resultBlue = sqrt(blueXValue * blueXValue + blueYValue * blueYValue.toFloat())

                resultRedArray[y * bufferedImage.width + x] = resultRed
                resultGreenArray[y * bufferedImage.width + x] = resultGreen
                resultBlueArray[y * bufferedImage.width + x] = resultBlue
            }
        }

        val maxRed = resultRedArray.max()!!
        val maxGreen = resultGreenArray.max()!!
        val maxBlue = resultBlueArray.max()!!

        for (y in kernelRadius until bufferedImage.height) {
            for (x in kernelRadius until bufferedImage.width) {
                val valueR = resultRedArray[y * bufferedImage.width + x]
                val valueG = resultGreenArray[y * bufferedImage.width + x]
                val valueB = resultBlueArray[y * bufferedImage.width + x]
                val r = (valueR * 255.0f / maxRed).roundToInt()
                val g = (valueG * 255.0f / maxGreen).roundToInt()
                val b = (valueB * 255.0f / maxBlue).roundToInt()
                try {
                    bufferedImage.setRGB(x, y, Color(r, g, b).rgb)
                } catch (e: Exception) {
                    println("$r $g $b")
                }
            }
        }

    }

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

private fun getBlueValue(bufferedImage: BufferedImage, x: Int, y: Int): Int {
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


fun fillEdges(newImage: BufferedImage, bufferedImage: BufferedImage, extensionSize: Int) {

    for (x in extensionSize until newImage.width - extensionSize) {
        //top
        for (y in 0 until extensionSize) {
            newImage.setRGB(x, y, bufferedImage.getRGB(x - extensionSize, 0))
        }
        //bottom
        for (y in newImage.height - extensionSize until newImage.height) {
            val value = bufferedImage.getRGB(x - extensionSize, bufferedImage.height - 1)
            newImage.setRGB(x, y, value)
        }
        //center
        for (y in extensionSize until newImage.height - extensionSize) {
            newImage.setRGB(x, y, bufferedImage.getRGB(x - extensionSize, y - extensionSize))
        }
    }

    for (y in extensionSize until newImage.height - extensionSize) {
        //left
        for (x in 0 until extensionSize) {
            newImage.setRGB(x, y, bufferedImage.getRGB(0, y - extensionSize))
        }
        //right
        for (x in newImage.width - extensionSize until newImage.width) {
            newImage.setRGB(x, y, bufferedImage.getRGB(bufferedImage.width - 1, y - extensionSize))
        }
    }

    for (x in 0 until extensionSize) {
        for (y in 0 until extensionSize) {
            newImage.setRGB(x, y, bufferedImage.getRGB(0, 0))
            newImage.setRGB(x + bufferedImage.width - 1, y, bufferedImage.getRGB(bufferedImage.width - 1, 0))
            newImage.setRGB(x, y + bufferedImage.height - 1, bufferedImage.getRGB(0, bufferedImage.height - 1))
            newImage.setRGB(x + bufferedImage.width - 1, y + bufferedImage.height - 1, bufferedImage.getRGB(bufferedImage.width - 1, bufferedImage.height - 1))
        }
    }
}