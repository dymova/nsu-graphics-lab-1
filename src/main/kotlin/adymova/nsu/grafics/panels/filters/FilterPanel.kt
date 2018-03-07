package adymova.nsu.grafics.panels.filters

import adymova.nsu.grafics.core.ImageContext
import java.awt.GridLayout
import javax.swing.JPanel

class FilterPanel(val imageContext: ImageContext) : JPanel() {
    private val sobelFilterPanel = SobelFilterPanel(imageContext)
    private val gaussianFilterPanel = GaussianFilterPanel(imageContext)
    private val gaborFilterPanel = GaborFilterPanel(imageContext)

    init {
        layout = GridLayout(0, 1)

        add(sobelFilterPanel)
        add(gaussianFilterPanel)
        add(gaborFilterPanel)
    }
}




