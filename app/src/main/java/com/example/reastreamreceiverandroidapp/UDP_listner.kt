package com.example.reastreamreceiverandroidapp

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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

class UDP_listner(UI : AppCompatActivity): Runnable, MainActivity() {
    override fun run() {
        // Thread callback message
        Log.i(TAG,"${Thread.currentThread()} Runnable Thread Started.")
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
                while (true) {
                    var packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    if (isReaStreamFrame(packet))
                    {
                        bufferUnpack(packet)
                        packetCounter++
                        Log.v(TAG,"open fun receiveUDP packet [$packetCounter] received = " + packet.data)
                    }
                    else{
                        Log.v(TAG,"not a reastream packet")
                    }
                }
            } catch (e: Exception) {
                Log.v(TAG, "open fun receiveUDP catch exception.$e")
                e.printStackTrace()
            } finally {
                socket?.close()
            }
        }
    }

    fun bufferUnpack(packet : DatagramPacket)
    {
//        %     typedef struct ReaStream
//        %     {
//        %     char ID[4]; // 'MRSR' tag for every packet like an ID (4 bytes)
//        %     unsigned int packetSize; // size of the entire UDP packet (4 bytes)
//        %     char name[32]; // Name of the stream (ie: default on the plugin) (32 bytes)
//        %     unsigned int nbChan; // the number of channels the plugin sends (1 byte)
//        %     unsigned int freq; // the rate Frequency of the data (44100, 48000, ...) (4 bytes)
//        %     unsigned datasSize; // size of the following bytes to read. (2 bytes)
//        %     float *datas; // start of the audio datas (variable get from "datasSize")
//        %     } ReaStream;
        var p: Int = 0+4// Position offset
        val data : ByteArray= packet.data// Get the data byte buffer
//        BitConverter.toInt32(data.sliceArray(p+..p))
        var L = toInt32(data.sliceArray(p until p+4))// Packet data length is 4 bytes
        L = packet.length// this should be the same
        p += 4
        // Reastream Label
        val ReaStreamLabel: String = String(packet.data.sliceArray(p until p+32), StandardCharsets.UTF_8)
        p += 32

        val numAudioChannels: Int = data[p].toInt()
        p += 1

        val audioSampleRate: Int = toInt32(data.sliceArray(p until p+4))
    }

    fun isReaStreamFrame(packet : DatagramPacket) : Boolean {
        return (String(packet.data.sliceArray(0..3), StandardCharsets.UTF_8) == "MRSR")
    }

    fun toInt32(bytes:ByteArray):Int {
        if (bytes.size != 4) {
            throw Exception("wrong len")
        }
        bytes.reverse()
        return ByteBuffer.wrap(bytes).int
    }

    class audioFrame
}