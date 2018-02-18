import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagConstraints.BOTH
import java.awt.GridBagLayout
import java.awt.Rectangle
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.*

class MainWindow : JFrame() {
    private var imageContext: ImageContext = ImageContext()

    private val imagePanel: ImagePanel = ImagePanel(imageContext)
    private val saveToFilePanel: SaveToFilePanel = SaveToFilePanel(imageContext)
    private val settingsPanel: SettingsPanel = SettingsPanel(imageContext)
    private var mainPanel: JPanel = JPanel()

    private var fileChooser = JFileChooser()

    private val changeImageListeners: MutableList<ChangeImageListener> = mutableListOf(saveToFilePanel)

    init {
        this.title = "Lab1"
        this.isResizable = true
        this.contentPane = mainPanel
        this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        this.minimumSize = Dimension(750, 500)

        createMenu()

        addComponentsToPanel()

        imagePanel.subscribeSelectionListener(settingsPanel)
        imagePanel.subscribeMousePositionListener(settingsPanel)

        this.isVisible = true
    }

    private fun addComponentsToPanel() {
        settingsPanel.add(saveToFilePanel)
        val scrollPane = JScrollPane(imagePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)

        mainPanel.layout = GridBagLayout()
        val constraints = GridBagConstraints()
        constraints.weightx = 1.0
        constraints.weighty = 1.0
        constraints.gridx = 0
        constraints.gridy = 0
//        constraints.fill = BOTH
        this.add(scrollPane, constraints)
        this.revalidate()
        this.repaint()
        constraints.weightx = 0.3
        constraints.weighty = 1.0
        constraints.gridx = 1
        constraints.gridy = 0
//        constraints.fill = BOTH
        this.add(settingsPanel, constraints)
    }

    private fun createMenu() {
        val menubar = JMenuBar()
        val fileMenuItem = JMenu("File")
        val eMenuItem = JMenuItem("Open")
        eMenuItem.toolTipText = "Open file"
        eMenuItem.addActionListener {
            openFile()
        }
        fileMenuItem.add(eMenuItem)

        menubar.add(fileMenuItem)

        jMenuBar = menubar
    }

    private fun openFile() {
        val returnValue = fileChooser.showOpenDialog(mainPanel)
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            val bufferedImage = ImageIO.read(file)
            if (bufferedImage == null) {
                //todo handle error
            }
            imageContext.image = bufferedImage
            imagePanel.preferredSize = Dimension(bufferedImage!!.width, bufferedImage.height)

            imagePanel.repaint()

            changeImageListeners.forEach { it.imageChanged() }

            mainPanel.revalidate()
            imagePanel.revalidate()
        }
    }
}

interface ChangeImageListener {
    fun imageChanged()
}

class ImageContext {
    var image: BufferedImage? = null
    var selection: Rectangle? = null
}