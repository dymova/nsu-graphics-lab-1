package adymova.nsu.grafics.core

import adymova.nsu.grafics.panels.colorformats.hsv.middle
import java.awt.Rectangle
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage

class ImageContext {
    var originalImage: BufferedImage? = null
    var selection: Rectangle? = null
    var changedImage: BufferedImage? = null
    var imageHsv: Hsv = Hsv(middle.toDouble(), middle.toDouble(), middle.toDouble())

    private var changeHsvListeners: MutableSet<ChangeHsvListener> = mutableSetOf()
    private val changeImageListeners: MutableList<ChangeImageListener> = mutableListOf()
    private var selectionListeners: MutableSet<ChangeSelectionListener> = mutableSetOf()
    private var mousePositionListeners: MutableSet<MousePositionChangedListener> = mutableSetOf()
    private var imageUpdateListeners: MutableSet<ImageUpdateListener> = mutableSetOf()


    fun subscribeChangeHsvListener(changeHsvListener: ChangeHsvListener) {
        changeHsvListeners.add(changeHsvListener)
    }

    fun notifyHsvListeners() {
        changeHsvListeners.forEach { it.imageHsvChanged() }
    }

    fun subscribeChangeImageListener(listener: ChangeImageListener) {
        changeImageListeners.add(listener)
    }

    fun notifyChangeImageListeners() {
        changeImageListeners.forEach { it.imageChanged() }
    }

    fun subscribeSelectionListener(listener: ChangeSelectionListener) {
        selectionListeners.add(listener)
    }

    fun notifySelectionListeners() {
        selectionListeners.forEach { it.selectionChanged() }
    }

    fun subscribeMousePositionListener(listener: MousePositionChangedListener) {
        mousePositionListeners.add(listener)
    }

    fun notifyMousePositionListener(e: MouseEvent) {
        mousePositionListeners.forEach { it.mouseMoved(e) }
    }

    fun subscribeImageUpdateListener(listener: ImageUpdateListener) {
        imageUpdateListeners.add(listener)
    }

    fun notifyImageUpdateListeners() {
        imageUpdateListeners.forEach { it.imageUpdated() }
    }
}