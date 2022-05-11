package com.example.reastreamreceiverandroidapp

// OS import
// UI view elements
// Audio Devices
//import android.util.Log

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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

// Debugging tags
const val TAG    = "ReaStreamReceiver"
const val sepTxt = "============================================="

// connection properties
class ReaperHostAddress {
    var hostIP: String = "192.168.0.101"
    var port: Int = 58710

    constructor()
    init{}
}

// Global connection properties initialization
val ConnectionProperties = ReaperHostAddress()

class UDP_listner: Runnable, MainActivity() {
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
                    bufferUnpack(packet)
                    packetCounter ++
                    Log.v(TAG, "open fun receiveUDP packet [$packetCounter] received = " + packet.data)
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

        val MRSR: String = ""//packet.data.copyofRange(0,3).
    }
}


open class MainActivity : AppCompatActivity() {

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

    //
    lateinit var  threadWithRunnable : Thread

    //***+++ Override methods +++***//
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG,sepTxt)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(TAG,"ReaStream Android app Main Activity Started")

        //on creation initialize the handles to the UI elements
        initializeHandles()

        //connection Setup
        preConnectionSetup()

    }

//    override fun onDestroy() {
//        // Call super on destroy
//        super.onDestroy();
//    }

    //***+++ Internal control methods +++***//

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

    // Pre connection setup
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

    // Load the web control web page
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
            var ListAudioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)+ audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            Log.i(TAG,sepTxt.replace("=","-"))
            Log.i(TAG,"Show Audio devices:")
            //  Print the device info
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
            Log.i(TAG,sepTxt.replace("=","-"))
        }

        Log.i(TAG,"Populate the spinner entries with the audio device data")
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
        // TODO: populate with the actual device values for the input and the output device

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
        // TODO("The callback needs to be connected to the UI elements for the device selection.")
        // TODO("Add sample rate boxes.")
        // TODO "make the web control UI dinamyc and make sliding page for it"

    }

    private fun startUDPlistner()
    {
        // todo add a listener check if the thread has already been initialized
        Log.i(TAG,"Create Runnable UDP listener.")
        threadWithRunnable = Thread(UDP_listner())
        threadWithRunnable.start()
        // TODO finish the udp listner
        Log.i(TAG,"UDP listener success.")
    }

    //***+++ Callback functions section +++***//

    // Connection callback function
    fun onSwitchToggleCb(view: View) {
        // TODO include debug messages in this function
        // Check if all inputs are there.
        if (controlWebView_top_atStart == 0){controlWebView_top_atStart = controlWebView.top}
        // Get the switch position
//        var isConnected:Boolean =
        if (connectionSwitchView.isChecked){
            controlWebView.top = controlWebView_top_atStart - 170*3//dp

            // Create the UDP listner
            startUDPlistner()

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


    //***+++ UDP connection functions  +++***//

    // Recieve UDP packet callback

    // todo for now ignore this function
    fun sendUDP(messageStr: String) {
        // Hack Prevent crash (sending should be done using an async task)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            //Open a port to send the package
            val socket = DatagramSocket()
            socket.broadcast = true
            val sendData = messageStr.toByteArray()
            val sendPacket = DatagramPacket(sendData, sendData.size, InetAddress.getByName(ConnectionProperties.hostIP), ConnectionProperties.port)
            socket.send(sendPacket)
            Log.v(TAG,"fun sendBroadcast: packet sent to: " + InetAddress.getByName(ConnectionProperties.hostIP) + ":" + ConnectionProperties.port)
        } catch (e: IOException) {
            //            Log.e(FragmentActivity.TAG, "IOException: " + e.message)
        }
    }

    fun clickButtonSend(view: View) {
        // todo fix this function
        // Do something in response to button
        // Send editText1 Text thru UDP.
        val editText = this.reastreamLabelView
        var message = editText.text.toString()
        sendUDP(message)
        // Add text to textView1.
//        val textView = findViewById<TextView>(R.id.textView1)
//        var chat = textView.text.toString()
//        textView.setText(chat + message + "\n")
        // Clear editText1 after all sent.
        editText.setText("")// Clear Input text.
    }



}


//////////////////////////////////////////////+++++++++++++++++++++++++++++++++++++++++++++++++++++

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
