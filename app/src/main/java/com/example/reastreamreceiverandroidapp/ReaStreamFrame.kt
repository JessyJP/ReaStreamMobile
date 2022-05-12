package com.example.reastreamreceiverandroidapp

import java.net.DatagramPacket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class ReastreamFrame {
    val MRSR : String = "MRSR"
    var packetSize : Int = 1247
    var ReaStreamLabel : String = ""
    var numAudioChannels: Int = 2
    var audioSampleRate: Int = 48000
    var sampleSize: Int = 1200
    var audioSample: IntArray = IntArray(sampleSize/4)

//        %     typedef struct ReaStream
//        %     {
//        %     char ID[4]; // 'MRSR' tag for every packet like an ID (4 bytes)
//        %     unsigned int packetSize; // size of the entire UDP packet (4 bytes)
//        %     char ReastreamLabel[32]; // Name of the stream (ie: default on the plugin) (32 bytes)
//        %     unsigned int numAudioChannels; // the number of channels the plugin sends (1 byte)
//        %     unsigned int audioSampleRate; // the rate Frequency of the data (44100, 48000, ...) (4 bytes)
//        %     unsigned sampleSize; // size of the following bytes to read. (2 bytes)
//        %     float *datas; // start of the audio datas (variable get from "sampleSize")
//        %     } ReaStream;


    fun unpackUDPdataStreamtoBuffer(packet : DatagramPacket)
    {

        var p: Int = 0+4// Position offset
        val data : ByteArray= packet.data// Get the data byte buffer
//        BitConverter.toInt32(data.sliceArray(p+..p))
        packetSize = toInt32(data.sliceArray(p until p+4))// Packet data length is 4 bytes
        packetSize = packet.length// this should be the same
        p += 4
        // Reastream Label
        ReaStreamLabel = String(packet.data.sliceArray(p until p+32), StandardCharsets.UTF_8)
        p += 32

        numAudioChannels = data[p].toInt()
        p += 1

        audioSampleRate = toInt32(data.sliceArray(p until p+4))
        p += 4

        sampleSize = toInt32(data.sliceArray(p until p+4))
        p += 4

//        audioSample = IntArray(data.sliceArray(p until p+sampleSize))

    }

    fun toInt32(bytes:ByteArray):Int {
        if (bytes.size != 4) {
            throw Exception("wrong len")
        }
        bytes.reverse()
        return ByteBuffer.wrap(bytes).int
    }

    fun packDataStreamtoToBynaryStream():ByteArray
    {
        var data : ByteArray = ByteArray(packetSize)
        MRSR.toByte() + packetSize.toByte() // TODO + add the rest of the
        return data
    }
}