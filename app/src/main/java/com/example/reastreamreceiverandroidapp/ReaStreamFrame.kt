package com.example.reastreamreceiverandroidapp

import java.net.DatagramPacket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class ReastreamFrame {
    val MRSR: String = "MRSR"
    var packetSize: Int = 1247
    var ReaStreamLabel: String = ""
    var numAudioChannels: Int = 2
    var audioSampleRate: Int = 48000
    var sampleSize: Int = 1200
    var audioSample: IntArray = IntArray(sampleSize / 4)

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


    fun unpackUDPdataStreamtoBuffer(packet: DatagramPacket) {

        var p: Int = 0 + 4// Position offset
        val data: ByteArray = packet.data// Get the data byte buffer
//        BitConverter.toInt32(data.sliceArray(p+..p))
        packetSize = toInt32(data.sliceArray(p until p + 4))// Packet data length is 4 bytes
        packetSize = packet.length// this should be the same
        p += 4
        // Reastream Label
        ReaStreamLabel = String(packet.data.sliceArray(p until p + 32), StandardCharsets.US_ASCII).trim(Char(0))
        p += 32

        numAudioChannels = data[p].toInt()
        p += 1

        audioSampleRate = toInt32(data.sliceArray(p until p + 4))
        p += 4

        sampleSize = toInt32(data.sliceArray(p until p + 2)+ByteArray(2))
        p += 2

        audioSample = convertByteArrayToIntArray(data.sliceArray(p until p + sampleSize))

        // TODO do intiger conversion testing
//        val generatedArray = IntArray(10) { i -> i * i }
//        val numbers: IntArray = intArrayOf(10, 20, 30, 40, 50)
// todo remove this here
    }

    // TODO Eventually those conversion routines should be made in the most optimized and fast and efficient way possible
    fun toInt32(bytes: ByteArray): Int {
        if (bytes.size != 4) {
            throw Exception("wrong len")
        }
        bytes.reverse()
        return ByteBuffer.wrap(bytes).int
    }

    fun packDataStreamtoToBynaryStream(): ByteArray {
        var data: ByteArray = ByteArray(packetSize)
        MRSR.toByte() + packetSize.toByte() // TODO + add the rest of the properties in the correct order

//        val ints = intArrayOf(0x01, 0xFF)//todo example array, test the code below
        data += audioSample.foldIndexed(ByteArray(audioSample.size)) { i, a, v ->
            a.apply {
                set(
                    i,
                    v.toByte()
                )
            }
        }

        return data
    }


    fun convertByteArrayToInt(intBytes: ByteArray): Int {
        val byteBuffer = ByteBuffer.wrap(intBytes)
        return byteBuffer.int
    }

    fun convertByteArrayToIntArray(data: ByteArray): IntArray {
//        if (data == null || data.size % 4 != 0) return null
        // ----------
        val ints = IntArray(data.size / 4)
        for (i in ints.indices) ints[i] = convertByteArrayToInt(
            byteArrayOf(
                data[i * 4],
                data[i * 4 + 1],
                data[i * 4 + 2],
                data[i * 4 + 3]
            )
        )
        return ints
    }

    override fun toString(): String {
        return "lbl[$ReaStreamLabel] ch[$numAudioChannels] smpR[$audioSampleRate]Hz SML:$sampleSize of data[${audioSample[0]},...]"
    }
}