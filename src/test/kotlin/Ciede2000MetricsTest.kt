import adymova.nsu.grafics.core.Lab
import adymova.nsu.grafics.panels.segmentation.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.offset
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

const val delta = 0.001

class Ciede2000MetricsTest {
    @ParameterizedTest
    @MethodSource("ciede2000MetricsProvider")
    fun test(lab1: Lab, lab2: Lab,
             a1Hatch: Double, c1Hatch: Double, h1Hatch: Double, hLineHatch: Double, g: Double, t: Double, sl: Double, sc: Double, sh: Double, rt: Double,
             e: Double,
             a2Hatch: Double, c2Hatch: Double, h2Hatch: Double
    ) {
        val l1 = lab1.l.toDouble()
        val a1 = lab1.a.toDouble()
        val b1 = lab1.b.toDouble()

        val l2 = lab2.l.toDouble()
        val a2 = lab2.a.toDouble()
        val b2 = lab2.b.toDouble()

        val realG = computeG(a1, b1, a2, b2)
        assertThat(realG).isEqualTo(g, offset(delta))

        val a1HatchReal = computeAHatch(g, a1)
        assertThat(a1HatchReal).isCloseTo(a1Hatch, offset(delta))

        val a2HatchReal = computeAHatch(g, a2)
        assertThat(a2HatchReal).isCloseTo(a2Hatch, offset(delta))

        val c1HatchReal = computeCHatch(a1Hatch, b1)
        assertThat(c1HatchReal).isCloseTo(c1Hatch, offset(delta))

        val c2HatchReal = computeCHatch(a2Hatch, b2)
        assertThat(c2HatchReal).isCloseTo(c2Hatch, offset(delta))

        val h1HatchReal = computeHHatch(b1, a1Hatch)
        assertThat(h1HatchReal).isCloseTo(h1Hatch, offset(delta))

        val h2HatchReal = computeHHatch(b2, a2Hatch)
        assertThat(h2HatchReal).isCloseTo(h2Hatch, offset(delta))

        val cHatchProduct = c1Hatch * c2Hatch
        val hLineHatchReal = computeHLineHatch(cHatchProduct, h1Hatch, h2Hatch)
        assertThat(hLineHatchReal).isCloseTo(hLineHatch, offset(delta))

        val tReal = computeT(hLineHatch)
        assertThat(tReal).isCloseTo(t, offset(delta))

        val lLineHatch = (l1 + l2) / 2
        val cLineHatch = (c1Hatch + c2Hatch) / 2
        val slReal = computeSl(lLineHatch)
        assertThat(slReal).isCloseTo(sl, offset(delta))
        val scReal = computeSc(cLineHatch)
        assertThat(scReal).isCloseTo(sc, offset(delta))
        val shReal = computeSh(cLineHatch, t)
        assertThat(shReal).isCloseTo(sh, offset(delta))

        val rtReal = computeRt(hLineHatch, cLineHatch)
        assertThat(rtReal).isCloseTo(rt, offset(delta))

        val eReal: Double = computeCiede2000Metrics(lab1, lab2)
        assertThat(eReal).isCloseTo(e, offset(delta))
    }

    companion object {
        @JvmStatic
        fun ciede2000MetricsProvider(): Stream<Arguments> {
            return Stream.of(
                    //1
                    Arguments.of(Lab(50.0f, 2.6772f, -79.7751f),
                            Lab(50.0f, 0.0f, -82.7485f),
                            2.6774, 79.8200, 271.9222, 270.9611, 0.0001, 0.6907, 1.0000, 4.6578, 1.8421, -1.7042, 2.0425,
                            0.0000, 82.7485, 270.0000),
                    //2
                    Arguments.of(Lab(50.0000f, 3.1571f, -77.28031f),
                            Lab(50.0000f, 0.0000f, -82.7485f),
                            3.1573, 77.3448, 272.3395, 271.1698, 0.0001, 0.6843, 1.0000, 4.6021, 1.8216, -1.7070, 2.8615,
                            0.0000, 82.7485, 270.00000),
                    //3
                    Arguments.of(Lab(50.0000f, 2.8361f, -74.0200f),
                            Lab(50.0000f, 0.0000f, -82.7485f),
                            2.8363, 74.0743, 272.1944, 271.0972, 0.0001, 0.6865, 1.0000, 4.5285, 1.8074, -1.7060, 3.4412,
                            0.0000, 82.7485, 270.00000),
                    //4
                    Arguments.of(Lab(50.0000f, -1.3802f, -84.2814f),
                            Lab(50.0000f, 0.0000f, -82.7485f),
                            -1.3803, 84.2927, 269.0618, 269.5309, 0.0001, 0.7357, 1.0000, 4.7584, 1.9217, -1.6809, 1.0000,
                            0.0000, 82.7485, 270.00000),
                    //5

                    Arguments.of(Lab(50.0000f, -1.1848f, -84.8006f),
                            Lab(50.0000f, 0.0000f, -82.7485f),
                            -1.1849, 84.8089, 269.1995, 269.5997, 0.0001, 0.7335, 1.0000, 4.7700, 1.9218, -1.6822, 1.0000,
                            0.0000, 82.7485, 270.00000),
                    //24
                    Arguments.of(Lab(50.0000f, 2.5000f, 0.0000f),
                            Lab(50.0000f, 3.2592f, 0.3350f),
                            3.7493, 3.7493, 0.0000, 1.9603, 0.4997, 1.2883, 1.0000, 1.1946, 1.0836, 0.0000, 1.0000,
                            4.8879, 4.8994, 3.9206)
            )
        }
    }
}