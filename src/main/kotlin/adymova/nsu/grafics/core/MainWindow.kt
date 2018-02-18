package adymova.nsu.grafics.core

import adymova.nsu.grafics.panels.ImagePanel
import adymova.nsu.grafics.panels.SettingsPanel
import adymova.nsu.grafics.panels.middle
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagConstraints.BOTH
import java.awt.GridBagLayout
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.*

class MainWindow : JFrame() {
    private var imageContext: ImageContext = ImageContext()

    private val imagePanel: ImagePanel = ImagePanel(imageContext)
    private val settingsPanel: SettingsPanel = SettingsPanel(imageContext)
    private var mainPanel: JPanel = JPanel()

    private val scrollPane = JScrollPane(imagePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)

    private var fileChooser = JFileChooser()

    init {
        this.title = "Lab1"
        this.isResizable = true
        this.contentPane = mainPanel
        this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        this.minimumSize = Dimension(800, 700)

        createMenu()

        addComponentsToPanel()

        extendedState = JFrame.MAXIMIZED_BOTH
        isUndecorated = true
        this.isVisible = true
    }

    private fun addComponentsToPanel() {
        mainPanel.layout = GridBagLayout()
        val constraints = GridBagConstraints()
        constraints.weightx = 0.8
        constraints.weighty = 1.0
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.fill = BOTH
        this.add(scrollPane, constraints)
        this.revalidate()
        this.repaint()
        constraints.weightx = 0.2
        constraints.weighty = 1.0
        constraints.gridx = 1
        constraints.gridy = 0
        constraints.fill = BOTH
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
            imageContext.notifyChangeImageListeners()
            updateImageContext(bufferedImage)


            imagePanel.preferredSize = Dimension(bufferedImage!!.width, bufferedImage.height)

            imagePanel.repaint()

            mainPanel.revalidate()
            imagePanel.revalidate()
        }
    }

    private fun updateImageContext(bufferedImage: BufferedImage?) {
        imageContext.originalImage = bufferedImage
        imageContext.changedImage = bufferedImage
        imageContext.imageHsv.h = middle.toDouble()
        imageContext.imageHsv.s = middle.toDouble()
        imageContext.imageHsv.v = middle.toDouble()

    }
}

