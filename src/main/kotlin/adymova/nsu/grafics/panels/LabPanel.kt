package adymova.nsu.grafics.panels

import adymova.nsu.grafics.core.ImageContext
import adymova.nsu.grafics.core.rgbToLab
import java.awt.Color
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JPanel

class LabPanel(private val imageContext: ImageContext) : JPanel() {
    private val selectionPanel: ColorFormatSelectionPanel = ColorFormatSelectionPanel(imageContext, lab)
    private val pixelPanel: ColorFormatPixelPanel = ColorFormatPixelPanel("L", "A", "B")

    init {
        layout = GridLayout(0, 1)
        border = BorderFactory.createTitledBorder(lab)

        add(pixelPanel)
        add(selectionPanel)
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