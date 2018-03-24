package adymova.nsu.grafics.panels.segmentation

import adymova.nsu.grafics.core.Lab
import java.lang.Math.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

const val q = 6103515625

fun computeCiede2000Metrics(lab1: Lab, lab2: Lab): Double {
    val l1 = lab1.l
    val a1 = lab1.a
    val b1 = lab1.b

    val l2 = lab2.l
    val a2 = lab2.a
    val b2 = lab2.b

    val g = computeG(a1, b1, a2, b2)
    val a1Hatch = computeAHatch(g, a1)
    val a2Hatch = computeAHatch(g, a2)
    val c1Hatch = computeCHatch(a1Hatch, b1)
    val c2Hatch = computeCHatch(a2Hatch, b2)
    val h1Hatch = computeHHatch(b1, a1Hatch)
    val h2Hatch = computeHHatch(b2, a2Hatch)

    val deltaLHatch = l2 - l1
    val deltaCHatch = c2Hatch - c1Hatch
    val deltaH = h2Hatch - h1Hatch
    val cHatchProduct = c1Hatch * c2Hatch
    val deltaHHatch = computeDeltaHHatch(cHatchProduct, deltaH)
    val upperH = 2 * sqrt(cHatchProduct) * sin(toRadians(deltaHHatch / 2))

    val lLineHatch = (l1 + l2) / 2
    val cLineHatch = (c1Hatch + c2Hatch) / 2
    val hLineHatch = computeHLineHatch(cHatchProduct, h1Hatch, h2Hatch)
    val t = computeT(hLineHatch)
    //todo check braces

    val sl = computeSl(lLineHatch)
    val sc = computeSc(cLineHatch)
    val sh = computeSh(cLineHatch, t)
    val rt = computeRt(hLineHatch, cLineHatch)

    val result = computeE(deltaLHatch, sl, deltaCHatch, sc, upperH, sh, rt)
    return result
}

fun computeRt(hLineHatch: Double, cLineHatch: Double): Double {
    val deltaTheta = computeDeltaTheta(hLineHatch)
    val rc = computeRc(cLineHatch)
    val rt = -sin(toRadians(2 * deltaTheta)) * rc
    return rt
}

fun computeSh(cLineHatch: Double, t: Double) = 1 + 0.015 * cLineHatch * t

fun computeSc(cLineHatch: Double) = 1 + 0.045 * cLineHatch

fun computeT(hLineHatch: Double) = 1 -
        0.17 * cos(toRadians(hLineHatch - 30)) +
        0.24 * cos(toRadians(2 * hLineHatch)) +
        0.32 * cos(toRadians(3 * hLineHatch + 6)) -
        0.20 * cos(toRadians(4 * hLineHatch - 63))

fun computeG(a1: Double, b1: Double, a2: Double, b2: Double): Double {
    val cLine = computeCLine(a1, b1, a2, b2)
    val cLinePow7 = pow(cLine, 7.0)
    val g = 0.5 * (1 - sqrt(cLinePow7 / (cLinePow7 + q)))
    return g
}

private fun computeCLine(a1: Double, b1: Double, a2: Double, b2: Double): Double {
    val c1 = computeC(a1, b1)
    val c2 = computeC(a2, b2)
    return (c1 + c2) / 2
}

fun computeAHatch(g: Double, a: Double) = (1 + g) * a

fun computeSl(lLineHatch: Double): Double {
    val coeff2 = (lLineHatch - 50) * (lLineHatch - 50)
    return 1 + 0.015 * coeff2 / sqrt(20 + coeff2)
}

fun computeDeltaTheta(hLineHatch: Double): Double {
    val coeff1 = (hLineHatch - 275) / 25
    return 30 * exp(-coeff1 * coeff1)
}

fun computeRc(cLineHatch: Double): Double {
    val cLineHatchPow7 = pow(cLineHatch, 7.0)

    return 2 * sqrt(cLineHatchPow7 / (cLineHatchPow7 + q))
}

fun computeE(deltaLHatch: Double, sl: Double, deltaCHatch: Double, sc: Double, upperH: Double, sh: Double, rt: Double): Double {
    val component1 = deltaLHatch / sl
    val component2 = deltaCHatch / sc
    val component3 = upperH / sh
    val component4 = rt * deltaCHatch * upperH / (sc * sh)
    return sqrt(component1 * component1 + component2 * component2 + component3 * component3 + component4)
}

fun computeHLineHatch(cHatchProduct: Double, h1Hatch: Double, h2Hatch: Double): Double {
    val sumHHatch = h1Hatch + h2Hatch
    val deltaH = h2Hatch - h1Hatch


    return when {
        abs(deltaH) <= 180 && cHatchProduct != 0.0 -> sumHHatch / 2
        abs(deltaH) > 180 && deltaH < 360 && cHatchProduct != 0.0 -> (sumHHatch + 360) / 2
        abs(deltaH) > 180 && deltaH >= 360 && cHatchProduct != 0.0 -> (sumHHatch - 360) / 2
        cHatchProduct == 0.0 -> sumHHatch
        else -> throw IllegalStateException()
    }
}

fun computeDeltaHHatch(cHatchProduct: Double, deltaH: Double): Double {
    return when {
        cHatchProduct == 0.0 -> 0.0
        cHatchProduct != 0.0 && abs(deltaH) <= 180 -> deltaH
        cHatchProduct != 0.0 && deltaH > 180 -> deltaH - 360
        cHatchProduct != 0.0 && deltaH < -180 -> deltaH + 360
        else -> throw IllegalStateException()
    }
}

fun computeC(a: Double, b: Double) = sqrt(a * a + b * b)

fun computeCHatch(aHatch: Double, b: Double) = sqrt(aHatch * aHatch + b * b)

fun computeHHatch(b: Double, aHatch: Double): Double {
    return when {
        b == aHatch && aHatch == 0.0 -> 0.0
        else -> {
            var hHatch = toDegrees(atan2(b, aHatch))
//            This must be converted to a angle in degrees between 0 and 360 by addition of 2􏰏*PI to negative hue angles.
            if (hHatch < 0) {
                hHatch += 360.0
            }
            return hHatch
        }
    }
}