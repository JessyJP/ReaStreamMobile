package com.example.reastreamreceiverandroidapp

import android.util.Log

// TODO to test "audioPlaybackProcess" the class is made open
class audioPlaybackProcess_test : audioPlaybackProcess() {
    val Frames: ArrayList<ReastreamFrame> = ArrayList<ReastreamFrame>(500)

    override fun playAudioBuffer(F: ReastreamFrame) {
        // First gather 500 frames
        if (Frames.size < 3*F.audioSampleRate/F.numSamples){
            Frames.add(F)
            Log.d(TAG,"Frame add ${Frames.size}")
//            Frames[Frames.size] = F
            return
        }

        for (F in Frames){
            super.playAudioBuffer(F)
        }

        Frames.clear()

    }
}