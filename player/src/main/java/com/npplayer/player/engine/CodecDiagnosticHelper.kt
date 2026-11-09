package com.npplayer.player.engine

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build

data class CodecInfoItem(
    val name: String,
    val mimeType: String,
    val isHardwareAccelerated: Boolean,
    val isEncoder: Boolean
)

object CodecDiagnosticHelper {

    fun getSupportedCodecs(): List<CodecInfoItem> {
        val result = mutableListOf<CodecInfoItem>()
        try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            for (info in codecList.codecInfos) {
                if (info.isEncoder) continue
                val types = info.supportedTypes
                for (type in types) {
                    val isHw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        info.isHardwareAccelerated
                    } else {
                        !info.name.startsWith("OMX.google.") && !info.name.startsWith("c2.android.")
                    }
                    result.add(
                        CodecInfoItem(
                            name = info.name,
                            mimeType = type,
                            isHardwareAccelerated = isHw,
                            isEncoder = false
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        return result
    }

    fun hasAudioCodec(mimePrefixOrSub: String): Boolean {
        return getSupportedCodecs().any {
            it.mimeType.contains(mimePrefixOrSub, ignoreCase = true)
        }
    }

    fun getCodecSummary(): Map<String, Boolean> {
        val codecs = getSupportedCodecs()
        val keyCodecs = listOf(
            "AAC" to listOf("audio/mp4a-latm", "aac"),
            "AC3 (Dolby Digital)" to listOf("audio/ac3", "audio/ac3-joc"),
            "E-AC3 (Dolby Digital Plus)" to listOf("audio/eac3", "audio/eac3-joc"),
            "DTS / DTS-HD" to listOf("audio/vnd.dts", "audio/dts"),
            "FLAC" to listOf("audio/flac"),
            "Opus" to listOf("audio/opus"),
            "H.264 (AVC)" to listOf("video/avc"),
            "H.265 (HEVC)" to listOf("video/hevc"),
            "AV1" to listOf("video/av01"),
            "VP9" to listOf("video/x-vnd.on2.vp9")
        )

        val summary = mutableMapOf<String, Boolean>()
        for ((label, mimes) in keyCodecs) {
            val supported = codecs.any { codec ->
                mimes.any { m -> codec.mimeType.contains(m, ignoreCase = true) }
            }
            summary[label] = supported
        }
        return summary
    }
}
