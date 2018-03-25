import adymova.nsu.grafics.panels.segmentation.PseudoImage
import adymova.nsu.grafics.panels.segmentation.SplitAndMergeSegmentation
import org.junit.jupiter.api.Test

class SplitAndMergeTest {

    @Test
    fun testSplitAndMerge() {
        val pseudoImage = PseudoImage(arrayOf(
                intArrayOf(7, 1, 2, 2),
                intArrayOf(1, 1, 2, 2),
                intArrayOf(3, 3, 4, 4),
                intArrayOf(3, 3, 4, 4)
        ))
        SplitAndMergeSegmentation().apply(pseudoImage, 2.0) { lab1, lab2 -> if (lab1 == lab2) 0.0 else 100.0 }
    }
}