package com.zynger.audiocapturesample

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder

class AudioCaptureService: Service() {

    private lateinit var mediaProjection: MediaProjection


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val mediaProjection = obtainMediaProjection()
        val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
//            .addMatchingUsage(AudioAttributes.USAGE_MEDIA) TODO provide UI options for inclusion/exclusion
            .build()
        val record = AudioRecord.Builder()
            .setAudioPlaybackCaptureConfig(config)
            .build()

        return Service.START_NOT_STICKY
    }

    private fun obtainMediaProjection(): MediaProjection {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        return mediaProjectionManager.getMediaProjection(resultCode, data!!) as MediaProjection
    }

    override fun onBind(p0: Intent?): IBinder? = null
}
