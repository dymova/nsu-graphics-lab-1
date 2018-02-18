package adymova.nsu.grafics.core

import java.awt.Color

fun rgbToHsv(rgb: Color): Hsv {
    val r = rgb.red
    val g = rgb.green
    val b = rgb.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)

    var h = when {
        max == r && g >= b -> 60 * (g - b) / (max - min).toDouble()
        max == r && g < b -> 60 * (g - b) / (max - min).toDouble() + 360
        max == g -> 60 * (b - r) / (max - min).toDouble() + 120
        max == b -> 60 * (r - g) / (max - min).toDouble() + 240
        else -> throw IllegalStateException()
    }

    if (h.isNaN()) {
        h = 360.0
    }

    var s = 1 - min / max.toDouble()

    if (s.isNaN()) {
        s = 1.0
    }
    val v = max.toDouble() * 100 / 255.0

    return Hsv(h, s, v)
}

fun rgbToXyz(rgb: Color): Xyz {
    val r = rgb.red
    val g = rgb.green
    val b = rgb.blue

    val xyzVector = arrayOf(
            doubleArrayOf(r.toDouble()),
            doubleArrayOf(g.toDouble()),
            doubleArrayOf(b.toDouble())
    )

    val m = arrayOf(
            doubleArrayOf(0.5767309, 0.1855540, 0.1881852),
            doubleArrayOf(0.2973769, 0.6273491, 0.0752741),
            doubleArrayOf(0.270343, 0.0706872, 0.9911085)
    )

    val product = multiplyMatrices(m, xyzVector, 3, 1)

    return Xyz(product[0][0], product[1][0], product[2][0])
}

private fun xyzToLab(x: Double, y: Double, z: Double): Lab {
    val xn = 0.31382
    val yn = 0.331
    val zn = 0.35518

    val l = 116 * f(y / yn) - 16
    val a = 500 * (f(x / xn) - f(y / yn))
    val b = 200 * (f(y / yn) - f(z / zn))

    return Lab(l, a, b)
}

fun rgbToLab(color: Color): Lab {
    val xyz = rgbToXyz(color)
    return xyzToLab(xyz.x, xyz.y, xyz.z)
}

private fun f(x: Double): Double {
    return if (x > Math.pow(6.0 / 29, 3.0)) {
        Math.pow(x, 1.0 / 3)
    } else {
        (1.0 / 3) * Math.pow(29.0 / 6, 2.0) * x + (4.0 / 29)
    }
}


private fun multiplyMatrices(firstMatrix: Array<DoubleArray>, secondMatrix: Array<DoubleArray>,
                             firstMatrixColumns: Int, secondMatrixColumns: Int): Array<DoubleArray> {
    val result = Array(firstMatrix.size) { DoubleArray(secondMatrixColumns) }
    for (i in 0 until firstMatrix.size) {
        for (j in 0 until secondMatrixColumns) {
            for (k in 0 until firstMatrixColumns) {
                result[i][j] += firstMatrix[i][k] * secondMatrix[k][j]
            }
        }
    }

    return result
}

data class Hsv(
        val h: Double,
        val s: Double,
        val v: Double
)

data class Xyz(
        val x: Double,
        val y: Double,
        val z: Double
)

data class Lab(
        val l: Double,
        val a: Double,
        val b: Double
)
