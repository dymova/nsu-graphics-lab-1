import adymova.nsu.grafics.core.Lab
import adymova.nsu.grafics.panels.segmentation.ImageAsLabArrays
import adymova.nsu.grafics.panels.segmentation.Node
import adymova.nsu.grafics.panels.segmentation.PseudoImage
import adymova.nsu.grafics.panels.segmentation.SplitAndMergeSegmentation
import org.assertj.core.api.Assertions.assertThat
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
        SplitAndMergeSegmentation().apply(pseudoImage, 2.0, getTestMetrics())
    }

    private fun getTestMetrics() = { lab1: Lab, lab2: Lab -> if (lab1 == lab2) 0.0 else 100.0 }


    @Test
    fun testFindNeighbors() {
        val pseudoImage = PseudoImage(arrayOf(
                intArrayOf(7, 1, 2, 2),
                intArrayOf(1, 1, 2, 2),
                intArrayOf(3, 3, 4, 4),
                intArrayOf(3, 3, 4, 4)
        ))

        val width = pseudoImage.getWidth()
        val height = pseudoImage.getHeight()
        val imageAsLabArrays = ImageAsLabArrays(pseudoImage)
        val root = Node(0, 0, width, height, imageAsLabArrays, null)
        root.pass { it.splitIfPossible(2.0, getTestMetrics()) }


        checkNeighborsForRoot(root)

        checkNeighborsNearEdge(root)

        checkNeighborsInTheCenter(root)

        checkNeighborsForBigArea(root)
    }

    private fun checkNeighborsForBigArea(root: Node) {
        val neighbors = root.children[1].findNeigbors()
        assertThat(neighbors.bottom).contains(root.children[3])
        assertThat(neighbors.right).isEmpty()
        assertThat(neighbors.top).isEmpty()
        assertThat(neighbors.left).contains(root.children[0].children[1], root.children[0].children[3])
    }

    private fun checkNeighborsInTheCenter(root: Node) {
        val neighbors = root.children[0].children[3].findNeigbors()
        assertThat(neighbors.bottom).contains(root.children[2])
        assertThat(neighbors.left).contains(root.children[0].children[2])
        assertThat(neighbors.top).contains(root.children[0].children[1])
        assertThat(neighbors.right).contains(root.children[1])
    }

    private fun checkNeighborsNearEdge(root: Node) {
        val neighbors = root.children[0].findNeigbors()
        assertThat(neighbors.bottom).contains(root.children[2])
        assertThat(neighbors.left).isEmpty()
        assertThat(neighbors.top).isEmpty()
        assertThat(neighbors.right).contains(root.children[1])
    }

    private fun checkNeighborsForRoot(root: Node) {
        val neighbors = root.findNeigbors()
        assertThat(neighbors.bottom).isEmpty()
        assertThat(neighbors.left).isEmpty()
        assertThat(neighbors.top).isEmpty()
        assertThat(neighbors.right).isEmpty()
    }
}