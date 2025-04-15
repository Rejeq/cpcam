package com.rejeq.cpcam.core.audio

import com.rejeq.cpcam.core.audio.source.AudioSource
import javax.inject.Singleton

@Singleton
class AudioController(private val audioSource: AudioSource)
