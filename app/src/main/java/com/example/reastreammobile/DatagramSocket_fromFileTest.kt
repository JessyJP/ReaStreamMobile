package com.example.reastreammobile

import java.io.InputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


class DatagramSocket_fromFileTest(port: Int = 58710, UI_handle : MainActivity) : DatagramSocket(port) {
    val UI : MainActivity = UI_handle
    val ins: InputStream = UI.resources.openRawResource(R.raw.testsong1)
    // Alternative way
//        var ins: InputStream = UI.resources.openRawResource(
//            UI.resources.getIdentifier(
//                "testtext",
//                "raw", UI.packageName
//            )
//        )

    val allData : ByteArray = ins.readBytes()
    var limit : Int = allData.size;
    var counter : Int = 0;
    var buffer : ByteArray = ByteArray(1247)

//    var c : Int = 1

    override fun receive(packet: DatagramPacket){
        packet.port = 58710
        packet.address = InetAddress.getByName("192.168.0.120")

//        if (c.mod(4) == 1) {
//            ins.read(buffer,0,496+47)
//            c = 2
//        }
//        else{
//            ins.read(buffer,0,1200+47)
//            c += 1
//        }
//        packet.data = buffer


        val counterStart: Int = locateNextFrameStart(counter)
        val counterEnd: Int = locateNextFrameStart(counterStart+4)
        packet.data = allData.slice(counterStart until counterEnd).toByteArray()
        if (counterEnd == limit)counter=0

        counter += packet.length-1
    }

    private fun locateNextFrameStart(startFrom : Int): Int{
        for (i in startFrom until limit){
            if (getIndMRSR(i)) return i
        }
        return limit
    }

    private fun getIndMRSR(i: Int): Boolean {
        if (allData[i].toInt().toChar() == 'M') {
            if ((allData[i + 1].toInt().toChar() == 'R') &&
                (allData[i + 2].toInt().toChar() == 'S') &&
                (allData[i + 3].toInt().toChar() == 'R')
            ) {
                return true
            }
        }
        return false
    }
}