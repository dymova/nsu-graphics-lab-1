import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*

class ColorFormatSelectionPanel(private val imageContext: ImageContext, private val format: String) : JPanel() {

    private val outputArea: JTextArea = JTextArea()

    init {
//        this.layout = BoxLayout(this, BoxLayout.X_AXIS)
        val scrollPane = JScrollPane(outputArea)
        this.border = BorderFactory.createTitledBorder(format)
        outputArea.isEditable = false
        outputArea.lineWrap = true
        add(scrollPane)
    }


    fun updateValues() {
        if (imageContext.selection != null && imageContext.image != null)  {

            val stringBuilder = StringBuilder()
            val selectionY = imageContext.selection!!.y
            val selectionX = imageContext.selection!!.x
            for (y in selectionY until imageContext.selection!!.height + selectionY) {
                stringBuilder.append("[")
                if (y != selectionY) {
                    stringBuilder.append(",")
                }
                for (x in selectionX until imageContext.selection!!.width + selectionX) {
                    val color = Color(imageContext.image!!.getRGB(x, y))
                    when (format) {
                        rgb -> {
                            stringBuilder.append("[${color.red}, ${color.green}, ${color.blue}]")
                        }
                        hsv -> {
                            val hsv = rgbToHsv(color)
                            stringBuilder.append("[${hsv.h}, ${hsv.s}, ${hsv.v}]")
                        }
                        lab -> {
                            val lab = rgbToLab(color)
                            stringBuilder.append("[${lab.l}, ${lab.a}, ${lab.b}]")
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