package adymova.nsu.grafics.core

import java.awt.Color

fun rgbToHsv(rgb: Color): Hsv {
    val r = rgb.red
    val g = rgb.green
    val b = rgb.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)

    var h = when {
        max == r && g >= b -> 60 * (g - b) / (max - min).toFloat()
        max == r && g < b -> 60 * (g - b) / (max - min).toFloat() + 360
        max == g -> 60 * (b - r) / (max - min).toFloat() + 120
        max == b -> 60 * (r - g) / (max - min).toFloat() + 240
        else -> throw IllegalStateException()
    }

    if (h.isNaN()) {
        h = 0.0f
    }

    var s = (1 - min / max.toFloat()) * 100

    if (s.isNaN()) {
        s = 0.0f
    }
    val v = max * 100 / 255.0f

    return Hsv(h, s, v)
}

fun rgbToXyz(rgb: Color): Xyz {
    val r = rgb.red / 255.0f
    val g = rgb.green / 255.0f
    val b = rgb.blue / 255.0f

    val xyzVector = arrayOf(
            floatArrayOf(r),
            floatArrayOf(g),
            floatArrayOf(b)
    )

    val m = arrayOf(
            floatArrayOf(0.5767309f, 0.1855540f, 0.1881852f),
            floatArrayOf(0.2973769f, 0.6273491f, 0.0752741f),
            floatArrayOf(0.0270343f, 0.0706872f, 0.9911085f)
    )

    val product = multiplyMatrices(m, xyzVector, 3, 1)

    return Xyz(product[0][0], product[1][0], product[2][0])
}

private fun xyzToLab(x: Float, y: Float, z: Float): Lab {
//    val xn = 0.31382
//    val yn = 0.331
//    val zn = 0.35518
    val xn = 0.9504f
    val yn = 1.0000f
    val zn = 1.0888f

    val l = 116 * f(y / yn) - 16
    val a = 500 * (f(x / xn) - f(y / yn))
    val b = 200 * (f(y / yn) - f(z / zn))

    return Lab(l, a, b)
}

fun rgbToLab(color: Color): Lab {
    val xyz = rgbToXyz(color)
    return xyzToLab(xyz.x, xyz.y, xyz.z)
}

private fun f(x: Float): Float {
    return if (x > Math.pow(6.0 / 29, 3.0)) {
        Math.pow(x.toDouble(), 1.0 / 3).toFloat()
    } else {
        (1.0f / 3) * Math.pow(29.0 / 6, 2.0).toFloat() * x + (4.0f / 29)
    }
}


private fun multiplyMatrices(firstMatrix: Array<FloatArray>, secondMatrix: Array<FloatArray>,
                             firstMatrixColumns: Int, secondMatrixColumns: Int): Array<FloatArray> {
    val result = Array(firstMatrix.size) { FloatArray(secondMatrixColumns) }
    for (i in 0 until firstMatrix.size) {
        for (j in 0 until secondMatrixColumns) {
            for (k in 0 until firstMatrixColumns) {
                result[i][j] += firstMatrix[i][k] * secondMatrix[k][j]
            }
        }
    }

    return result
}

private fun toColor(r: Float, g: Float, b: Float) = Color(
        Math.round(r * 255).normalize(),
        Math.round(g * 255).normalize(),
        Math.round(b * 255).normalize()
)

private fun Int.normalize() = if (this >= 256) 255 else this.toInt()

class Hsv(
        var h: Float,
        var s: Float,
        var v: Float) {

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
        val x: Float,
        val y: Float,
        val z: Float
)

class Lab(
        var l: Float,
        var a: Float,
        var b: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Lab

        if (l - other.l > 0.004) return false
        if (a - other.a > 0.004) return false
        if (b - other.b > 0.004) return false

        return true
    }

    override fun hashCode(): Int {
        var result = l.hashCode()
        result = 31 * result + a.hashCode()
        result = 31 * result + b.hashCode()
        return result
    }
}
