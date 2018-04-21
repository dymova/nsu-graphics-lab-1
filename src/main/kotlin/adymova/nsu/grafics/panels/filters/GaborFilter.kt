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

    fun apply(bufferedImage: BufferedImage, theta: Float, gamma: Float, lambda: Float, kernelSize: Int) {
        val kernel = generateKernel(kernelSize, theta, gamma, lambda)
        val kernelRadius = kernelSize / 2

        val intermediateImage = createIntermediateImage(bufferedImage, kernelRadius)

        val resultVArray = FloatArray(bufferedImage.height * bufferedImage.width)

        for (y in kernelRadius until intermediateImage.height - kernelRadius) {
            for (x in kernelRadius until intermediateImage.width - kernelRadius) {
                val resultIndex = (y - kernelRadius) * bufferedImage.width + (x - kernelRadius)
                resultVArray[resultIndex] =
                        gaborConvolution(kernel, intermediateImage, x, y, kernelRadius)
            }
        }

        applyResultToImageWithNormalizationGabor(resultVArray, bufferedImage)
    }

    private fun generateKernel(size: Int, theta: Float, gamma: Float, lambda: Float): Array<FloatArray> {
        val kernel = Array(size, {
            FloatArray(size)
        })
        val fi = 0
        val sigma = 0.56f * lambda

        for (x in 0 until size) {
            for (y in 0 until size) {
                val (polarX, polarY) = convertToPolarCoordinates(x - kernel.size / 2, y - kernel.size / 2, theta)
                kernel[x][y] = exp(-(polarX * polarX + gamma * gamma * polarY * polarY) / (sigma * sigma * 2)) * cos(2 * PI * polarX / lambda + fi).toFloat()
            }
        }

        return kernel
    }
//    private fun showMatrix(matrix: Array<FloatArray>): BufferedImage {
//        val min = matrix.values.min()!!
//        val max = matrix.values.max()!! - min
//        val size = matrix.size
//        val img = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
//        for (y in (0 until size)) {
//            for (x in (0 until size)) {
//                val value = matrix[x][y] - min
//                val brightness = (value / max * 255.0f).toInt()
//                img.setRGB(x, y, Color(brightness, brightness, brightness).rgb)
//            }
//        }
//        return img
//    }

    private fun convertToPolarCoordinates(x: Int, y: Int, tetta: Float): Coordinate {
        val thetaInRad = Math.toRadians(tetta.toDouble())
        val polarX = x * cos(thetaInRad) + y * sin(thetaInRad)
        val polarY = -x * sin(thetaInRad) + y * cos(thetaInRad)
        return Coordinate(polarX.toFloat(), polarY.toFloat())
    }

    data class Coordinate(
            val x: Float,
            val y: Float
    )
}
