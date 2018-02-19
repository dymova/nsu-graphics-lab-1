import adymova.nsu.grafics.core.*
import adymova.nsu.grafics.panels.ImagePanel
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.awt.Color
import java.util.stream.Stream
import kotlin.math.abs

class FormatConverterTest {
    @ParameterizedTest
    @MethodSource("rgbAndHsvProvider")
    fun testRgbToHsv(rgb: Color, expectedHsv: Hsv) {
        val actualHsv = rgbToHsv(rgb)

        println("Actual: (${actualHsv.h}, ${actualHsv.s}, ${actualHsv.v})")
        println("Expected: (${expectedHsv.h}, ${expectedHsv.s}, ${expectedHsv.v})")
        assertDoubleEquals(actualHsv.h, expectedHsv.h)
        assertDoubleEquals(actualHsv.s, expectedHsv.s)
        assertDoubleEquals(actualHsv.v, expectedHsv.v)

    }

    @ParameterizedTest
    @MethodSource("rgbAndHsvProvider")
    fun testHsvToRgb(expectedRgb: Color, hsv: Hsv) {
        val actualRgb = hsv.toRgb()

        println("Actual: (${actualRgb.red}, ${actualRgb.green}, ${actualRgb.blue})")
        println("Expected: (${expectedRgb.red}, ${expectedRgb.green}, ${expectedRgb.blue})")
        assertThat(actualRgb.red).isEqualTo( expectedRgb.red)
        assertThat(actualRgb.green).isEqualTo( expectedRgb.green)
        assertThat(actualRgb.blue).isEqualTo( expectedRgb.blue)

    }

    private fun assertDoubleEquals(actual: Double, expected: Double) {
        assertThat(abs(actual - expected) < 0.2).isTrue()
    }

    @ParameterizedTest
    @MethodSource("hsvValueProvider")
    fun testGetNewHvsValue(sliderValue: Double, currentValue: Double, expectedValue: Double) {
        val newValue = ImagePanel(ImageContext()).getNewValue(sliderValue, currentValue)
        assertThat(newValue).isCloseTo(expectedValue, Offset.offset(0.5))
    }

    @ParameterizedTest
    @MethodSource("rgbAndLabProvider")
    fun rgbToLabTest(rgb: Color, expectedValue: Lab) {
        val newValue = rgbToLab(rgb)

        println("Actual: (${newValue.l}, ${newValue.a}, ${newValue.b})")
        println("Expected: (${expectedValue.l}, ${expectedValue.a}, ${expectedValue.b})")
        assertDoubleEquals(newValue.l, expectedValue.l)
        assertDoubleEquals(newValue.a, expectedValue.a)
        assertDoubleEquals(newValue.b, expectedValue.b)
    }

    companion object {
        @JvmStatic
        fun rgbAndHsvProvider(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(Color(0, 0, 0), Hsv(0.0, 0.0, 0.0)),
                    Arguments.of(Color(255, 255, 255), Hsv(0.0, 0.0, 100.0)),
                    Arguments.of(Color(255, 0, 0), Hsv(0.0, 100.0, 100.0)),
                    Arguments.of(Color(0,255,0), Hsv(120.0, 100.0, 100.0)),
                    Arguments.of(Color(0, 128, 128), Hsv(180.0, 100.0, 50.0)),
                    Arguments.of(Color(192, 192, 192), Hsv(0.0, 0.0, 75.0)),
                    Arguments.of(Color(128, 0, 128), Hsv(300.0, 100.0, 50.0))
                    )
        }

        @JvmStatic
        fun hsvValueProvider(): Stream<Arguments> {
            return Stream.of(
//                    sliderValue: Double, currentValue: Double, expectedValue: Double
                    Arguments.of(75.0, 50.0, 75.0),
                    Arguments.of(25.0, 50.0, 25.0),
                    Arguments.of(25.0, 0.0, 0.0),
                    Arguments.of(75.0, 0.0, 50.0),
                    Arguments.of(50.0, 50.0, 50.0)
            )
        }

        @JvmStatic
        fun rgbAndLabProvider(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(Color(0, 0, 0), Lab(0.0, 0.0, 0.0)),
                    Arguments.of(Color(255, 255, 255), Lab(100.0, 0.005, -0.01)),
                    Arguments.of(Color(128, 0, 128), Lab(29.7, 58.93, -36.49))
            )
        }

    }


}
