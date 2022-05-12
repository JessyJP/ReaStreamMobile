package com.example.reastreamreceiverandroidapp

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
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