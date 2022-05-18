package com.example.reastreamreceiverandroidapp

import android.os.SystemClock.sleep
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.charset.StandardCharsets

// connection properties
// Todo this will eventually have to be moved to more appropriate location and the IP (and just in case probably the port number ) has to be extracted from the UI
class ReaperHostAddress {
    var hostIP: String = "192.168.0.100"
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

    private val msgPrefix = "UDP Receiver:"

    lateinit var  AudioPlaybackProcessThreadWithRunnable : Thread

    override fun run() {
        // Thread callback message ID
        Log.i(TAG,"${Thread.currentThread()} $msgPrefix Thread Started")

        // Define all the variables and classes
        var socket: DatagramSocket? = null
        var buffer = ByteArray(2048)
        var frameCounter = 0

        // Audio output initialization as a separate process
//        startAudioProcessThread()
        //TODO test via simple run
        val audioOutput : audioPlaybackProcess = audioPlaybackProcess()

        // Retry to open the socket
        val maxRetryCount = 100
        var retryCount = maxRetryCount

        // Inside the loop is the effective receive UDP packet callback
        while (retryCount > 0 && UI.getIsConnectionSwitchStateON()){
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
                    // Check if the label matches
                    if (!(firstFrame.ReaStreamLabel == ListenReaStreamLabel)) {
                        Log.d(TAG,"$msgPrefix frame skip because [\"${firstFrame.ReaStreamLabel}\"] != [\"$ListenReaStreamLabel\"]")
                        continue
                    }
                    Log.d(TAG,"$msgPrefix first packet frame for setup [$frameCounter] received = $firstFrame")
                    // Pass the IP to the UI
                    UI.setHostIPTextView(firstPacket.address.toString())
                    // Pass the audio device info to the audio playback
                    audioOutput.setAudioPlaybackProperties(firstFrame)
                    // TODO: Start the audio thread in a separate process
                    audioOutput.initializeOutputStream()
                }
                else {
                    Log.d(TAG,"$msgPrefix first packet frame for setup [$frameCounter] received = NOT a ReastreamFrame")
                    continue
                }

                while (UI.getIsConnectionSwitchStateON()) {
                    // local to the loop packet and frame instances
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    if (isReaStreamFrame(packet))
                    {
                        val frame : ReastreamFrame = ReastreamFrame()
                        frame.unpackUDPdataStreamtoBuffer(packet)
                        if (frame.ReaStreamLabel == ListenReaStreamLabel) {
                            frameCounter++
                            if(DEBUG)Log.d(TAG, "$msgPrefix frame [$frameCounter] received = $frame")
                            //Todo Pass the audio frame to the audio playback listener
                            // todo code to audio here---+++
                            audioOutput.playAudioBuffer(frame)
                        }else{
                            Log.d(TAG,"$msgPrefix frame skip because [\"${frame.ReaStreamLabel}\"] != [\"$ListenReaStreamLabel\"]")
                            // TODO include some time delay
                        }
                    }
                    else{
                        Log.e(TAG,"$msgPrefix not a reastream packet")
                    }
                }

                 Log.v( TAG, "$msgPrefix UDP receiver stopped by UI.getIsConnectionSwitchStateON() = [${UI.getIsConnectionSwitchStateON()}]")
                // TODO inform the audio device to disconnect
                 audioOutput.closeOutputStream()
            } catch (e: Exception) {
                Log.e(TAG, "$msgPrefix catch exception.$e")
                e.printStackTrace()
            } finally {
                socket?.close()
            }

            // Decrease the retry count if the connection switch is turned off
            if (UI.getIsConnectionSwitchStateON()) {
                retryCount -= 1
                Log.v(TAG,"${Thread.currentThread()}  $msgPrefix Thread remaining retry to connect $retryCount / $maxRetryCount ")
                // Pause execution and wait a little
                sleep(100)
            }
        }
        Log.v(TAG,"${Thread.currentThread()}  $msgPrefix Thread Stopped")
    }

    fun isReaStreamFrame(packet : DatagramPacket) : Boolean {
        return (String(packet.data.sliceArray(0..3), StandardCharsets.UTF_8) == "MRSR")
    }

    private fun startAudioProcessThread() {
        // todo add a listener check if the thread has already been initialized
        Log.i(TAG,msgPrefix+"Create Audio Playback Process receiver.")
        AudioPlaybackProcessThreadWithRunnable = Thread(audioPlaybackProcess())
        Log.i(TAG,msgPrefix+"Start Audio Playback Process in separate thread.")
        AudioPlaybackProcessThreadWithRunnable.start()
    }

}