package adymova.nsu.grafics.panels

import javax.swing.JLabel
import javax.swing.JPanel

class ColorFormatPixelPanel(a: String, b: String, c: String) : JPanel() {
    private val aPanel: KeyValuePanel = KeyValuePanel(a)
    private val bPanel: KeyValuePanel = KeyValuePanel(b)
    private val cPanel: KeyValuePanel = KeyValuePanel(c)

    init {
        add(aPanel)
        add(bPanel)
        add(cPanel)
    }

    fun updateValues(aNewValue: String, bNewValue: String, cNewValue: String) {
        aPanel.updateValue(aNewValue)
        bPanel.updateValue(bNewValue)
        cPanel.updateValue(cNewValue)
    }
}

class KeyValuePanel(key: String) : JPanel() {
    private val label: JLabel = JLabel("$key: ")
    private val value: JLabel = JLabel("")

    init {
        this.add(label)
        this.add(value)
    }

    fun updateValue(newValue: String) {
        value.text = newValue
    }
}
