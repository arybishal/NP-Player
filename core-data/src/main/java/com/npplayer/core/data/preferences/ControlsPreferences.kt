package com.npplayer.core.data.preferences

enum class PlayerAction(
    val id: String,
    val title: String,
    val description: String
) {
    INFO("info", "Video Info", "Displays video codec, resolution, bitrate and format details"),
    RECORD("record", "Screen Record", "Records a clip of the current video playback"),
    SCREENSHOT("screenshot", "Screenshot", "Captures high-resolution frame to gallery"),
    NIGHT_DIM("night_dim", "Night Dim", "Applies extra screen dimming for low-light viewing"),
    AB_REPEAT("ab_repeat", "A-B Repeat", "Loops a selected segment between point A and B"),
    PLAYLIST("playlist", "Playlist", "Opens quick queue and playlist drawer"),
    LOCK("lock", "Lock Screen", "Locks player controls to prevent accidental touches"),
    SUBTITLES("subtitles", "Subtitles", "Select subtitle track, sync delay and text styles"),
    AUDIO_FX("audio_fx", "Audio FX", "Equalizer, bass boost and sound presets"),
    SPEED("speed", "Speed", "Adjust playback speed from 0.25x to 4.0x"),
    FRAMERATE_MEMC("framerate_memc", "MEMC Smoothing", "Frame-rate interpolation for smoother motion"),
    BATTERY_SAVER("battery_saver", "Battery Saver", "Optimizes decoding pipeline to extend battery life"),
    QUALITY_HD("quality_hd", "Quality / HD", "Stream quality and decoder resolution info"),
    PICTURE_QUALITY("picture_quality", "Picture Quality", "Color tuning, brightness, contrast, saturation, and filters"),
    AUDIO_TRACK("audio_track", "Audio Track", "Switch between multi-language audio streams"),
    ASPECT_RATIO("aspect_ratio", "Aspect Ratio", "Toggle Fit, Fill, Stretch, 16:9, and 4:3"),
    PIP("pip", "Picture-in-Picture", "Minimizes video into floating corner window"),
    ORIENTATION("orientation", "Orientation", "Lock or rotate landscape/portrait orientation"),
    SLEEP_TIMER("sleep_timer", "Sleep Timer", "Automatically pauses playback after a set time"),
    BACKGROUND_PLAY("background_play", "Background Audio", "Continues audio playback when switching apps"),
    AUDIO_ONLY("audio_only", "Audio-Only Mode", "Turns off display rendering to save power"),
    MIRROR_FLIP("mirror_flip", "Mirror Flip", "Flips video horizontally"),
    MUTE("mute", "Mute", "Instant audio mute/unmute toggle"),
    MORE("more", "More Options", "Opens extra playback settings"),
    DECODER_MODE("decoder_mode", "Decoder (HW/SW)", "Switches between hardware and software fallback")
}

data class PlayerControlsSlots(
    val besideTitle: List<PlayerAction> = listOf(
        PlayerAction.INFO,
        PlayerAction.AUDIO_TRACK
    ),
    val topRight: List<PlayerAction> = listOf(
        PlayerAction.SCREENSHOT,
        PlayerAction.NIGHT_DIM,
        PlayerAction.SLEEP_TIMER,
        PlayerAction.MORE
    ),
    val bottomLeft: List<PlayerAction> = listOf(
        PlayerAction.LOCK,
        PlayerAction.SUBTITLES,
        PlayerAction.AUDIO_FX,
        PlayerAction.SPEED
    ),
    val bottomRight: List<PlayerAction> = listOf(
        PlayerAction.AUDIO_TRACK,
        PlayerAction.ASPECT_RATIO,
        PlayerAction.PIP,
        PlayerAction.ORIENTATION,
        PlayerAction.PLAYLIST
    )
) {
    companion object {
        const val MAX_BESIDE_TITLE = 3
        const val MAX_TOP_RIGHT = 6
        const val MAX_BOTTOM_LEFT = 6
        const val MAX_BOTTOM_RIGHT = 7

        val DEFAULT = PlayerControlsSlots()
    }
}
