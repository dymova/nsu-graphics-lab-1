package adymova.nsu.grafics.panels.general

import adymova.nsu.grafics.core.ChangeImageListener
import adymova.nsu.grafics.core.ImageContext
import adymova.nsu.grafics.core.rgbToHsv
import adymova.nsu.grafics.core.rgbToLab
import java.awt.Color
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JFileChooser
import javax.swing.JPanel
import kotlin.concurrent.thread

const val rgb = "RGB"
const val hsv = "HSV"
const val lab = "LAB"


class SaveToFilePanel(private val imageContext: ImageContext) : JPanel(), ChangeImageListener {
    private var fileChooser = JFileChooser()

    private val formats = arrayOf(rgb, hsv, lab)
    private val comboBox: JComboBox<String> = JComboBox(formats)

    private val saveButton = JButton("Save to file")

    init {
        add(comboBox)
        add(saveButton)

        saveButton.addActionListener {
            chooseFile()
        }

        this.saveButton.isEnabled = false
        imageContext.subscribeChangeImageListener(this)

    }

    override fun imageChanged() {
        if (imageContext.originalImage != null) {
            this.saveButton.isEnabled = true
        }
    }

    private fun chooseFile() {
        val returnValue = fileChooser.showDialog(this, "Save")
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            thread {
                writeToFile(comboBox.selectedItem as String)
            }
        }
    }

    private fun writeToFile(format: String) {
        val file = fileChooser.selectedFile
        file.printWriter().use {
            for (y in 0 until imageContext.changedImage!!.height) {
                if (y != 0) {
                    it.append(",")
                }
                it.append("[")
                for (x in 0 until imageContext.changedImage!!.width) {
                    val color = Color(imageContext.changedImage!!.getRGB(x, y))

                    when (format) {
                        rgb -> {
                            it.append("[${color.red}, ${color.green}, ${color.blue}]")
                        }
                        hsv -> {
                            val hsv = rgbToHsv(color)
                            it.append("[${hsv.h}, ${hsv.s}, ${hsv.v}]")
                        }
                        lab -> {
                            val lab = rgbToLab(color)
                            it.append("[${lab.l}, ${lab.a}, ${lab.b}]")
                        }
                    }
                }
                it.append("]")
            }
        }
    }

}
