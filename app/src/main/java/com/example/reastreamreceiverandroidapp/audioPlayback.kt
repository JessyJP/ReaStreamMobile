package com.example.reastreamreceiverandroidapp

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log

class audioPlaybackProcess : Runnable {
    val msgPrefix = "Audio Playback:"
    private var UDP_receiverConnected : Boolean = false

    private var propertiesSet_FLAG :Boolean = false
    private var sampleRateHz : Int = 48000
    private var numberAudioChannels : Int = 2

    private var playbackDelay = 0

    lateinit var OutputStream: AudioTrack

    //    BufferArray
    var byteData : ByteArray = ByteArray(0)

    override fun run() {
        Log.i(TAG,"${Thread.currentThread()} $msgPrefix Thread Started")
        // wait
        while(!propertiesSet_FLAG)
        {
            //Wait here until a setup frame sets up the audio device
        }

        initializeOutputStream()

        while(UDP_receiverConnected) {
            if (OutputStream != null) {
                // Write the byte array to the track
                OutputStream.write(byteData, 0, byteData.size)

            } else Log.d(TAG, "$msgPrefix audio track stopped in loop ")
        }

        closeOutputStream()
    }

    fun setAudioPlaybackProperties(frame: ReastreamFrame){
        sampleRateHz = frame.audioSampleRate
        numberAudioChannels = frame.numAudioChannels
        // If the audio properties have been set
        UDP_receiverConnected = true
        propertiesSet_FLAG = true

    }

    fun initializeOutputStream() {
        // Set and push to audio track..
        val intSize = AudioTrack.getMinBufferSize(
            sampleRateHz,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_8BIT
        )
        OutputStream = AudioTrack(
            AudioManager.STREAM_MUSIC, sampleRateHz, AudioFormat.CHANNEL_CONFIGURATION_MONO,
            AudioFormat.ENCODING_PCM_8BIT, intSize, AudioTrack.MODE_STREAM
        )

        //TODO change the if statements to TRY CATCH blocks
        if (OutputStream != null) {
            OutputStream.play()
        } else Log.d(TAG, "$msgPrefix audio track is not initialised ")
    }

    fun playAudioBuffer(frame: ReastreamFrame) {
        byteData = frame.audioSampleBytes
        if (OutputStream != null) {
            // Write the byte array to the track
            OutputStream.write(byteData, 0, byteData.size)

        } else Log.d(TAG, "$msgPrefix audio track stopped in loop ")
        byteData.drop(byteData.size)
    }


    fun closeOutputStream() {
        if (OutputStream != null) {
            OutputStream.stop()
            OutputStream.release()
        } else Log.d(TAG, "$msgPrefix audio track can not deallocate ")
    }
}

