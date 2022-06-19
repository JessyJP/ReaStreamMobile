package com.example.reastreammobile

import java.net.DatagramPacket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class ReastreamFrame {
    private val MRSR: String = "MRSR"
    private var packetSize: Int = 1247
    var ReaStreamLabel: String = ""
    var numAudioChannels: Int = 2
    var audioSampleRate: Int = 48000
    var sampleByteSize: Int = 1200
    var audioSampleBytes : ByteArray = ByteArray(sampleByteSize)
    var numSamples = sampleByteSize/(4*numAudioChannels)
    var audioSample: FloatArray = FloatArray(sampleByteSize / 4)

//        %     typedef struct ReaStream
//        %     {
//        %     char ID[4]; // 'MRSR' tag for every packet like an ID (4 bytes)
//        %     unsigned int packetSize; // size of the entire UDP packet (4 bytes)
//        %     char ReastreamLabel[32]; // Name of the stream (ie: default on the plugin) (32 bytes)
//        %     unsigned int numAudioChannels; // the number of channels the plugin sends (1 byte)
//        %     unsigned int audioSampleRate; // the rate Frequency of the data (44100, 48000, ...) (4 bytes)
//        %     unsigned sampleByteSize; // size of the following bytes to read. (2 bytes)
//        %     float *datas; // start of the audio datas (variable get from "sampleByteSize")
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

        sampleByteSize = toInt32(data.sliceArray(p until p + 2)+ByteArray(2))
        p += 2

        audioSampleBytes = data.sliceArray(p until p + sampleByteSize)
        audioSample = convertByteArrayToFloatArray(audioSampleBytes)

        numSamples = sampleByteSize/(4*numAudioChannels)
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
                    v.toBits().toByte()
                )
            }
        }

        return data
    }

    fun convertByteArrayToFloat(floatBytes: ByteArray): Float {
        floatBytes.reverse()
        return ByteBuffer.wrap(floatBytes).float
    }

    fun convertByteArrayToFloatArray(data: ByteArray): FloatArray {
//        if (data == null || data.size % 4 != 0) return null
        // ----------
        val floats = FloatArray(data.size / 4)
        for (i in floats.indices) floats[i] = convertByteArrayToFloat(
            byteArrayOf(
                data[i * 4],
                data[i * 4 + 1],
                data[i * 4 + 2],
                data[i * 4 + 3]
            )
        )
        return floats
    }

    override fun toString(): String {
        return "lbl[$ReaStreamLabel] ch[$numAudioChannels] smpR[$audioSampleRate]Hz SML:${numSamples} of data[${audioSample[0]},...]"
    }

    fun audioBufferReorder(): FloatArray{
        var buffer = FloatArray(audioSample.size)
        var chSampLen = audioSample.size / numAudioChannels
        for (ch in 0 until numAudioChannels) {
            for (s in 0 until chSampLen) {
                buffer[s*numAudioChannels+ch] = audioSample[s+ch*chSampLen]
            }
        }

//        // Trim
//        buffer = F.audioSample.take(chSampLen).toFloatArray()
        return buffer
    }
}