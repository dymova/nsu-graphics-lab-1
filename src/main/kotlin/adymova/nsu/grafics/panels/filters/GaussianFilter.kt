package adymova.nsu.grafics.panels.filters

import adymova.nsu.grafics.core.ImageContext
import java.awt.GridLayout
import java.awt.image.BufferedImage
import javax.swing.*
import kotlin.math.PI
import kotlin.math.exp

class GaussianFilterPanel(val imageContext: ImageContext) : JPanel() {
    private val gaussianFilter = GaussianFilter()
    private val kernelSizeSlider: JSlider = JSlider(JSlider.HORIZONTAL, 1, 10, 3)
    private val sigmaSlider: JSlider = JSlider(JSlider.HORIZONTAL, 0, 10, 5)

    init {
        layout = GridLayout(0, 1)

        border = BorderFactory.createTitledBorder("Gaussian")

        addApplyButton()

        addSliders()
    }

    private fun addApplyButton() {
        val applyButton = JButton("Apply")
        applyButton.addActionListener {
            val image = imageContext.changedImage ?: return@addActionListener
            gaussianFilter.apply(image, kernelSizeSlider.value, sigmaSlider.value / 10.0f)
            imageContext.notifyImageUpdateListeners()
        }
        add(applyButton)
    }

    private fun addSliders() {
        add(JLabel("sigma, 10^(-1)"))
        sigmaSlider.majorTickSpacing = 1
        sigmaSlider.paintTicks = true
        sigmaSlider.paintLabels = true
        add(sigmaSlider)

        add(JLabel("kernal"))
        kernelSizeSlider.majorTickSpacing = 2
        kernelSizeSlider.paintTicks = true
        kernelSizeSlider.paintLabels = true
        kernelSizeSlider.snapToTicks = true
        add(kernelSizeSlider)
    }

}

class GaussianFilter {
    //todo debug image moving

    fun apply(bufferedImage: BufferedImage, kernelSize: Int, sigma: Float) {
        val kernel = generateKernel(kernelSize, sigma)
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

    private fun generateKernel(size: Int, sigma: Float): Array<FloatArray> {
        val kernel = Array(size, {
            FloatArray(size)
        })

        for (x in 0 until size) {
            for (y in 0 until size) {
                val coef = 2 * sigma * sigma
                kernel[x][y] = exp(-(x * x + y * y) / coef) / (coef * PI).toFloat()
            }
        }

        return kernel
    }
}
