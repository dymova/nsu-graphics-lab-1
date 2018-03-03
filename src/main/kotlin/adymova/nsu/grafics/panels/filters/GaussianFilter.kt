package adymova.nsu.grafics.panels.filters

import java.awt.image.BufferedImage
import kotlin.math.PI
import kotlin.math.exp

class GaussianFilter {
    fun apply(bufferedImage: BufferedImage, kernelRadius: Int) {
        val kernel = generateKernel(kernelRadius)

        val intermediateImage = createIntermediateImage(bufferedImage, kernelRadius)

        for (x in )

    }

    private fun generateKernel(kernelRadius: Int): Array<FloatArray> {
        val kernel = arrayOf(
                FloatArray(kernelRadius),
                FloatArray(kernelRadius),
                FloatArray(kernelRadius)
        )

        val sigma = 1.0f

        for (x in 0 until kernelRadius) {
            for (y in 0 until kernelRadius) {
                val coef = 2 * sigma * sigma
                kernel[x][y] = exp(-(x * x + y * y) / coef) / (coef * PI).toFloat()
            }
        }

        return kernel
    }
}

inline fun convolveChanel(
        matrix: Array<FloatArray>,
        img: BufferedImage,
        x: Int,
        y: Int,
        chanelType: ChanelType,
        radius: Int
) : Float {
    var sum = 0f
    for (currentY in (y - radius.. y + radius)) {
        for (currentX in (x - radius.. x + radius)) {
            val rgb = img.getRGB(currentX, currentY)
            val chanelValue = selectChanel(chanelType, rgb)
            val matrixX = currentX - x + radius
            val matrixY = currentY - y + radius
            val kernelVal = matrix[matrixX][matrixY]
            sum += kernelVal * chanelValue
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