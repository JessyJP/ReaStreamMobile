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
                var packet = DatagramPacket(buffer, buffer.size)
                //First packet to setup
                socket.receive(packet)
//                if (isReaStreamFrame(packet))  &
                bufferUnpack(packet)
                //todo do the first packet and get info to pass to the UI and the audio device setup

                while (true) {
                    packet = DatagramPacket(buffer, buffer.size)
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


// todo for now ignore this function
//fun sendUDP(messageStr: String) {
//    // Hack Prevent crash (sending should be done using an async task)
//    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//    StrictMode.setThreadPolicy(policy)
//    try {
//        //Open a port to send the package
//        val socket = DatagramSocket()
//        socket.broadcast = true
//        val sendData = messageStr.toByteArray()
//        val sendPacket = DatagramPacket(sendData, sendData.size, InetAddress.getByName(ConnectionProperties.hostIP), ConnectionProperties.port)
//        socket.send(sendPacket)
//        Log.v(TAG,"fun sendBroadcast: packet sent to: " + InetAddress.getByName(ConnectionProperties.hostIP) + ":" + ConnectionProperties.port)
//    } catch (e: IOException) {
//        //            Log.e(FragmentActivity.TAG, "IOException: " + e.message)
//    }
//}
//
//fun clickButtonSend(view: View) {
//    // todo fix this function
//    // Do something in response to button
//    // Send editText1 Text thru UDP.
//    val editText = this.reastreamLabelView
//    var message = editText.text.toString()
//    sendUDP(message)
//    // Add text to textView1.
////        val textView = findViewById<TextView>(R.id.textView1)
////        var chat = textView.text.toString()
////        textView.setText(chat + message + "\n")
//    // Clear editText1 after all sent.
//    editText.setText("")// Clear Input text.
//}