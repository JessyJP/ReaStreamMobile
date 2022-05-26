package com.example.reastreammobile

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
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textfield.TextInputLayout
import java.net.DatagramSocket
import java.net.InetSocketAddress
import kotlin.random.Random


// Debugging tags
const val TAG    = "ReaStreamMobile"
const val sepTxt = "============================================="
const val DEBUG=false
val UI_PROFILES = arrayOf("activity_main", "profile1")

open class MainActivity : AppCompatActivity() {
    // Debug message prefix
    private val msgPrefix = "Main Activity (UI):"

    // UI Setup Layout
    private var setupLayout:Int = 0

    // UI Element handles
    private lateinit var ip_addressView: TextInputLayout
    private lateinit var receiverSwitchView: SwitchCompat
    private lateinit var transmitterSwitchView: SwitchCompat
    private lateinit var reastreamLabelView: EditText
    private lateinit var outputDeviceListView: Spinner
    private lateinit var inputDeviceListView: Spinner
    private lateinit var controlURLView: TextView
    private lateinit var controlWebView: WebView


    // Thread
    private lateinit var  udpReceiverProcessThreadWithRunnable : Thread

    //***+++ Override methods +++***//
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG,sepTxt)
        super.onCreate(savedInstanceState)

        //on creation initialize the handles to the UI elements
        initializeSetContentViewAndHandles(UI_PROFILES[0])

        //connection Setup
        preConnectionSetup()

    }

    override fun onDestroy() {
        // TODO this can determine if it will be Kept in the back ground
        // First Disconnect
        receiverSwitchView.isChecked    = false
        transmitterSwitchView.isChecked = false

        // Call super on destroy
        super.onDestroy()
    }

    //***+++ Internal control methods +++***//

    fun getViewID(ProfileName: String,id:String):Int { return resources.getIdentifier("${id}_$ProfileName", "id", packageName)}

    // Initialize UI element handles and loading of the web control page
    private fun initializeSetContentViewAndHandles(ProfileName:String = UI_PROFILES[0]) {

        if (UI_PROFILES[0] != ProfileName){
            setupLayout = R.layout.activity_main_profile1//TODO this has to be acquired by a string
            setContentView(setupLayout)
            Log.i(TAG,msgPrefix+"ReaStream Android app Main Activity Started with [$ProfileName]")

            // Create handles on initial loading of the app
            ip_addressView        = findViewById(getViewID(ProfileName,"serverIP_portView"))
            receiverSwitchView    = findViewById(getViewID(ProfileName,"switchConnectReceiver"))
            transmitterSwitchView = findViewById(getViewID(ProfileName,"switchConnectTransmitter"))
            reastreamLabelView    = findViewById(getViewID(ProfileName,"reastreamReceiverFrameLabelView"))
            outputDeviceListView  = findViewById(getViewID(ProfileName,"outputDeviceListView"))
            inputDeviceListView   = findViewById(getViewID(ProfileName,"inputDeviceListView"))
            controlWebView        = findViewById(getViewID(ProfileName,"webControlView"))
            controlURLView        = findViewById(getViewID(ProfileName,"webControlURLinputView"))
        }
        else {
            setupLayout = R.layout.activity_main
            setContentView(setupLayout)
            Log.i(TAG,msgPrefix+"ReaStream Android app Main Activity Started")

            // Create handles on initial loading of the app
            ip_addressView        = findViewById(R.id.serverIP_portView)
            receiverSwitchView    = findViewById(R.id.switchConnectReceiver)
            transmitterSwitchView = findViewById(R.id.switchConnectTransmitter)
            reastreamLabelView    = findViewById(R.id.reastreamReceiverFrameLabelView)
            outputDeviceListView  = findViewById(R.id.outputDeviceListView)
            inputDeviceListView   = findViewById(R.id.inputDeviceListView)
            controlWebView        = findViewById(R.id.webControlView)
            controlURLView        = findViewById(R.id.webControlURLinputView)
        }
        Log.i(TAG,msgPrefix+"Initialize UI handles with [$ProfileName]")
    }

    // Pre connection setup
    private fun preConnectionSetup() {
        Log.i(TAG,msgPrefix+"Do pre connection setup")
        // Enable javaScript
        Log.i(TAG,msgPrefix+"controlWebView.settings.javaScriptEnabled = true")
        controlWebView.settings.javaScriptEnabled = true
        // Create a web view for the web control page
        Log.i(TAG,msgPrefix+"Create web client")
        controlWebView.webViewClient = WebViewClient()
        // Load the web control page
        loadWebControlPage()

        // Get audio devices and populate the dropdowns
        getAudioDevices()
    }

    // Load the web control web page
    private fun loadWebControlPage() {
        Log.i(TAG,msgPrefix+"Load the web control web page")
        val url: String = controlURLView.text.toString()
        controlWebView.loadUrl(url)
    }

    // Get the available audio device
    private fun getAudioDevices() {
        // Get audio devices
        Log.i(TAG,msgPrefix+"Initialize the audio manager")
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var ListAudioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)+ audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            Log.i(TAG,sepTxt.replace("=","-"))
            Log.i(TAG,msgPrefix+"Show Audio devices:")
            //  Print the device info
            for (dev in ListAudioDevices) {
                Log.i(
                    TAG,msgPrefix+
                    dev.productName.toString() + " " +
                            "IN[" + dev.isSink + "]  " +
                            "OUT[" + dev.isSource + "] "
                )
                for (ch in dev.channelCounts) {
                    Log.i(TAG,msgPrefix+ "    + Ch[" + ch + "]) ")
                }
                for (sr in dev.sampleRates) {
                    Log.i(TAG,msgPrefix+ "        + SR[$sr]Hz ")
                }
            }
            Log.i(TAG,sepTxt.replace("=","-"))
        }

        Log.i(TAG,msgPrefix+"Populate the spinner entries with the audio device data")
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

    private fun startUDPlistnerProcessThread() {
        // The variable is reinitialized on every call "udpReceiverProcessThreadWithRunnable"
        // TODO: maybe the variable "udpReceiverProcessThreadWithRunnable" is unnecessary
        Log.i(TAG,msgPrefix+"Create UDP receiver process.")
        udpReceiverProcessThreadWithRunnable = Thread(UDP_receiver(this))
        Log.i(TAG,msgPrefix+"Start UDP receiver process in separate thread.")
        udpReceiverProcessThreadWithRunnable.start()
        // TODO a handler is needed to ensure the Switch is returned to off position once the Thread finishes
    }

    //***+++ Get & Set Methods +++***//
    fun getReastreamLabel(): String =  reastreamLabelView.text.toString()

    fun setHostIPTextView(hostIP:String){
        ip_addressView.hint = hostIP
    }

    fun getIsReceiverSwitchStateON(): Boolean{
        return receiverSwitchView.isChecked
    }

    fun getIsTransmitterSwitchStateON(): Boolean{
        return transmitterSwitchView.isChecked
    }

    internal fun setReceiverConnectionSwitchState(state: Boolean){
        receiverSwitchView.isChecked = state
        Log.d(TAG, "$msgPrefix (auto) set Receiver Connection Switch State = $state.")
    }

    //***+++ Callback functions section +++***//

    // Connection callback function
    fun onReceiverSwitchToggleCb(view: View) {
        // Based on the switch position
        if (receiverSwitchView.isChecked){

            // Create the UDP listner
            startUDPlistnerProcessThread()

            val params = controlWebView.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = ip_addressView.id
//                params.topMargin = 10
                  controlWebView.requestLayout()
            Log.d(TAG, "$msgPrefix Switch web control view to layout/constraint for expanded state")

        }
        else {
            val params = controlWebView.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = controlURLView.id
            controlWebView.requestLayout()
            Log.d(TAG, "$msgPrefix Switch to disconnected view layout/constraint")
        }
    }

    fun onTransmitterSwitchToggleCb(view: View){
        udpTest()
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


    fun udpTest(){

        for (i4 in 0 until 256) {
            var ipStr: String = "192.168.0.$i4"
            val s = DatagramSocket(null)
            try {
//                val address = InetSocketAddress(ConnectionProperties.localHost, 30345)

                s.bind(InetSocketAddress(ipStr, Random.nextInt(10000, 65536)))
                Thread.sleep(10)
                Log.d(TAG, "$msgPrefix\n---------\n Try: ${ipStr}")
                Log.d(TAG, msgPrefix + "SUCCESS  BIND!!! to ${s.localAddress}:"+s.localPort)
//                println(TAG + msgPrefix + "SUCCESS!!!! BIND!!! to ${s.localAddress}")
                s.close()
            } catch (e: Exception) {
                e.printStackTrace()
                s.close()
//                Log.d(TAG, msgPrefix + "ERROR BIND!!! to ${s.localAddress}")
            }
        }
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
