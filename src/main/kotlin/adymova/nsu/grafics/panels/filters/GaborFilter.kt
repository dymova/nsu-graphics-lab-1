package adymova.nsu.grafics.panels.filters

import adymova.nsu.grafics.core.ImageContext
import java.awt.GridLayout
import java.awt.image.BufferedImage
import javax.swing.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

class GaborFilterPanel(val imageContext: ImageContext) : JPanel() {
    private val gaborFilter = GaborFilter()
    private val tettaSlider: JSlider = JSlider(JSlider.HORIZONTAL, 0, 135, 45)

    init {
        layout = GridLayout(0, 1)

        border = BorderFactory.createTitledBorder("Gabor")

        addApplyButton()
        addSliders()
    }

    private fun addApplyButton() {
        val applyButton = JButton("Apply")
        applyButton.addActionListener {
            val image = imageContext.changedImage ?: return@addActionListener
                    //todo add kernel size slider
            gaborFilter.apply(image, tettaSlider.value.toFloat(), 1.0f, 2.0f, 5)
            imageContext.notifyImageUpdateListeners()
        }
        add(applyButton)
    }

    private fun addSliders() {
        add(JLabel("tetta"))
        tettaSlider.majorTickSpacing = 45
        tettaSlider.paintTicks = true
        tettaSlider.paintLabels = true
        tettaSlider.snapToTicks = true
        add(tettaSlider)
    }

}

class GaborFilter {

    fun apply(bufferedImage: BufferedImage, tetta: Float, gamma: Float, lambda: Float, kernelSize: Int) {
        println("$tetta $gamma $lambda")
        val kernel = generateKernel(kernelSize, tetta, gamma, lambda)
        val kernelRadius = kernelSize / 2

        val intermediateImage = createIntermediateImage(bufferedImage, kernelRadius)

        val resultRedArray = FloatArray(bufferedImage.height * bufferedImage.width)
        val resultGreenArray = FloatArray(bufferedImage.height * bufferedImage.width)
        val resultBlueArray = FloatArray(bufferedImage.height * bufferedImage.width)

        for (y in kernelRadius until intermediateImage.height - kernelRadius) {
            for (x in kernelRadius until intermediateImage.width - kernelRadius) {
                val resultIndex = (y - kernelRadius) * bufferedImage.width + (x - kernelRadius)
                resultRedArray[resultIndex] =
                        convolution(kernel, intermediateImage, x, y, ChanelType.R, kernelRadius)
                resultGreenArray[resultIndex] =
                        convolution(kernel, intermediateImage, x, y, ChanelType.G, kernelRadius)
                resultBlueArray[resultIndex] =
                        convolution(kernel, intermediateImage, x, y, ChanelType.B, kernelRadius)
            }
        }

        applyResultToImageWithNormalization(resultRedArray, resultGreenArray, resultBlueArray, bufferedImage)

    }

    private fun generateKernel(size: Int, theta: Float, gamma: Float, lambda: Float): Array<FloatArray> {
        val kernel = Array(size, {
            FloatArray(size)
        })
        val fi = 0
        val sigma = 0.56f*lambda

        for (x in 0 until size) {
            for (y in 0 until size) {
                val (polarX, polarY) = convertToPolarCoordinates(x, y, theta)
                kernel[x][y] = exp(-((polarX * polarX + gamma * gamma * polarY * polarY) / sigma * sigma) / 2) * cos(2 * PI * f(lambda) * polarX + fi).toFloat()
            }
        }

        return kernel
    }

    private fun convertToPolarCoordinates(x: Int, y: Int, tetta: Float): Coordinate {
        val polarX = x * cos(tetta) + y * sin(tetta)
        val polarY = -x * sin(tetta) + y * cos(tetta)
        return Coordinate(polarX, polarY)
    }

    private fun f(lambda: Float): Float {
        return 1 / lambda
    }

    data class Coordinate(
            val x: Float,
            val y: Float
    )
}
