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
        h = 0.0
    }

    var s = (1 - min / max.toDouble()) * 100

    if (s.isNaN()) {
        s = 0.0
    }
    val v = max.toDouble() * 100 / 255.0

    return Hsv(h, s, v)
}

fun rgbToXyz(rgb: Color): Xyz {
    val r = rgb.red / 255.0
    val g = rgb.green / 255.0
    val b = rgb.blue / 255.0

    val xyzVector = arrayOf(
            doubleArrayOf(r),
            doubleArrayOf(g),
            doubleArrayOf(b)
    )

    val m = arrayOf(
            doubleArrayOf(0.5767309, 0.1855540, 0.1881852),
            doubleArrayOf(0.2973769, 0.6273491, 0.0752741),
            doubleArrayOf(0.0270343, 0.0706872, 0.9911085)
    )

    val product = multiplyMatrices(m, xyzVector, 3, 1)

    return Xyz(product[0][0], product[1][0], product[2][0])
}

private fun xyzToLab(x: Double, y: Double, z: Double): Lab {
//    val xn = 0.31382
//    val yn = 0.331
//    val zn = 0.35518
    val xn = 0.9504
    val yn = 1.0000
    val zn = 1.0888

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

private fun toColor(r: Double, g: Double, b: Double) = Color(
        Math.round(r * 255).normalize(),
        Math.round(g * 255).normalize(),
        Math.round(b * 255).normalize()
)

private fun Long.normalize() = if (this >= 256) 255 else this.toInt()

class Hsv(
        var h: Double,
        var s: Double,
        var v: Double) {

    fun toRgb(): Color {
        val s1 = s / 100
        val v1 = v / 100
        val h1 = h / 360

        val hI = (h1 * 6).toInt()
        val f = h1 * 6 - hI
        val p = v1 * (1 - s1)
        val q = v1 * (1 - f * s1)
        val t = v1 * (1 - (1 - f) * s1)

        return when (hI) {
            0 -> toColor(v1, t, p)
            1 -> toColor(q, v1, p)
            2 -> toColor(p, v1, t)
            3 -> toColor(p, q, v1)
            4 -> toColor(t, p, v1)
            5, 6 -> toColor(v1, p, q)
            else -> throw RuntimeException("RgbToHsv converting error for $h, $s, $v")
        }
    }

}

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
