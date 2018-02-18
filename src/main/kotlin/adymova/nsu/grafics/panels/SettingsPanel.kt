package adymova.nsu.grafics.panels

import adymova.nsu.grafics.core.ImageContext
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.MouseEvent
import javax.swing.JPanel


class SettingsPanel(private val imageContext: ImageContext, saveToFilePanel: SaveToFilePanel) : JPanel(), ChangeSelectionListener,
        MousePositionChangedListener {

    private val xyPanel = XyPanel()

    private val hsvPanel = HvsPanel(imageContext)
    private val rgbPanel = RgbPanel(imageContext)
    private val labPanel = LabPanel(imageContext)


    init {
        this.minimumSize = Dimension(160, 700)
        this.preferredSize = Dimension(160, 700)
        this.maximumSize = Dimension(160, 700)


//        this.layout = GridLayout(0, 1)
//        add(xyPanel)
//        add(rgbPanel)
//        add(hsvPanel)
//        add(labPanel)

        layout = GridBagLayout()
        val constraints = GridBagConstraints()
        constraints.weightx = 1.0
        constraints.weighty = 0.05
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.fill = GridBagConstraints.BOTH
        add(xyPanel, constraints)

        constraints.weightx = 1.0
        constraints.weighty = 0.2
        constraints.gridx = 0
        constraints.gridy = 1
        constraints.fill = GridBagConstraints.BOTH
        add(rgbPanel, constraints)

        constraints.weightx = 1.0
        constraints.weighty = 0.30
        constraints.gridx = 0
        constraints.gridy = 2
        constraints.fill = GridBagConstraints.BOTH
        add(hsvPanel, constraints)

        constraints.weightx = 1.0
        constraints.weighty = 0.40
        constraints.gridx = 0
        constraints.gridy = 3
        constraints.fill = GridBagConstraints.BOTH
        add(labPanel, constraints)

        constraints.weightx = 1.0
        constraints.weighty = 0.05
        constraints.gridx = 0
        constraints.gridy = 4
        constraints.fill = GridBagConstraints.BOTH
        add(saveToFilePanel, constraints)

    }

    private fun updateSettings(x: Int, y: Int, rgb: Int) {
        rgbPanel.updateCursorValue(rgb)
        hsvPanel.updateCursorValue(rgb)
        labPanel.updateCursorValue(rgb)

        xyPanel.updateCursorValue(x, y)

        //        mainWindow.repaint()
    }

    override fun mouseMoved(e: MouseEvent) {
        val x = e.x
        val y = e.y
        if (imageContext.image != null && x < imageContext.image!!.width && y < imageContext.image!!.height) {
            updateSettings(x, y, imageContext.image!!.getRGB(x, y))
        }

    }


    override fun selectionChanged() {
        rgbPanel.updateSelectionValues()
        hsvPanel.updateSelectionValues()
        labPanel.updateSelectionValues()
    }
}

class XyPanel : JPanel() {
    private val xPanel: KeyValuePanel = KeyValuePanel("X")
    private val yPanel: KeyValuePanel = KeyValuePanel("Y")

    init {
        add(xPanel)
        add(yPanel)
    }

    fun updateCursorValue(x: Int, y: Int) {
        xPanel.updateValue(x.toString())
        yPanel.updateValue(y.toString())
    }
}
