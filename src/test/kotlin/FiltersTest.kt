import adymova.nsu.grafics.panels.filters.fillEdges
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.Test
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB

class FiltersTest {

    @Test
    fun testFillEdges() {
        val bufferedImage = BufferedImage(3, 3, TYPE_INT_RGB)
        var i = 0
        for (y in 0 until bufferedImage.height) {

            for (x in 0 until bufferedImage.width) {
                bufferedImage.setRGB(x, y, i++)
            }
        }
        val kernelRadius = 5/2
        val newImage = BufferedImage(bufferedImage.width + kernelRadius * 2,
                bufferedImage.height + kernelRadius * 2,
                BufferedImage.TYPE_INT_RGB)
        fillEdges(newImage, bufferedImage, kernelRadius)
    }
}