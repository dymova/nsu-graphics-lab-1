package adymova.nsu.grafics.panels.general.lab

import adymova.nsu.grafics.core.ChangeHsvListener
import adymova.nsu.grafics.core.ImageContext
import adymova.nsu.grafics.core.rgbToLab
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JRadioButton

class HistogramPanel(private val imageContext: ImageContext) : JPanel(), ChangeHsvListener {
    private var histogram: Histogram? = null
    private val panelHeight = 100
    private val panelWidth = 200
    private val bucketBoxWidth = panelWidth / stepsCount
    var lSelectButton = JRadioButton("L")
    var aSelectButton = JRadioButton("A")
    var bSelectButton = JRadioButton("B")

    init {
        size = Dimension(panelWidth, panelHeight)
        imageContext.subscribeChangeHsvListener(this)

        val group = ButtonGroup()
        group.add(lSelectButton)
        group.add(aSelectButton)
        group.add(bSelectButton)
        lSelectButton.isSelected = true
        add(lSelectButton)
        add(aSelectButton)
        add(bSelectButton)

        lSelectButton.addActionListener { updateHistogram() }
        aSelectButton.addActionListener { updateHistogram() }
        bSelectButton.addActionListener { updateHistogram() }
    }

    private fun updateHistogram() {
        histogram = Histogram(imageToDoubleArray())
        repaint()

    }

    private fun imageToDoubleArray(): DoubleArray {
        val image = imageContext.changedImage
        image ?: return doubleArrayOf()
        val result = DoubleArray(image.width * image.height)
        for (y in 0 until imageContext.changedImage!!.height) {
            for (x in 0 until imageContext.changedImage!!.width) {
                val rgb = Color(imageContext.changedImage!!.getRGB(x, y))
                val lab = rgbToLab(rgb)

                when {
                    lSelectButton.isSelected -> result[y * image.width + x] = lab.l
                    aSelectButton.isSelected -> result[y * image.width + x] = lab.a
                    bSelectButton.isSelected -> result[y * image.width + x] = lab.b
                }
            }
        }
        return result
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        val histogramVal = histogram
        histogramVal ?: return
        val buckets = histogramVal.buckets
        val maxSize = buckets.max() ?: return
        for ((index, bucketSize) in buckets.withIndex()) {
            drawBucket(index, bucketSize, g, maxSize)
        }
    }

    private fun drawBucket(bucketIndex: Int, bucketSize: Int, g: Graphics, maxSize: Int) {
        val bucketHeight = Math.round(bucketSize.toDouble() / maxSize * panelHeight).toInt()
        val x = bucketIndex * bucketBoxWidth
        val y = panelHeight - bucketHeight
        g.drawRect(x, y, bucketBoxWidth, bucketHeight)
    }

    override fun imageHsvChanged() {
        updateHistogram()
    }
}

const val stepsCount = 40

class Histogram(values: DoubleArray) {
    val max = values.max() ?: throw IllegalStateException()
    val min = values.min() ?: throw IllegalStateException()
    val diff = max - min

    val step = diff / stepsCount

    val buckets = IntArray(stepsCount)

    init {
        for (value in values) {
            var bucketIndex = Math.floor((value - min) / step).toInt()
            if (bucketIndex >= stepsCount) {
                bucketIndex = stepsCount - 1
            }
            buckets[bucketIndex]++
        }
    }
}
