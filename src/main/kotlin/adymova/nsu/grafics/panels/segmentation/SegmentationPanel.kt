package adymova.nsu.grafics.panels.segmentation

import adymova.nsu.grafics.core.ImageContext
import java.awt.GridLayout
import javax.swing.*

class SegmentationPanel(val imageContext: ImageContext) : JPanel() {
    private val splitAndMergeSegmentation = SplitAndMergeSegmentation()
    private val meanShiftSegmentation = MeanShiftSegmentation()
    private val normalizedCutSegmentation = NormalizedCutSegmentation()

    init {
        addSplitAndMergeSegmentation()
    }

    private fun addSplitAndMergeSegmentation() {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

//        val spinner = JSpinner(SpinnerNumberModel(2.0, 1.0, 10.0, 0.5))

        val splitAndMergeButton = JButton("Apply Split&Merge")
        splitAndMergeButton.addActionListener {
            val image = imageContext.changedImage ?: return@addActionListener
            splitAndMergeSegmentation.apply(
                BufferedImg(image),
//                spinner.value as Double
                2.0
            ) { lab1, lab2 -> computeCiede2000Metrics(lab1, lab2) }
            imageContext.notifyImageUpdateListeners()
        }
//        add(spinner)
        add(splitAndMergeButton)

        val meanShiftButton = JButton("Apply MeanShift")
        meanShiftButton.addActionListener {
            val image = imageContext.changedImage ?: return@addActionListener
            meanShiftSegmentation.apply(BufferedImg(image), 0.01, 20.0001f)
            imageContext.notifyImageUpdateListeners()
        }
        add(meanShiftButton)

        val normalizedCutButton = JButton("Apply NormalizedCut")
        normalizedCutButton.addActionListener {
            val image = imageContext.changedImage ?: return@addActionListener
            normalizedCutSegmentation.apply(BufferedImg(image))
            imageContext.notifyImageUpdateListeners()
        }
        add(normalizedCutButton)
    }
}