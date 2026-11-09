package com.npplayer.player.effects

import androidx.media3.common.util.UnstableApi
import com.npplayer.core.data.preferences.PicturePreset
import com.npplayer.core.data.preferences.PictureSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

@androidx.annotation.OptIn(UnstableApi::class)
class PictureEffectTest {

    @Test
    fun pictureSettingsHolder_updatesAtomically() {
        val holder = PictureSettingsHolder(PictureSettings.DEFAULT)
        assertEquals(PictureSettings.DEFAULT, holder.current)

        val updated = PictureSettings.fromPreset(PicturePreset.VIVID)
        holder.current = updated
        assertEquals(PicturePreset.VIVID, holder.current.activePreset)
        assertEquals(updated.saturation, holder.current.saturation, 0.001f)
    }

    @Test
    fun pictureGlEffect_isNoOpReturnsFalseToKeepShaderReadyForLiveDragging() {
        val holder = PictureSettingsHolder(PictureSettings.DEFAULT)
        val effect = PictureGlEffect(holder)
        assertFalse(effect.isNoOp(1920, 1080))
    }

    @Test
    fun allPresets_haveDistinctNonZeroAttributes() {
        for (preset in PicturePreset.values()) {
            val settings = PictureSettings.fromPreset(preset)
            assertNotNull(settings.activePreset)
            if (preset != PicturePreset.DEFAULT) {
                assertFalse("Preset $preset should differ from default", settings.isDefault())
            }
        }
    }
}
