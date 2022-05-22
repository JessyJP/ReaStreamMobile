package com.example.reastreamreceiverandroidapp

import android.util.Log
import java.io.InputStream
import java.net.DatagramPacket
import java.net.DatagramSocket


class DatagramSocket_fromFileTest(port: Int = 58710, UI_handle : MainActivity) : DatagramSocket(port) {
    val UI : MainActivity = UI_handle
    var ins: InputStream = UI.resources.openRawResource(R.raw.textsong)
    // Alternative way
//        var ins: InputStream = UI.resources.openRawResource(
//            UI.resources.getIdentifier(
//                "testtext",
//                "raw", UI.packageName
//            )
//        )

    var buf : ByteArray = ByteArray(1247)

    override fun receive(packet: DatagramPacket){
        packet.port = 58710
//        packet.address = InetAddress()




        Log.d(TAG,"!!!!!! AVILABLE BITES = ${ins.available()}")


        buf = ins.readBytes()
        Log.e(TAG,"\n\n\n")
    }
}