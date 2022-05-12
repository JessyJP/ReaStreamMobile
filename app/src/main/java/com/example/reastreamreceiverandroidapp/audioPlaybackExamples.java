package com.example.reastreamreceiverandroidapp;

// todo example playback class


        //출처: https://darksilber.tistory.com/61 [IT 개발 / 게임 / 일상]

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;




public class audioPlaybackExamples {
    public void PlayShortAudioFileViaAudioTrack(String filePath) throws IOException {
// We keep temporarily filePath globally as we have only two sample sounds now..
        if (filePath == null)
            return;

//Reading the file..
        byte[] byteData = null;
        File file = null;
        file = new File(filePath); // for ex. path= "/sdcard/samplesound.pcm" or "/sdcard/samplesound.wav"
        byteData = new byte[(int) file.length()];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(byteData);
            in.close();

        } catch (FileNotFoundException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
// Set and push to audio track..
        int intSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_8BIT);
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_8BIT, intSize, AudioTrack.MODE_STREAM);
        if (at != null) {
            at.play();
// Write the byte array to the track
            at.write(byteData, 0, byteData.length);
            at.stop();
            at.release();
        } else
            Log.d("TCAudio", "audio track is not initialised ");

    }


    private void PlayAudioFileViaAudioTrack2(String filePath) throws IOException {
// We keep temporarily filePath globally as we have only two sample sounds now..
        if (filePath == null)
            return;

        int intSize = android.media.AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM);


        if (at == null) {
            Log.d("TCAudio", "audio track is not initialised ");
            return;
        }

        int count = 512 * 1024; // 512 kb
//Reading the file..
        byte[] byteData = null;
        File file = null;
        file = new File(filePath);

        byteData = new byte[(int) count];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);

        } catch (FileNotFoundException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }

//        출처: https://darksilber.tistory.com/61 [IT 개발 / 게임 / 일상]
    }

}