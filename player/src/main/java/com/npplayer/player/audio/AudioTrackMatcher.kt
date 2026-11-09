package com.npplayer.player.audio

import com.npplayer.core.data.database.entity.VideoAudioPreferenceEntity
import com.npplayer.player.engine.MediaTrackItem

object AudioTrackMatcher {

    fun findBestMatch(
        availableTracks: List<MediaTrackItem>,
        savedPreference: VideoAudioPreferenceEntity?,
        folderPreference: VideoAudioPreferenceEntity?,
        preferredLanguages: List<String>
    ): MediaTrackItem? {
        if (availableTracks.isEmpty()) return null

        // 1. Saved video-specific preference
        if (savedPreference != null) {
            // (a) Exact match: language + codec + channels
            availableTracks.firstOrNull {
                it.language.equals(savedPreference.languageCode, ignoreCase = true) &&
                    it.codec.equals(savedPreference.codec, ignoreCase = true) &&
                    it.channelCount == savedPreference.channelCount
            }?.let { return it }

            // (b) Fallback: language only
            availableTracks.firstOrNull {
                it.language.equals(savedPreference.languageCode, ignoreCase = true)
            }?.let { return it }

            // (c) Fallback: track index
            if (savedPreference.trackIndex in availableTracks.indices) {
                return availableTracks[savedPreference.trackIndex]
            }
        }

        // 2. Folder / series preference
        if (folderPreference != null) {
            availableTracks.firstOrNull {
                it.language.equals(folderPreference.languageCode, ignoreCase = true)
            }?.let { return it }
        }

        // 3. Preferred audio languages in user order
        for (prefLang in preferredLanguages) {
            availableTracks.firstOrNull {
                it.language.equals(prefLang, ignoreCase = true)
            }?.let { return it }
        }

        return null
    }
}
