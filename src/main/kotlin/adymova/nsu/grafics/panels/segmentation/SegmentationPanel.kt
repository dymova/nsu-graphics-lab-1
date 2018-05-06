package adymova.nsu.grafics.panels.segmentation

import adymova.nsu.grafics.core.ImageContext
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class SegmentationPanel(val imageContext: ImageContext) : JPanel() {
    private val splitAndMergeSegmentation = SplitAndMergeSegmentation()

    init {
        addSplitAndMergeSegmentation()
    }

    private fun addSplitAndMergeSegmentation() {
        val spinner = JSpinner(SpinnerNumberModel(2.0, 1.0, 10.0, 0.5))

        val applyButton = JButton("Apply Split&Merge")
        applyButton.addActionListener {
            val image = imageContext.changedImage ?: return@addActionListener
            splitAndMergeSegmentation.apply(BufferedImg (image), spinner.value as Double) { lab1, lab2 -> computeCiede2000Metrics(lab1, lab2) }
            imageContext.notifyImageUpdateListeners()
        }
        add(spinner)
        add(applyButton)
    }
}