package adymova.nsu.grafics.panels.filters

import adymova.nsu.grafics.core.ImageContext
import javax.swing.JPanel

class FilterPanel(val imageContext: ImageContext) : JPanel() {
    val sobelFilterPanel = SobelFilterPanel(imageContext)

    init {
        add(sobelFilterPanel)
    }
}




