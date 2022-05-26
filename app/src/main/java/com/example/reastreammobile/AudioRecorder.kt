package com.example.reastreammobile

import android.media.AudioRecord
import android.media.AudioTrack

class AudioRecorder : Runnable {
    val msgPrefix = "Audio Recorder:"
    private var UDP_transmitterConnected : Boolean = false

    private var propertiesSet_FLAG :Boolean = false
    internal var sampleRateHz : Int = 48000
    internal var numberAudioChannels : Int = 2

    private var playbackDelay = 0

    lateinit var OutputStream: AudioRecord

    //    BufferArray
    var byteData : ByteArray = ByteArray(0)
    override fun run() {
        TODO("Not yet implemented")
    }

}