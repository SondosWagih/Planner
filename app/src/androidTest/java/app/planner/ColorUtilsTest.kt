package app.planner

import app.planner.ui.study.ColorTheme
import app.planner.ui.study.parseHexColor
import app.planner.ui.study.toHex
import org.junit.Assert.*
import org.junit.Test

class ColorUtilsTest {
    @Test fun parsesSixDigitCodes() {
        assertEquals(0xFF3F6FD8.toInt(), parseHexColor("3F6FD8"))
        assertEquals(0xFF3F6FD8.toInt(), parseHexColor("3f6fd8"))
    }

    @Test fun rejectsAnythingElse() {
        assertNull(parseHexColor("3F6FD"))
        assertNull(parseHexColor("3F6FD8A"))
        assertNull(parseHexColor("ZZZZZZ"))
        assertNull(parseHexColor(""))
    }

    @Test fun hexRoundTrips() {
        assertEquals("3F6FD8", toHex(0xFF3F6FD8.toInt()))
        assertEquals("000000", toHex(0xFF000000.toInt()))
        ColorTheme.entries.flatMap { it.colors }.forEach { assertEquals(it, parseHexColor(toHex(it))) }
    }

    @Test fun eachThemeIsSmall() {
        assertTrue(ColorTheme.entries.all { it.colors.size == 6 })
    }
}
