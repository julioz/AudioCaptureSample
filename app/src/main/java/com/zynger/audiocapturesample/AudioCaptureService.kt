package com.zynger.audiocapturesample

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import androidx.core.app.NotificationCompat


class AudioCaptureService : Service() {

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null

    private var isRecording: Boolean = false
    private var audioRecord: AudioRecord? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(
            SERVICE_ID,
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).build()
        )

        // use applicationContext to avoid memory leak on Android 10.
        // see: https://partnerissuetracker.corp.google.com/issues/139732252
        mediaProjectionManager =
            applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Audio Capture Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(NotificationManager::class.java) as NotificationManager
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return if (intent != null) {
            when (intent.action) {
                ACTION_START -> {
                    mediaProjection =
                        mediaProjectionManager.getMediaProjection(
                            Activity.RESULT_OK,
                            intent.getParcelableExtra(EXTRA_RESULT_DATA)!!
                        ) as MediaProjection
                    startAudioCapture()
                    Service.START_STICKY
                }
                ACTION_STOP -> {
                    stopAudioCapture()
                    Service.START_NOT_STICKY
                }
                else -> throw IllegalArgumentException("Unexpected action received: ${intent.action}")
            }
        } else {
            Service.START_NOT_STICKY
        }
    }

    private fun startAudioCapture() {
        val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection!!)
            .addMatchingUsage(AudioAttributes.USAGE_MEDIA) // TODO provide UI options for inclusion/exclusion
            .build()

        /**
         * Using hardcoded values for the audio format, Mono PCM samples with a sample rate of 8000Hz
         * These can be changed according to your application's needs
         */
        val audioFormat = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(8000)
            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
            .build()

        audioRecord = AudioRecord.Builder()
            .setAudioFormat(audioFormat)
            // For optimal performance, the buffer size
            // can be optionally specified to store audio samples.
            // If the value is not specified,
            // uses a single frame and lets the
            // native code figure out the minimum buffer size.
            // .setBufferSizeInBytes(sizeInBytes)
            .setAudioPlaybackCaptureConfig(config)
            .build()

        isRecording = true
        audioRecord!!.startRecording()
        writeAudioDataToFile()
    }

    private fun writeAudioDataToFile() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun stopAudioCapture() {
        requireNotNull(mediaProjection) { "Tried to stop audio capture, but there was no ongoing capture in place!" }

        isRecording = false

        audioRecord!!.stop()
        audioRecord!!.release()
        audioRecord = null

        mediaProjection!!.stop()
        stopSelf()
    }

    override fun onBind(p0: Intent?): IBinder? = null

    companion object {
        private const val SERVICE_ID = 123
        private const val NOTIFICATION_CHANNEL_ID = "AudioCapture channel"
        const val ACTION_START = "AudioCaptureService:Start"
        const val ACTION_STOP = "AudioCaptureService:Stop"
        const val EXTRA_RESULT_DATA = "AudioCaptureService:Extra:ResultData"
    }
}
