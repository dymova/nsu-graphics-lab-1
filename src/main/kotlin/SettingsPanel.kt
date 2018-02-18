import java.awt.Color
import java.awt.GridLayout
import java.awt.event.MouseEvent
import javax.swing.JPanel


class SettingsPanel(private val imageContext: ImageContext) : JPanel(), ChangeSelectionListener,
        MousePositionChangedListener {


    private val xPanel: KeyValuePanel = KeyValuePanel("X")
    private val yPanel: KeyValuePanel = KeyValuePanel("Y")

    private val rgbPixelPanel: ColorFormatPixelPanel = ColorFormatPixelPanel("R", "G", "B")

    private val hsvPixelPanel: ColorFormatPixelPanel = ColorFormatPixelPanel("H", "S", "V")
    private val labPixelPanel: ColorFormatPixelPanel = ColorFormatPixelPanel("L", "A", "B")

    private val rgbSelectionPanel: ColorFormatSelectionPanel = ColorFormatSelectionPanel(imageContext, rgb)
    private val hsvSelectionPanel: ColorFormatSelectionPanel = ColorFormatSelectionPanel(imageContext, hsv)
    private val labSelectionPanel: ColorFormatSelectionPanel = ColorFormatSelectionPanel(imageContext, lab)


    init {
        this.layout = GridLayout(0, 1)
        this.add(xPanel)
        this.add(yPanel)

        this.add(rgbPixelPanel)
        this.add(hsvPixelPanel)
        this.add(labPixelPanel)

        add(rgbSelectionPanel)
        add(hsvSelectionPanel)
        add(labSelectionPanel)
    }

    private fun updateSettings(x: Int, y: Int, rgb: Int) {
        xPanel.updateValue(x.toString())
        yPanel.updateValue(y.toString())

        val color = Color(rgb)
        rgbPixelPanel.updateValues(color.red.toString(), color.green.toString(), color.blue.toString())

        val hsv = rgbToHsv(color)
        hsvPixelPanel.updateValues(String.format("%.2f", hsv.h), String.format("%.2f", hsv.s), String.format("%.2f", hsv.v))

        val lab = rgbToLab(color)
        labPixelPanel.updateValues(String.format("%.2f", lab.l), String.format("%.2f", lab.a), String.format("%.2f", lab.b))
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
        rgbSelectionPanel.updateValues()
        labSelectionPanel.updateValues()
        hsvSelectionPanel.updateValues()
    }
}
