import adymova.nsu.grafics.panels.segmentation.ImageAsLabArrays
import adymova.nsu.grafics.panels.segmentation.NormalizedCutSegmentation
import adymova.nsu.grafics.panels.segmentation.PseudoImage
import org.junit.jupiter.api.Test

class NCutTest {
    @Test
    fun testNCut() {
        val pseudoImage = PseudoImage(
            arrayOf(
                intArrayOf(1, 1),
                intArrayOf(2, 2)
            )
        )

        val width = pseudoImage.getWidth()
        val height = pseudoImage.getHeight()
        val weightsMatrix = NormalizedCutSegmentation().buildWeightsMatrix(
            width * height,
            width,
            height,
            { lab1, lab2 -> if(lab1== lab2) 0.0 else    1.0 },
            ImageAsLabArrays(pseudoImage)
        )
        println( weightsMatrix.toString())
    }
}