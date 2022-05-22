package com.example.reastreammobile

class UDP_transmitter(UI_handle : MainActivity): Runnable, MainActivity() {
    val UI : MainActivity = UI_handle
    var ListenReaStreamLabel : String = ""
    var audioOutputReady : Boolean  = false

    init{
        ListenReaStreamLabel = UI_handle.getReastreamLabel()
    }

    override fun run() {
        TODO("Not yet implemented")
    }

//    fun bufferPack(audioBuff: int[]) : DatagramPacket
//    {
//        return // TODO: implement the buffer pack function
//    }
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