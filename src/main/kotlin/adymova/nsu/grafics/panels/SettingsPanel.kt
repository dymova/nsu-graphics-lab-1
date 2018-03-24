package adymova.nsu.grafics.panels

import adymova.nsu.grafics.core.ImageContext
import adymova.nsu.grafics.panels.filters.FilterPanel
import adymova.nsu.grafics.panels.general.GeneralSettingsPanel
import adymova.nsu.grafics.panels.segmentation.SegmentationPanel
import javax.swing.JTabbedPane

class SettingsPanel(imageContext: ImageContext) : JTabbedPane() {
    init {
        addTab("General", GeneralSettingsPanel(imageContext))
        addTab("Filters", FilterPanel(imageContext))
        addTab("Segmentation", SegmentationPanel(imageContext))
    }
}