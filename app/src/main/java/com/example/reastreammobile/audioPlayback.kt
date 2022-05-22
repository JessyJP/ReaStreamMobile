package com.example.reastreammobile

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log

open class audioPlaybackProcess : Runnable {
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
                if(DEBUG)Log.d(TAG,msgPrefix+" send ${byteData.size} bytes")
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
        Log.d(TAG,msgPrefix+" setAudioPlaybackProperties [$sampleRateHz]Hz Ch[$numberAudioChannels]")
    }

    private fun audioChanelConfig(numAudioChIn:Int): Int  =  when (numAudioChIn) {
        1 -> AudioFormat.CHANNEL_OUT_MONO
        2 -> AudioFormat.CHANNEL_OUT_STEREO
        else -> {AudioFormat.CHANNEL_OUT_STEREO}
    }


    fun initializeOutputStream() {
//        var audioMode =
        var audioEncoding: Int = AudioFormat.ENCODING_PCM_FLOAT
        // Set and push to audio track..
        var minAudioBufferSize: Int = AudioTrack.getMinBufferSize(
            sampleRateHz,
            audioChanelConfig(numberAudioChannels),
            audioEncoding
        )

        OutputStream = AudioTrack(
            AudioManager.STREAM_MUSIC, sampleRateHz, audioChanelConfig(numberAudioChannels),
            audioEncoding, minAudioBufferSize, AudioTrack.MODE_STREAM
        )

        OutputStream = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(audioEncoding)
                    .setSampleRate(sampleRateHz)
                    .setChannelMask(audioChanelConfig(numberAudioChannels))
                    .build()
            )
            .setBufferSizeInBytes(minAudioBufferSize)
            .build()

        //TODO change the if statements to TRY CATCH blocks
        if (OutputStream != null) {
            OutputStream.play()
        } else Log.d(TAG, "$msgPrefix audio track is not initialised ")
    }

    open fun playAudioBuffer(F: ReastreamFrame) {
        F.audioSample = audioBufferReorder(F)// TODO reorder frame may be needed
        if (OutputStream != null) {
            // Write the byte array to the track
            OutputStream.write(
                F.audioSample,
                0,
                F.numSamples,
                AudioTrack.WRITE_NON_BLOCKING
            )
            if(DEBUG)Log.d(TAG,msgPrefix+" Play ${F.sampleByteSize} bytes as ${F.numSamples}")

        } else Log.d(TAG, "$msgPrefix audio track stopped in loop ")
    }

    fun audioBufferReorder(F: ReastreamFrame): FloatArray{
        var buffer = FloatArray(F.audioSample.size)
        var chSampLen = F.audioSample.size / F.numAudioChannels
        for (ch in 0 until F.numAudioChannels) {
            for (s in 0 until chSampLen) {
                buffer[s*F.numAudioChannels+ch] = F.audioSample[s+ch*chSampLen]
            }
        }

//        // Trim
//        buffer = F.audioSample.take(chSampLen).toFloatArray()
        return buffer
    }

    fun closeOutputStream() {
        if (OutputStream != null) {
            OutputStream.stop()
            OutputStream.release()
        } else Log.d(TAG, "$msgPrefix audio track can not deallocate ")
    }
}
