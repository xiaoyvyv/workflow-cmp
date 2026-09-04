package com.xiaoyv.workflow.platform.video

class JvmMediaVolumeManager : MediaVolumeManager {
    override fun getVolume(): Float = 1f

    override fun setVolume(level: Float) = Unit
}

fun MediaVolumeManager(): MediaVolumeManager = JvmMediaVolumeManager()
