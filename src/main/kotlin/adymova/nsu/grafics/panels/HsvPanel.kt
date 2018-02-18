package adymova.nsu.grafics.panels

import adymova.nsu.grafics.core.ImageContext
import adymova.nsu.grafics.core.rgbToHsv
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JSlider
import kotlin.concurrent.thread

class HvsPanel(imageContext: ImageContext) : JPanel() {
    private val hsvPixelPanel: ColorFormatPixelPanel = ColorFormatPixelPanel("H", "S", "V")
    private val hsvSelectionPanel: ColorFormatSelectionPanel = ColorFormatSelectionPanel(imageContext, hsv)
    private val changeImageHsvPanel = ChangeImageHsvPanel()

    init {
        layout = GridBagLayout()
        border = BorderFactory.createTitledBorder(hsv)

        val constraints = GridBagConstraints()
        constraints.weightx = 1.0
        constraints.weighty = 0.10
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.fill = GridBagConstraints.BOTH
        add(hsvPixelPanel, constraints)

        constraints.weightx = 1.0
        constraints.weighty = 0.40
        constraints.gridx = 0
        constraints.gridy = 1
        constraints.fill = GridBagConstraints.BOTH
        add(hsvSelectionPanel, constraints)

        constraints.weightx = 1.0
        constraints.weighty = 0.50
        constraints.gridx = 0
        constraints.gridy = 2
        constraints.fill = GridBagConstraints.BOTH
        add(changeImageHsvPanel, constraints)

    }

    fun updateSelectionValues() {
        thread { hsvSelectionPanel.updateValues() }
    }

    fun updateCursorValue(rgb: Int) {
        val color = Color(rgb)
        val hsv = rgbToHsv(color)
        hsvPixelPanel.updateValues(String.format("%.2f", hsv.h), String.format("%.2f", hsv.s), String.format("%.2f", hsv.v))
    }
}

class ChangeImageHsvPanel :JPanel() {
    private val hSlider: JSlider = JSlider(JSlider.VERTICAL, 0, 360, 180)
    private val sSlider: JSlider = JSlider(JSlider.VERTICAL, 0, 100, 50)
    private val vSlider: JSlider = JSlider(JSlider.VERTICAL, 0, 100, 50)
//    private val applyButton: JButton = JButton("Apply")

    init {
        layout = GridLayout(1, 0)

        hSlider.minorTickSpacing = 20
        hSlider.majorTickSpacing = 120
        hSlider.paintTicks = true
        hSlider.paintLabels = true
        hSlider.toolTipText = "H"
        hSlider.name= "H"
        add(hSlider)

        sSlider.minorTickSpacing = 10
        sSlider.majorTickSpacing = 25
        sSlider.paintTicks = true
        sSlider.paintLabels = true
        hSlider.toolTipText = "S"
        add(sSlider)

        vSlider.minorTickSpacing = 10
        vSlider.majorTickSpacing = 25
        vSlider.paintTicks = true
        vSlider.paintLabels = true
        hSlider.toolTipText = "V"
        add(vSlider)

//        add(applyButton)

    }
}