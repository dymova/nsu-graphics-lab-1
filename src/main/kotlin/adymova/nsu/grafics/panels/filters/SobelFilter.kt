package adymova.nsu.grafics.panels.filters

import adymova.nsu.grafics.core.ImageContext
import java.awt.image.BufferedImage
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
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

                resultRedArray[(y - kernelRadius) * bufferedImage.width + (x - kernelRadius)] = resultRed
                resultGreenArray[(y - kernelRadius) * bufferedImage.width + (x - kernelRadius)] = resultGreen
                resultBlueArray[(y - kernelRadius) * bufferedImage.width + (x - kernelRadius)] = resultBlue
            }
        }

        applyResultToImageWithNormalization(resultRedArray, resultGreenArray, resultBlueArray, bufferedImage)
    }
}
