package com.example.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.util.Log

class AudioRingHelper(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null
    private var isPlayingOutgoingTone = false
    private var outgoingToneThread: Thread? = null

    private var incomingRingtone: Ringtone? = null

    fun startOutgoingDialtone() {
        if (isPlayingOutgoingTone) return
        isPlayingOutgoingTone = true

        outgoingToneThread = Thread {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 80)
                while (isPlayingOutgoingTone) {
                    toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 1200)
                    Thread.sleep(3200) // Ring interval
                }
            } catch (e: Exception) {
                Log.e("AudioRingHelper", "Error playing dialtone: ${e.message}")
            } finally {
                stopOutgoingDialtone()
            }
        }.apply { start() }
    }

    fun stopOutgoingDialtone() {
        isPlayingOutgoingTone = false
        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            Log.e("AudioRingHelper", "Error stopping dialtone: ${e.message}")
        }
        outgoingToneThread?.interrupt()
        outgoingToneThread = null
    }

    fun startIncomingRingtone() {
        if (incomingRingtone?.isPlaying == true) return
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            incomingRingtone = RingtoneManager.getRingtone(context.applicationContext, ringtoneUri)
            incomingRingtone?.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            incomingRingtone?.play()
        } catch (e: Exception) {
            Log.e("AudioRingHelper", "Error playing incoming ringtone: ${e.message}")
        }
    }

    fun stopIncomingRingtone() {
        try {
            if (incomingRingtone?.isPlaying == true) {
                incomingRingtone?.stop()
            }
            incomingRingtone = null
        } catch (e: Exception) {
            Log.e("AudioRingHelper", "Error stopping incoming ringtone: ${e.message}")
        }
    }

    fun stopAll() {
        stopOutgoingDialtone()
        stopIncomingRingtone()
    }
}
