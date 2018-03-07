package adymova.nsu.grafics.panels.general.lab

import adymova.nsu.grafics.core.ImageContext
import adymova.nsu.grafics.core.rgbToLab
import adymova.nsu.grafics.panels.general.ColorFormatPixelPanel
import adymova.nsu.grafics.panels.general.ColorFormatSelectionPanel
import adymova.nsu.grafics.panels.general.lab
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JPanel


class LabPanel(imageContext: ImageContext) : JPanel() {
    private val selectionPanel: ColorFormatSelectionPanel = ColorFormatSelectionPanel(imageContext, lab)
    private val pixelPanel: ColorFormatPixelPanel = ColorFormatPixelPanel("L", "A", "B")
    private val histogramPanel = HistogramPanel(imageContext)

    init {
        layout = GridLayout(0, 1)
        border = BorderFactory.createTitledBorder(lab)

        val constraints = GridBagConstraints()
        constraints.weightx = 1.0
        constraints.weighty = 0.10
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.fill = GridBagConstraints.BOTH
        add(pixelPanel, constraints)

        constraints.weightx = 1.0
        constraints.weighty = 0.30
        constraints.gridx = 0
        constraints.gridy = 1
        constraints.fill = GridBagConstraints.BOTH
        add(selectionPanel, constraints)

        constraints.weightx = 1.0
        constraints.weighty = 0.60
        constraints.gridx = 0
        constraints.gridy = 2
        constraints.fill = GridBagConstraints.BOTH
        add(histogramPanel, constraints)
    }

    fun updateSelectionValues() {
        selectionPanel.updateValues()
    }

    fun updateCursorValue(rgb: Int) {
        val color = Color(rgb)
        val lab = rgbToLab(color)
        pixelPanel.updateValues(String.format("%.2f", lab.l), String.format("%.2f", lab.a), String.format("%.2f", lab.b))
    }

}

