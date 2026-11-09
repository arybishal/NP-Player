package com.npplayer.core.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ControlsPreferencesTest {

    @Test
    fun testSlotLimitsConstants() {
        assertEquals(3, PlayerControlsSlots.MAX_BESIDE_TITLE)
        assertEquals(6, PlayerControlsSlots.MAX_TOP_RIGHT)
        assertEquals(6, PlayerControlsSlots.MAX_BOTTOM_LEFT)
        assertEquals(7, PlayerControlsSlots.MAX_BOTTOM_RIGHT)
    }

    @Test
    fun testDefaultSlotsWithinLimits() {
        val defaults = PlayerControlsSlots.DEFAULT

        assertTrue(
            "Beside title count (${defaults.besideTitle.size}) exceeds limit ${PlayerControlsSlots.MAX_BESIDE_TITLE}",
            defaults.besideTitle.size <= PlayerControlsSlots.MAX_BESIDE_TITLE
        )
        assertTrue(
            "Top right count (${defaults.topRight.size}) exceeds limit ${PlayerControlsSlots.MAX_TOP_RIGHT}",
            defaults.topRight.size <= PlayerControlsSlots.MAX_TOP_RIGHT
        )
        assertTrue(
            "Bottom left count (${defaults.bottomLeft.size}) exceeds limit ${PlayerControlsSlots.MAX_BOTTOM_LEFT}",
            defaults.bottomLeft.size <= PlayerControlsSlots.MAX_BOTTOM_LEFT
        )
        assertTrue(
            "Bottom right count (${defaults.bottomRight.size}) exceeds limit ${PlayerControlsSlots.MAX_BOTTOM_RIGHT}",
            defaults.bottomRight.size <= PlayerControlsSlots.MAX_BOTTOM_RIGHT
        )
    }

    @Test
    fun testAllPlayerActionsHaveValidMetadata() {
        val actions = PlayerAction.values()
        assertEquals(25, actions.size)

        for (action in actions) {
            assertNotNull(action.id)
            assertTrue("Action ID should not be blank", action.id.isNotBlank())
            assertTrue("Action title should not be blank", action.title.isNotBlank())
            assertTrue("Action description should not be blank", action.description.isNotBlank())
        }
    }

    @Test
    fun testPlayerActionUniqueIds() {
        val actions = PlayerAction.values()
        val ids = actions.map { it.id }.toSet()
        assertEquals("Action IDs must be unique across all 25 actions", actions.size, ids.size)
    }

    @Test
    fun testPlayerControlsSlotsCustomCreation() {
        val custom = PlayerControlsSlots(
            besideTitle = listOf(PlayerAction.INFO),
            topRight = listOf(PlayerAction.SCREENSHOT),
            bottomLeft = listOf(PlayerAction.LOCK),
            bottomRight = listOf(PlayerAction.PIP)
        )

        assertEquals(1, custom.besideTitle.size)
        assertEquals(PlayerAction.INFO, custom.besideTitle[0])
        assertEquals(PlayerAction.SCREENSHOT, custom.topRight[0])
        assertEquals(PlayerAction.LOCK, custom.bottomLeft[0])
        assertEquals(PlayerAction.PIP, custom.bottomRight[0])
    }
}
