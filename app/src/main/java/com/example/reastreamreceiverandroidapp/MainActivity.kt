package com.example.reastreamreceiverandroidapp

// OS import
// UI view elements
// Audio Devices
//import android.util.Log

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


open class MainActivity : AppCompatActivity() {
    private val TAG = "ReaStreamReceiver"
    private val sepTxt = "============================================="

    // UI Element handles
    private lateinit var ip_addressView: TextInputLayout
    private lateinit var connectionSwitchView: Switch
    private lateinit var reastreamLabelView: EditText
    private lateinit var outputDeviceListView: Spinner
    private lateinit var inputDeviceListView: Spinner
    private lateinit var controlURLView: TextView
    private lateinit var controlWebView: WebView

    // Internal variables
    private var controlWebView_top_atStart = 0


    //*** Override methods ***//
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG,sepTxt)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(TAG,"ReaStream Android app Main Activity Started")

        //on creation initialize the handles to the UI elements
        initializeHandles()

        //connection Setup
        preConnectionSetup()


        ////////////++++++++++++++++++++++++++//////////// UDP TEST
        testUDP()
        ////////////++++++++++++++++++++++++++//////////// UDP TEST
    }

//    override fun onDestroy() {
//        // Call super on destroy
//        super.onDestroy();
//    }

    //*** Internal control methods ***//

    // Initialize UI element handles and loading of the web control page

    private fun initializeHandles() {
        Log.i(TAG,"Initialize UI handles")
        // Create handles on initial loading of the app
        ip_addressView =  findViewById(R.id.serverIP_portInputView)
        connectionSwitchView = findViewById(R.id.switchConnect)
        reastreamLabelView = findViewById(R.id.reastreamLabelInputView)
        outputDeviceListView = findViewById(R.id.outputDeviceListView)
        inputDeviceListView = findViewById(R.id.inputDeviceListView)
        controlWebView = findViewById(R.id.webControlView)
        controlURLView = findViewById(R.id.webControlURLinputView)

    }

    private fun preConnectionSetup() {
        Log.i(TAG,"Do pre connection setup")
        // Enable javaScript
        Log.i(TAG,"controlWebView.settings.javaScriptEnabled = true")
        controlWebView.settings.javaScriptEnabled = true
        // Create a web view for the web control page
        Log.i(TAG,"Create web client")
        controlWebView.webViewClient = WebViewClient()
        // Load the web control page
        loadWebControlPage()

        // Get audio devices and populate the dropdowns
        getAudioDevices()
    }

    private fun loadWebControlPage() {
        Log.i(TAG,"Load the web control web page")
        val url: String = controlURLView.text.toString()
        controlWebView.loadUrl(url)
    }

    // Get the available audio device
    private fun getAudioDevices() {
        // Get audio devices
        Log.i(TAG,"Initialize the audio manager")
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var ListAudioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)+audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            Log.i(TAG,sepTxt.replace("=","-"))
            Log.i(TAG,"Show Audio devices:")

            fun printAudioDeviceList(ListAudioDevices : Array<AudioDeviceInfo>) {
                for (dev in ListAudioDevices) {
                    Log.i(
                        TAG,
                        dev.productName.toString() + " " +
                                "IN[" + dev.isSink + "]  " +
                                "OUT[" + dev.isSource + "] "
                    )
                    for (ch in dev.channelCounts) {
                        Log.i(TAG, "    + Ch[" + ch + "]) ")
                    }
                    for (sr in dev.sampleRates) {
                        Log.i(TAG, "        + SR[$sr]Hz ")
                    }
                }
            }
            printAudioDeviceList(ListAudioDevices )
            Log.i(TAG,sepTxt.replace("=","-"))
        }

        Log.i(TAG,"Populate the spinner enteis with the audio device data")
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.defaultOuputDeviceList,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            outputDeviceListView.adapter = adapter
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.defaultInputDeviceList,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            inputDeviceListView.adapter = adapter
        }

//        class SpinnerActivity : Activity(), AdapterView.OnItemSelectedListener {
//
//            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
//                // An item was selected. You can retrieve the selected item using
//                // parent.getItemAtPosition(pos)
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // Another interface callback
//            }
//        }

        //spinner.onItemSelectedListener = this

    }


    //*** Callback functions section ***//

    // Connection callback function
    fun onSwitchToggleCb(view: View) {
        // Check if all inputs are there.
        if (controlWebView_top_atStart == 0){controlWebView_top_atStart = controlWebView.top}
        // Get the switch position
//        var isConnected:Boolean =
        if (connectionSwitchView.isChecked){
            controlWebView.top = controlWebView_top_atStart - 170*3//dp
            // **********
            testUDP()
        }
        else {
            controlWebView.top = controlWebView_top_atStart
        }



//        connectionSwitchView.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//                // The toggle is enabled
//            } else {
//                // The toggle is disabled
//            }
//        }


    }

    fun onControlURLChangeCb(view: View) {
       this.loadWebControlPage()
    }

    fun onReaStreamLabelChangeCb(view: View) {}


    fun testUDP()
    {
        Log.i(TAG,"Create Runnable example.")
        val threadWithRunnable = Thread(udp_DataArrival())
        threadWithRunnable.start()

        // Add text to textView1.
        val textView = findViewById<TextView>(R.id.textViewDebugTest)
        textView.text = "Hello World from main!\n"

        Log.i(TAG,"MainActivity onCreate success.")
    }

   // ********** AUDIO device selection
//    https://www.twilio.com/blog/easily-manage-audio-devices-on-android-with-audioswitch
    fun onOutputDeviceClick(view: View) {}

    fun onInputDeviceClick(view: View) {
//        if(shouldEnableExternalSpeaker) {
//            if(isBlueToothConnected) {
//                // 1. case - bluetooth device
//                mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//                mAudioManager.startBluetoothSco();
//                mAudioManager.setBluetoothScoOn(true);
//            } else {
//                // 2. case - wired device
//                mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//                mAudioManager.stopBluetoothSco();
//                mAudioManager.setBluetoothScoOn(false);
//                mAudioManager.setSpeakerphoneOn(false);
//            }
//        } else {
//            // 3. case - phone speaker
//            mAudioManager.setMode(AudioManager.MODE_NORMAL);
//            mAudioManager.stopBluetoothSco();
//            mAudioManager.setBluetoothScoOn(false);
//            mAudioManager.setSpeakerphoneOn(true);
//        }
    }
//////////////////////////////////////////////////////////////
    // UDP


    fun clickButtonSend(view: View) {
        // Do something in response to button
        // Send editText1 Text thru UDP.
        val editText = findViewById<EditText>(R.id.editText1)
        var message = editText.text.toString()
        sendUDP(message)
        // Add text to textView1.
//        val textView = findViewById<TextView>(R.id.textView1)
//        var chat = textView.text.toString()
//        textView.setText(chat + message + "\n")
        // Clear editText1 after all sent.
        editText.setText("")// Clear Input text.
    }

    fun sendUDP(messageStr: String) {
        // Hack Prevent crash (sending should be done using an async task)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            //Open a port to send the package
            val socket = DatagramSocket()
            socket.broadcast = true
            val sendData = messageStr.toByteArray()
            val sendPacket = DatagramPacket(sendData, sendData.size, InetAddress.getByName(Settings.RemoteHost), Settings.RemotePort)
            socket.send(sendPacket)
            println("fun sendBroadcast: packet sent to: " + InetAddress.getByName(Settings.RemoteHost) + ":" + Settings.RemotePort)
        } catch (e: IOException) {
            //            Log.e(FragmentActivity.TAG, "IOException: " + e.message)
        }
    }

    open fun receiveUDP() {
        val buffer = ByteArray(2048)
        var socket: DatagramSocket? = null
        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            socket = DatagramSocket(Settings.RemotePort, InetAddress.getByName(Settings.RemoteHost))
            socket.broadcast = true
            val packet = DatagramPacket(buffer, buffer.size)
            socket.receive(packet)
            println("open fun receiveUDP packet received = " + packet.data)

        } catch (e: Exception) {
            println("open fun receiveUDP catch exception." + e.toString())
            e.printStackTrace()
        } finally {
            socket?.close()
        }
    }

}


class BluetoothReceiver : BroadcastReceiver() {
    private var localAudioManager: AudioManager? = null
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "onReceive - BluetoothBroadcast")
        localAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        val action = intent.action
        if (action == ACTION_BT_HEADSET_STATE_CHANGED) {
            val extraData = intent.getIntExtra(EXTRA_STATE, STATE_DISCONNECTED)
            if (extraData == STATE_DISCONNECTED) {
                //no headset -> going other modes
                localAudioManager!!.isBluetoothScoOn = false
                localAudioManager!!.stopBluetoothSco()
                localAudioManager!!.mode = AudioManager.MODE_NORMAL
                Log.i(TAG, "Bluetooth Headset Off " + localAudioManager!!.mode)
                Log.i(
                    TAG,
                    "A2DP: " + localAudioManager!!.isBluetoothA2dpOn + ". SCO: " + localAudioManager!!.isBluetoothScoAvailableOffCall
                )
            } else {
                localAudioManager!!.mode = AudioManager.MODE_CALL_SCREENING
                localAudioManager!!.isBluetoothScoOn = true
                localAudioManager!!.startBluetoothSco()
                localAudioManager!!.mode = AudioManager.MODE_IN_CALL
                Log.i(TAG, "Bluetooth Headset On " + localAudioManager!!.mode)
                Log.i(
                    TAG,
                    "A2DP: " + localAudioManager!!.isBluetoothA2dpOn + ". SCO: " + localAudioManager!!.isBluetoothScoAvailableOffCall
                )
            }
        }
        if (action == ACTION_BT_HEADSET_FORCE_ON) {
            localAudioManager!!.mode = AudioManager.MODE_CALL_SCREENING
            localAudioManager!!.isBluetoothScoOn = true
            localAudioManager!!.startBluetoothSco()
            localAudioManager!!.mode = AudioManager.MODE_IN_CALL
            Log.i(TAG, "Bluetooth Headset On " + localAudioManager!!.mode)
            Log.i(
                TAG,
                "A2DP: " + localAudioManager!!.isBluetoothA2dpOn + ". SCO: " + localAudioManager!!.isBluetoothScoAvailableOffCall
            )
        }
        if (action == ACTION_BT_HEADSET_FORCE_OFF) {
            localAudioManager!!.isBluetoothScoOn = false
            localAudioManager!!.stopBluetoothSco()
            localAudioManager!!.mode = AudioManager.MODE_NORMAL
            Log.i(TAG, "Bluetooth Headset Off " + localAudioManager!!.mode)
            Log.i(
                TAG,
                "A2DP: " + localAudioManager!!.isBluetoothA2dpOn + ". SCO: " + localAudioManager!!.isBluetoothScoAvailableOffCall
            )
        }
    }

    companion object {
        private const val STATE_DISCONNECTED = 0x00000000
        private const val EXTRA_STATE = "android.bluetooth.headset.extra.STATE"
        private const val TAG = "BluetoothReceiver"
        private const val ACTION_BT_HEADSET_STATE_CHANGED =
            "android.bluetooth.headset.action.STATE_CHANGED"
        private const val ACTION_BT_HEADSET_FORCE_ON = "android.bluetooth.headset.action.FORCE_ON"
        private const val ACTION_BT_HEADSET_FORCE_OFF = "android.bluetooth.headset.action.FORCE_OFF"
    }
}

//
//Lines:
//
//audioManager.isSpeakerphoneOn = true
//audioManager.isSpeakerphoneOn = false
//
//will not work. You have to use setMethods:
//
//Case 1 - bluetooth
//
//mAudioManager.startBluetoothSco(); // This method can be used by applications wanting to send and received audio to/from a bluetooth SCO headset while the phone is not in call.
//mAudioManager.setBluetoothScoOn(true); // set true to use bluetooth SCO for communications; false to not use bluetooth SCO for communications
//
//Case 2 - wired
//
//mAudioManager.stopBluetoothSco(); // stop wanting to send and receive
//mAudioManager.setBluetoothScoOn(false); // turn off bluetooth
//mAudioManager.setSpeakerphoneOn(false); //set true to turn on speakerphone; false to turn it off
//
//Case 3 - internal speaker
//
//mAudioManager.stopBluetoothSco();
//mAudioManager.setBluetoothScoOn(false);
//mAudioManager.setSpeakerphoneOn(true); // turn on speaker phone




class SoftOptions {
    var RemoteHost: String = "192.168.1.255"
    var RemotePort: Int = 6454

    constructor()
    init{}
}


// Global
val Settings = SoftOptions()

class udp_DataArrival: Runnable, MainActivity() {
    override fun run() {
        println("${Thread.currentThread()} Runnable Thread Started.")
        while (true){
            receiveUDP()
        }
    }
}