package com.example.reastreammobile

import android.annotation.SuppressLint
import android.media.*
import android.util.Log
import java.io.*


class AudioRecorder(UI:MainActivity) : Runnable {
    val msgPrefix = "Audio Recorder:"
    private var UDP_transmitterConnected : Boolean = false

    private var propertiesSet_FLAG :Boolean = false
    internal var sampleRateHz : Int = 48000
    internal var numberAudioChannels : Int = 2

    private var playbackDelay = 0

    lateinit var OutputStream: AudioRecord
    ////////////////
    val SAMPLING_RATE: Int = 44100
    val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC
    val CHANNEL_IN_CONFIG: Int = AudioFormat.CHANNEL_IN_MONO
    val AUDIO_FORMAT: Int = AudioFormat.ENCODING_PCM_16BIT
    val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_IN_CONFIG, AUDIO_FORMAT)


    //    BufferArray
    var byteData : ByteArray = ByteArray(0)
    @SuppressLint("MissingPermission")// Todo
    override fun run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        Log.d(TAG, "$msgPrefix [Starting recording]"); //TODO change this message
        var audioData : ByteArray = ByteArray(BUFFER_SIZE);


        val recorder:AudioRecord  = AudioRecord(
            AUDIO_SOURCE,
            SAMPLING_RATE,
            CHANNEL_IN_CONFIG,
            AUDIO_FORMAT,
            BUFFER_SIZE);


        recorder.startRecording();

        while (!true) {

            var status:Int = recorder.read(audioData, 0, audioData.size);

            if (status == AudioRecord.ERROR_INVALID_OPERATION ||
                status == AudioRecord.ERROR_BAD_VALUE) {
                Log.e("Record", "Error reading audio data!");
                return;
            }
            // TODO pack the audio data
            // TODO Send the audio via UDP
        }

        try {

            recorder.stop();
            recorder.release();


        } catch (   e: IOException  ) {
            Log.e("Record", "Error when releasing", e);
        }





    }



}