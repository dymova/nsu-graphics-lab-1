package adymova.nsu.grafics.panels.colorformats.rgb

import adymova.nsu.grafics.core.ImageContext
import adymova.nsu.grafics.panels.colorformats.ColorFormatPixelPanel
import adymova.nsu.grafics.panels.colorformats.ColorFormatSelectionPanel
import adymova.nsu.grafics.panels.general.rgb
import java.awt.Color
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JPanel
import kotlin.concurrent.thread

class RgbPanel(imageContext: ImageContext) : JPanel() {
    private val rgbSelectionPanel: ColorFormatSelectionPanel = ColorFormatSelectionPanel(imageContext, rgb)
    private val rgbPixelPanel: ColorFormatPixelPanel = ColorFormatPixelPanel("R", "G", "B")

    init {
        layout = GridLayout(0, 1)
        border = BorderFactory.createTitledBorder(rgb)

        add(rgbPixelPanel)
        add(rgbSelectionPanel)
    }

    fun updateSelectionValues() {
        thread { rgbSelectionPanel.updateValues() }
    }

    fun updateCursorValue(rgb: Int) {
        val color = Color(rgb)
        rgbPixelPanel.updateValues(color.red.toString(), color.green.toString(), color.blue.toString())
    }
}