package com.npplayer.player.effects

import android.content.Context
import android.opengl.GLES20
import androidx.annotation.OptIn
import androidx.media3.common.VideoFrameProcessingException
import androidx.media3.common.util.GlProgram
import androidx.media3.common.util.GlUtil
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BaseGlShaderProgram
import androidx.media3.effect.GlEffect
import androidx.media3.effect.GlShaderProgram
import com.npplayer.core.data.preferences.PictureSettings
import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safe holder for dynamic picture adjustments.
 * Allows zero-latency live preview during slider drags without recreating GL pipeline or rebuffering.
 */
class PictureSettingsHolder(initial: PictureSettings = PictureSettings.DEFAULT) {
    private val ref = AtomicReference(initial)

    var current: PictureSettings
        get() = ref.get()
        set(value) = ref.set(value)
}

@OptIn(UnstableApi::class)
class PictureGlEffect(
    val settingsHolder: PictureSettingsHolder
) : GlEffect {

    override fun toGlShaderProgram(context: Context, useHdr: Boolean): GlShaderProgram {
        return PictureGlShaderProgram(useHdr, settingsHolder)
    }

    override fun isNoOp(inputWidth: Int, inputHeight: Int): Boolean {
        // Return false to keep the fast single-pass shader active in the pipeline for immediate live preview
        return false
    }
}

@OptIn(UnstableApi::class)
internal class PictureGlShaderProgram(
    useHdr: Boolean,
    private val settingsHolder: PictureSettingsHolder
) : BaseGlShaderProgram(useHdr, /* capacity= */ 1) {

    private var inputWidth: Int = 1920
    private var inputHeight: Int = 1080
    private val glProgram: GlProgram

    companion object {
        private const val VERTEX_SHADER = """
            attribute vec4 aFramePosition;
            uniform mat4 uTransformationMatrix;
            uniform mat4 uTexTransformationMatrix;
            varying vec2 vTexSamplingCoord;
            void main() {
              gl_Position = uTransformationMatrix * aFramePosition;
              vec4 texturePosition = vec4(aFramePosition.x * 0.5 + 0.5,
                                          aFramePosition.y * 0.5 + 0.5, 0.0, 1.0);
              vTexSamplingCoord = (uTexTransformationMatrix * texturePosition).xy;
            }
        """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            uniform sampler2D uTexSampler;
            varying vec2 vTexSamplingCoord;

            uniform float uBrightness;
            uniform float uContrast;
            uniform float uSaturation;
            uniform float uGamma;
            uniform float uSharpness;
            uniform float uWarmth;
            uniform float uHue;
            uniform vec2 uTexStep;

            vec3 applyHue(vec3 color, float angleDeg) {
                if (abs(angleDeg) < 0.1) return color;
                float rad = radians(angleDeg);
                float cosA = cos(rad);
                float sinA = sin(rad);
                mat3 rot = mat3(
                    0.299 + 0.701 * cosA + 0.168 * sinA, 0.587 - 0.587 * cosA + 0.330 * sinA, 0.114 - 0.114 * cosA - 0.497 * sinA,
                    0.299 - 0.299 * cosA - 0.328 * sinA, 0.587 + 0.413 * cosA + 0.035 * sinA, 0.114 - 0.114 * cosA + 0.288 * sinA,
                    0.299 - 0.300 * cosA + 1.250 * sinA, 0.587 - 0.588 * cosA - 1.050 * sinA, 0.114 + 0.886 * cosA - 0.203 * sinA
                );
                return clamp(rot * color, 0.0, 1.0);
            }

            void main() {
                vec4 baseColor = texture2D(uTexSampler, vTexSamplingCoord);
                vec3 c = baseColor.rgb;

                // 1. Sharpness (unsharp mask via 4-tap Laplacian kernel)
                if (uSharpness > 0.001) {
                    vec3 top = texture2D(uTexSampler, vTexSamplingCoord + vec2(0.0, uTexStep.y)).rgb;
                    vec3 bottom = texture2D(uTexSampler, vTexSamplingCoord - vec2(0.0, uTexStep.y)).rgb;
                    vec3 left = texture2D(uTexSampler, vTexSamplingCoord - vec2(uTexStep.x, 0.0)).rgb;
                    vec3 right = texture2D(uTexSampler, vTexSamplingCoord + vec2(uTexStep.x, 0.0)).rgb;
                    vec3 laplacian = 4.0 * c - (top + bottom + left + right);
                    c = clamp(c + uSharpness * laplacian, 0.0, 1.0);
                }

                // 2. Brightness
                c += uBrightness;

                // 3. Contrast (around 0.5 pivot)
                c = (c - 0.5) * uContrast + 0.5;

                // 4. Saturation (BT.709 luma)
                float luma = dot(c, vec3(0.2126, 0.7152, 0.0722));
                c = mix(vec3(luma), c, uSaturation);

                // 5. Hue
                c = applyHue(c, uHue);

                // 6. Warm / Night-light filter (amber/yellow tint, suppress harsh blue)
                if (uWarmth > 0.001) {
                    vec3 warmTint = vec3(1.0 + 0.15 * uWarmth, 1.0 + 0.08 * uWarmth, max(0.0, 1.0 - 0.55 * uWarmth));
                    c *= warmTint;
                }

                // 7. Gamma
                if (abs(uGamma - 1.0) > 0.01) {
                    c = pow(max(c, vec3(0.0)), vec3(1.0 / max(uGamma, 0.1)));
                }

                gl_FragColor = vec4(clamp(c, 0.0, 1.0), baseColor.a);
            }
        """
    }

    init {
        try {
            glProgram = GlProgram(VERTEX_SHADER, FRAGMENT_SHADER)
            glProgram.setBufferAttribute(
                "aFramePosition",
                GlUtil.getNormalizedCoordinateBounds(),
                4
            )
            val identity = GlUtil.create4x4IdentityMatrix()
            glProgram.setFloatsUniform("uTransformationMatrix", identity)
            glProgram.setFloatsUniform("uTexTransformationMatrix", identity)
        } catch (e: Exception) {
            throw VideoFrameProcessingException(e)
        }
    }

    override fun configure(inputWidth: Int, inputHeight: Int): Size {
        this.inputWidth = inputWidth.coerceAtLeast(1)
        this.inputHeight = inputHeight.coerceAtLeast(1)
        return Size(this.inputWidth, this.inputHeight)
    }

    override fun drawFrame(inputTexId: Int, presentationTimeUs: Long) {
        try {
            glProgram.use()
            glProgram.setSamplerTexIdUniform("uTexSampler", inputTexId, /* texUnitIndex= */ 0)

            val s = settingsHolder.current
            glProgram.setFloatUniform("uBrightness", s.brightness)
            glProgram.setFloatUniform("uContrast", s.contrast)
            glProgram.setFloatUniform("uSaturation", s.saturation)
            glProgram.setFloatUniform("uGamma", s.gamma)
            glProgram.setFloatUniform("uSharpness", s.sharpness)
            glProgram.setFloatUniform("uWarmth", s.warmth)
            glProgram.setFloatUniform("uHue", s.hue)
            glProgram.setFloatsUniform(
                "uTexStep",
                floatArrayOf(1.0f / inputWidth, 1.0f / inputHeight)
            )

            glProgram.bindAttributesAndUniforms()
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
            GlUtil.checkGlError()
        } catch (e: Exception) {
            throw VideoFrameProcessingException(e, presentationTimeUs)
        }
    }

    override fun release() {
        super.release()
        try {
            glProgram.delete()
        } catch (_: Exception) {}
    }
}
