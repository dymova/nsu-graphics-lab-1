import adymova.nsu.grafics.core.*
import adymova.nsu.grafics.panels.general.ImagePanel
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
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

    private fun assertDoubleEquals(actual: Float, expected: Float) {
        assertThat(abs(actual - expected) < 0.2).isTrue()
    }

    @ParameterizedTest
    @MethodSource("hsvValueProvider")
    fun testGetNewHvsValue(sliderValue: Float, currentValue: Float, expectedValue: Float) {
        val newValue = ImagePanel(ImageContext()).getNewValue(sliderValue, currentValue)
        assertThat(newValue).isCloseTo(expectedValue, Offset.offset(0.5f))
    }

    @ParameterizedTest
    @MethodSource("rgbAndLabProvider")
    fun rgbToLabTest(rgb: Color, expectedValue: Lab) {
        val newValue = rgbToLab(rgb)

        println("Actual: (${newValue.l}, ${newValue.a}, ${newValue.b})")
        println("Expected: (${expectedValue.l}, ${expectedValue.a}, ${expectedValue.b})")
        assertThat(newValue.l).isCloseTo(expectedValue.l, Offset.offset(0.2f))
        assertThat(newValue.a).isCloseTo(expectedValue.a, Offset.offset(0.2f))
        assertThat(newValue.b).isCloseTo(expectedValue.b, Offset.offset(0.2f))
    }

    companion object {
        @JvmStatic
        fun rgbAndHsvProvider(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(Color(0, 0, 0), Hsv(0.0f, 0.0f, 0.0f)),
                    Arguments.of(Color(255, 255, 255), Hsv(0.0f, 0.0f, 100.0f)),
                    Arguments.of(Color(255, 0, 0), Hsv(0.0f, 100.0f, 100.0f)),
                    Arguments.of(Color(0,255,0), Hsv(120.0f, 100.0f, 100.0f)),
                    Arguments.of(Color(0, 128, 128), Hsv(180.0f, 100.0f, 50.0f)),
                    Arguments.of(Color(192, 192, 192), Hsv(0.0f, 0.0f, 75.0f)),
                    Arguments.of(Color(128, 0, 128), Hsv(300.0f, 100.0f, 50.0f))
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
                    Arguments.of(Color(0, 0, 0), Lab(0.0f, 0.0f, 0.0f)),
                    Arguments.of(Color(255, 255, 255), Lab(100.0f, 0.005f, -0.01f)),
                    Arguments.of(Color(128, 0, 128), Lab(29.7f, 58.93f, -36.49f))
            )
        }

    }


}
