import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel


class ImagePanel(private val imageContext: ImageContext) : JPanel() {
    private var clickPoint: Point? = null

    private var selectionListeners: MutableSet<ChangeSelectionListener> = mutableSetOf()
    private var mousePositionListeners: MutableSet<MousePositionChangedListener> = mutableSetOf()

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
                selectionListeners.forEach { it.selectionChanged() }
            }

            override fun mouseMoved(e: MouseEvent) {
                mousePositionListeners.forEach { it.mouseMoved(e) }
            }

        }
        addMouseListener(handler)
        addMouseMotionListener(handler)
    }

    fun subscribeSelectionListener(listener: ChangeSelectionListener) {
        selectionListeners.add(listener)
    }

    fun subscribeMousePositionListener(listener: MousePositionChangedListener) {
        mousePositionListeners.add(listener)
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        if (imageContext.image != null) {
            val g2d = g.create() as Graphics2D
            g2d.drawImage(imageContext.image, 0, 0, this)
            this.preferredSize = Dimension(imageContext.image!!.width, imageContext.image!!.height)

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

}

interface ChangeSelectionListener {
    fun selectionChanged()
}

interface MousePositionChangedListener {
    fun mouseMoved(e: MouseEvent)
}

