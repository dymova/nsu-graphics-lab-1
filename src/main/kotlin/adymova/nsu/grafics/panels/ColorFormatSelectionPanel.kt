package adymova.nsu.grafics.panels

import adymova.nsu.grafics.core.ImageContext
import adymova.nsu.grafics.core.rgbToHsv
import adymova.nsu.grafics.core.rgbToLab
import java.awt.Color
import javax.swing.*

class ColorFormatSelectionPanel(private val imageContext: ImageContext, private val format: String) : JPanel() {

    private val outputArea: JTextArea = JTextArea()

    init {
        this.layout = BoxLayout(this, BoxLayout.X_AXIS)
        val scrollPane = JScrollPane(outputArea)
//        this.border = BorderFactory.createTitledBorder(format)
        outputArea.isEditable = false
        outputArea.lineWrap = true
        add(scrollPane)
    }


    fun updateValues() {
        if (imageContext.selection != null && imageContext.originalImage != null) {
            outputArea.text = ""
            val stringBuilder = StringBuilder()
            val selectionY = imageContext.selection!!.y
            val selectionX = imageContext.selection!!.x
            for (y in selectionY until imageContext.selection!!.height + selectionY) {
                if (y != selectionY) {
                    stringBuilder.append(",")
                }
                stringBuilder.append("[")
                for (x in selectionX until imageContext.selection!!.width + selectionX) {
                    if (x != selectionX) {
                        stringBuilder.append(",")
                    }
                    val color = Color(imageContext.changedImage!!.getRGB(x, y))
                    when (format) {
                        rgb -> {
                            stringBuilder.append("[${color.red}; ${color.green}; ${color.blue}]")
                        }
                        hsv -> {
                            val hsv = rgbToHsv(color)
                            stringBuilder.append("[${String.format("%.2f", hsv.h)}; ${String.format("%.2f", hsv.s)}; ${String.format("%.2f", hsv.v)}]")
                        }
                        lab -> {
                            val lab = rgbToLab(color)
                            stringBuilder.append("[${String.format("%.2f", lab.l)}; ${String.format("%.2f", lab.a)}; ${String.format("%.2f", lab.b)}]")
                        }
                    }
                }
                stringBuilder.append("]")
            }
            outputArea.text = stringBuilder.toString()
            revalidate()
            repaint()
        }
    }

}