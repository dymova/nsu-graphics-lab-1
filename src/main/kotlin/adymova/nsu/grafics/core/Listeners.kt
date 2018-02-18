package adymova.nsu.grafics.core

import java.awt.event.MouseEvent

interface ChangeSelectionListener {
    fun selectionChanged()
}

interface MousePositionChangedListener {
    fun mouseMoved(e: MouseEvent)
}

interface ChangeHsvListener {
    fun imageHsvChanged()
}

interface ChangeImageListener {
    fun imageChanged()
}