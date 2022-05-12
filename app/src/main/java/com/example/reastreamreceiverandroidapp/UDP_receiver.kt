package com.example.reastreamreceiverandroidapp

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.charset.StandardCharsets

// connection properties
// Todo this will eventually have to be moved to more appropriate location and the IP (and just in case probably the port number ) has to be extracted from the UI
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
    var ListenReaStreamLabel : String = UI_handle.getReastreamLabel()
    var audioOutputReady : Boolean  = false

    init{//TODO remove this init block
        ListenReaStreamLabel = UI_handle.getReastreamLabel()
    }

    override fun run() {
        // Thread callback message ID
        val msgPrefix = "UDP Receiver:"
        Log.i(TAG,"${Thread.currentThread()} $msgPrefix Thread Started")

        // Define all the variables and classes
        var socket: DatagramSocket? = null
        var buffer = ByteArray(2048)
        var frameCounter = 0

        // Retry to open the socket
        val maxRetryCount = 10
        var retryCount = maxRetryCount

        // Inside the loop is the effective receive UDP packet callback
        while (retryCount > 0){
             try {
                //Keep a socket open to listen to all the UDP trafic that is destined for this port
                socket = DatagramSocket(ConnectionProperties.port)//, InetAddress.getByName(ConnectionProperties.hostIP)
                socket.broadcast = true// TODO check if the broadcast is needed to get from all IPs on this port

                // First packet used for setup of the audio device
                val firstPacket = DatagramPacket(buffer, buffer.size)
                // Get the first packet from the socket
                socket.receive(firstPacket)


                // Check if the packet is a reastream frame
                if (isReaStreamFrame(firstPacket)) {
                    val firstFrame: ReastreamFrame = ReastreamFrame()
                    firstFrame.unpackUDPdataStreamtoBuffer(firstPacket)
                    Log.d(TAG,"$msgPrefix first packet frame for setup [$frameCounter] received = $firstFrame")
                    // Pass the IP to the UI
                    UI.setHostIPTextView(firstPacket.address.toString())
                    // TODO Pass the audio device info to the audio playback
                }
                else {
                    Log.d(TAG,"$msgPrefix first packet frame for setup [$frameCounter] received = NOT a ReastreamFrame")
                    continue
                }

                while (UI.getIsConnectionSwitchStateON()) {
                    val frame : ReastreamFrame = ReastreamFrame()
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    if (isReaStreamFrame(packet))
                    {
                        frame.unpackUDPdataStreamtoBuffer(packet)
                        //Todo pass the audio frame "RSF_frame" to the audio playback listener
                        frameCounter++
                        Log.d(TAG,"$msgPrefix frame [$frameCounter] received = $frame")
                        //Todo initial test to playback audio data
                    }
                    else{
                        Log.e(TAG,"$msgPrefix not a reastream packet")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "$msgPrefix catch exception.$e")
                e.printStackTrace()
            } finally {
                socket?.close()
            }
            // Decrease the retry count
            retryCount -= 1
            Log.v(TAG,"${Thread.currentThread()}  $msgPrefix Thread remaining retry to connect $retryCount / $maxRetryCount ")
        }
        Log.v(TAG,"${Thread.currentThread()}  $msgPrefix Thread Stopped")
    }



    fun isReaStreamFrame(packet : DatagramPacket) : Boolean {
        return (String(packet.data.sliceArray(0..3), StandardCharsets.UTF_8) == "MRSR")
    }


}