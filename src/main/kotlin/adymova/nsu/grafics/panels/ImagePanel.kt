package adymova.nsu.grafics.panels

import adymova.nsu.grafics.core.ChangeHsvListener
import adymova.nsu.grafics.core.ImageContext
import adymova.nsu.grafics.core.rgbToHsv
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel


class ImagePanel(private val imageContext: ImageContext) : JPanel(), ChangeHsvListener {
    private var clickPoint: Point? = null


    init {
        val handler = object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                clickPoint = e.point
                imageContext.selection = Rectangle(e.point)
                repaint()
            }

            override fun mouseDragged(e: MouseEvent) {
                val dragPoint = e.point
                val x = Math.min(clickPoint!!.x, dragPoint.x)
                val y = Math.min(clickPoint!!.y, dragPoint.y)
                val width = Math.max(clickPoint!!.x - dragPoint.x, dragPoint.x - clickPoint!!.x)
                val height = Math.max(clickPoint!!.y - dragPoint.y, dragPoint.y - clickPoint!!.y)
                imageContext.selection = Rectangle(x, y, width, height)
                repaint()
            }

            override fun mouseReleased(e: MouseEvent) {
                clickPoint = null
                imageContext.notifySelectionListeners()
            }

            override fun mouseMoved(e: MouseEvent) {
                imageContext.notifyMousePositionListener(e)
            }

        }
        addMouseListener(handler)
        addMouseMotionListener(handler)

        imageContext.subscribeChangeHsvListener(this)
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        if (imageContext.changedImage != null) {
            val g2d = g.create() as Graphics2D
            g2d.drawImage(imageContext.changedImage, 0, 0, this)
//            this.preferredSize = Dimension(imageContext.image!!.width, imageContext.image!!.height)

            if (imageContext.selection != null) {
                g2d.color = Color(225, 225, 255, 128)
                g2d.fill(imageContext.selection)
                g2d.color = Color.GRAY
                g2d.draw(imageContext.selection)
            }
            g2d.dispose()

//            mainWindow.revalidate()
        }
    }

    override fun imageHsvChanged() {
        for (y in 0 until imageContext.originalImage!!.height) {
            for (x in 0 until imageContext.originalImage!!.width) {
                val rgb = Color(imageContext.originalImage!!.getRGB(x, y))
                val hsv = rgbToHsv(rgb)

                hsv.h = getNewValue(imageContext.imageHsv.h, hsv.h)
                hsv.s = getNewValue(imageContext.imageHsv.s, hsv.s)
                hsv.v = getNewValue(imageContext.imageHsv.v, hsv.v)

                val newRgb = hsv.toRgb()
                imageContext.changedImage!!.setRGB(x, y, newRgb.rgb)
            }
        }
        repaint()
    }

    fun getNewValue(sliderValue: Double, currentValue: Double): Double {
        return when {
            sliderValue < middle -> sliderValue / middle * currentValue
            sliderValue > middle -> ((sliderValue / middle) - 1) * (100 - currentValue) + currentValue
            else -> currentValue
        }
    }

}

