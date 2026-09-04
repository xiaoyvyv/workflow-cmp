package com.xiaoyv.workflow.platform.video

class IosMediaVolumeManager : MediaVolumeManager {
    override fun getVolume(): Float = 1f

    override fun setVolume(level: Float) = Unit
}

fun MediaVolumeManager(): MediaVolumeManager = IosMediaVolumeManager()
