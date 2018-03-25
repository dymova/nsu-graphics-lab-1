package adymova.nsu.grafics.panels.segmentation

import adymova.nsu.grafics.core.ImageContext
import javax.swing.JButton
import javax.swing.JPanel

class SegmentationPanel(val imageContext: ImageContext) : JPanel() {
    private val splitAndMergeSegmentation = SplitAndMergeSegmentation()
    private val maxDifference = 0.1

    init {
        addApplyButton()
    }

    private fun addApplyButton() {
        val applyButton = JButton("Apply")
        applyButton.addActionListener {
            val image = imageContext.changedImage ?: return@addActionListener
            splitAndMergeSegmentation.apply(BufferedImg (image), maxDifference) { lab1, lab2 -> computeCiede2000Metrics(lab1, lab2) }
            imageContext.notifyImageUpdateListeners()
        }
        add(applyButton)
    }
}