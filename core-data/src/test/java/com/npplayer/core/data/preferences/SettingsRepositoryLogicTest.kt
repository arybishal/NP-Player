package com.npplayer.core.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryLogicTest {

    @Test
    fun testDefaultListPreferences() {
        val prefs = ListPreferences()
        assertEquals(7, prefs.newPeriodDays)
        assertTrue(prefs.markLastMedia)
        assertTrue(prefs.autoScrollToLast)
        assertFalse(prefs.selectByThumbnail)
        assertTrue(prefs.floatingResumeButton)
        assertFalse(prefs.showHiddenFiles)
        assertTrue(prefs.respectNoMedia)
    }

    @Test
    fun testDefaultPlayerPreferences() {
        val prefs = PlayerPreferences()
        assertEquals(3, prefs.controlsTimeoutSeconds)
        assertEquals(10, prefs.doubleTapSeekSeconds)
        assertTrue(prefs.rememberVolumeAndBrightness)
        assertTrue(prefs.autoplayNext)
        assertTrue(prefs.seekUndoEnabled)
        assertEquals(SeekBarStylePreference.CLASSIC, prefs.seekBarStyle)
        assertEquals(150, prefs.readAheadCacheMb)
        assertEquals(50, prefs.backSeekCacheMb)
        assertEquals("AUTOMATIC", prefs.hardwareAcceleration)
    }

    @Test
    fun testActionParsingLogic() {
        val raw = "info,lock,pip"
        val map = PlayerAction.values().associateBy { it.id }
        val parsed = raw.split(",").mapNotNull { map[it.trim()] }

        assertEquals(3, parsed.size)
        assertEquals(PlayerAction.INFO, parsed[0])
        assertEquals(PlayerAction.LOCK, parsed[1])
        assertEquals(PlayerAction.PIP, parsed[2])
    }

    @Test
    fun testActionParsingWithInvalidAndEmptyItems() {
        val raw = "info, unknown_action_xyz , , pip"
        val map = PlayerAction.values().associateBy { it.id }
        val parsed = raw.split(",").mapNotNull { map[it.trim()] }

        assertEquals(2, parsed.size)
        assertEquals(PlayerAction.INFO, parsed[0])
        assertEquals(PlayerAction.PIP, parsed[1])
    }

    @Test
    fun testSeekBarStyleValues() {
        val styles = SeekBarStylePreference.values()
        assertEquals(9, styles.size)
        assertTrue(styles.contains(SeekBarStylePreference.CLASSIC))
        assertTrue(styles.contains(SeekBarStylePreference.CHAPTERS))
        assertTrue(styles.contains(SeekBarStylePreference.WAVE))
        assertTrue(styles.contains(SeekBarStylePreference.GLOW))
        assertTrue(styles.contains(SeekBarStylePreference.NEON_MINIMAL))
        assertTrue(styles.contains(SeekBarStylePreference.GRADIENT_FLAME))
        assertTrue(styles.contains(SeekBarStylePreference.RETRO_BLOCKS))
        assertTrue(styles.contains(SeekBarStylePreference.BUBBLE_POP))
        assertTrue(styles.contains(SeekBarStylePreference.AMOLED_PULSE))
    }

    @Test
    fun testAppThemeValues() {
        val themes = AppTheme.values()
        assertEquals(3, themes.size)
        assertTrue(themes.contains(AppTheme.FOLLOW_DEVICE))
        assertTrue(themes.contains(AppTheme.LIGHT))
        assertTrue(themes.contains(AppTheme.DARK))
    }
}
