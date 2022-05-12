package com.example.reastreamreceiverandroidapp

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

// connection properties
class ReaperHostAddress {
    var hostIP: String = "192.168.0.101"
    var port: Int = 58710

    constructor()
    init{}
}

// Global connection properties initialization
val ConnectionProperties = ReaperHostAddress()

class UDP_receiver(UI_handle : MainActivity): Runnable, MainActivity() {
    val UI : MainActivity = UI_handle
    var ListenReaStreamLabel : String = ""
    var audioOutputReady : Boolean  = false

    init{
        ListenReaStreamLabel = UI_handle.getReastreamLabel()
    }

    override fun run() {
        var msgPrefix = "Audio Receiver:"
        // Thread callback message
        Log.i(TAG,"${Thread.currentThread()} $msgPrefix Thread Started")
        // Retry to open the socket
        while (true){
            // Recieve UDP packet callback
            var buffer = ByteArray(2048)
            var socket: DatagramSocket? = null
            var packetCounter = 0
            try {
                //Keep a socket open to listen to all the UDP trafic that is destined for this port
                socket = DatagramSocket(ConnectionProperties.port)//, InetAddress.getByName(ConnectionProperties.hostIP)
                socket.broadcast = true
                var packet = DatagramPacket(buffer, buffer.size)
                //First packet to setup
                socket.receive(packet)
                var RSF_frame : ReastreamFrame = ReastreamFrame()
//                if (isReaStreamFrame(packet))  &
                RSF_frame.unpackUDPdataStreamtoBuffer(packet)
                //todo do the first packet and get info to pass to the UI and the audio device setup

                while (true) {
                    packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    if (isReaStreamFrame(packet))
                    {
                        RSF_frame.unpackUDPdataStreamtoBuffer(packet)
                        //Todo pass the audio frame "RSF_frame" to the audio playback listener
                        packetCounter++
                        Log.v(TAG,"$msgPrefix packet [$packetCounter] received = " + packet.data)
                    }
                    else{
                        Log.v(TAG,"$msgPrefix not a reastream packet")
                    }
                }
            } catch (e: Exception) {
                Log.v(TAG, "$msgPrefix catch exception.$e")
                e.printStackTrace()
            } finally {
                socket?.close()
            }
        }
        Log.i(TAG,"${Thread.currentThread()} Start Audio Receiver Thread")
    }



    fun isReaStreamFrame(packet : DatagramPacket) : Boolean {
        return (String(packet.data.sliceArray(0..3), StandardCharsets.UTF_8) == "MRSR")
    }


}

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

//        audioSample = data.sliceArray(p until p+sampleSize)
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