package com.xiaoyv.workflow.platform.video

import android.content.Context
import android.media.AudioManager

class AndroidMediaVolumeManager(
    context: Context,
) : MediaVolumeManager {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun getVolume(): Float {
        val maximum = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (maximum == 0) return 0f
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / maximum.toFloat()
    }

    override fun setVolume(level: Float) {
        val maximum = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            (level.coerceIn(0f, 1f) * maximum).toInt(),
            0,
        )
    }
}

fun MediaVolumeManager(context: Context): MediaVolumeManager = AndroidMediaVolumeManager(context)
