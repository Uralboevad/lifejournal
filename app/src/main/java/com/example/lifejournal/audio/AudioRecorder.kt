package com.example.lifejournal.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var currentFilePath: String? = null

    var isRecording = false
        private set
    var isPlaying = false
        private set

    fun startRecording(): String {
        val fileName = "audio_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.m4a"
        val file = File(context.getExternalFilesDir(null), fileName)
        currentFilePath = file.absolutePath

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        recorder!!.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(currentFilePath)
            prepare()
            start()
        }

        isRecording = true
        return currentFilePath!!
    }

    fun stopRecording(): String? {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        isRecording = false
        return currentFilePath
    }

    fun startPlayback(filePath: String, onComplete: () -> Unit = {}) {
        player?.release()
        player = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
            start()
            setOnCompletionListener {
                this@AudioRecorder.isPlaying = false
                onComplete()
            }
        }
        isPlaying = true
    }

    fun stopPlayback() {
        player?.apply {
            if (this.isPlaying) stop()
            release()
        }
        player = null
        isPlaying = false
    }

    fun release() {
        stopPlayback()
        recorder?.release()
        recorder = null
    }
}