package com.example.reastreammobile

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log

// TODO to test "AudioPlaybackProcess" the class is made open
class AudioPlaybackProcess_test : AudioPlaybackProcess() {
    val Frames: ArrayList<ReastreamFrame> = ArrayList<ReastreamFrame>(500)

//    override fun playAudioBuffer(F: ReastreamFrame) {
//        // First gather 500 frames
//        if (Frames.size < 10000){
//            Frames.add(F)
//            Log.d(TAG,"Frame add ${Frames.size}")
////            Frames[Frames.size] = F
//            return
//        }
//
//        for (F in Frames){
//            super.playAudioBuffer(F)
//        }
//
//        Frames.clear()
//
//    }

//    var fbuff : FloatArray = FloatArray(0)
//    override fun playAudioBuffer(F: ReastreamFrame) {
//                // First gather 500 frames
//        if (Frames.size < 3000){
//            Frames.add(F)
//            Log.d(TAG,"Frame add ${Frames.size}")
//            fbuff += audioBufferReorder(F)
//            return
//        }
//
//
//        if (OutputStream != null) {
//            // Write the byte array to the track
//            OutputStream.write(
//                fbuff,
//                0,
//                fbuff.size,
//                AudioTrack.WRITE_NON_BLOCKING
//            )
//            if(DEBUG)Log.d(TAG,msgPrefix+" Play ${fbuff.size*4} bytes as ${fbuff.size}")
//
//        } else Log.d(TAG, "$msgPrefix audio track stopped in loop ")
//        fbuff =  FloatArray(0)
//    }

    override fun initializeOutputStream() {
//        var audioMode =
        var audioEncoding: Int = AudioFormat.ENCODING_PCM_FLOAT
        // Set and push to audio track..
        var minAudioBufferSize: Int = AudioTrack.getMinBufferSize(
            samplingRateHz,
            audioChanelConfig(numberAudioChannels),
            audioEncoding
        )
        minAudioBufferSize = samplingRateHz*10*4*numberAudioChannels

        OutputStream = AudioTrack(
            AudioManager.STREAM_MUSIC, samplingRateHz, audioChanelConfig(numberAudioChannels),
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
                    .setSampleRate(samplingRateHz)
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
}

