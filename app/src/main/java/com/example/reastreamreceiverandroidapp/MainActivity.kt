package com.example.reastreamreceiverandroidapp

// UI view elements
// Audio Devices


import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    // UI Element handles
    private lateinit var ip_addressView: TextInputEditText
    private lateinit var connectionSwitchView: Switch
    private lateinit var reastreamLabelView: EditText
    private lateinit var outputDeviceListView: Spinner
    private lateinit var inputDeviceListView: Spinner
    private lateinit var controlURLView: TextView
    private lateinit var controlWebView: WebView

    // Internal variables


    //*** Override methods ***//
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //on creation initialize the handles to the UI elements
        initializeHandles()

        //connection Setup
        connectionSetup()

        // setup all the callbackfunctions
        setupCallbacks()
    }

//    override fun onDestroy() {
//        // Call super on destroy
//        super.onDestroy();
//    }

    //*** Internal control methods ***//

    // Initialize UI element handles and loading of the web control page
    private fun initializeHandles() {
        // Create handles on initial loading of the app
        controlWebView = findViewById(R.id.webControlView)
        controlURLView = findViewById(R.id.webControlURLinputView)
        outputDeviceListView = findViewById(R.id.outputDeviceListView)
        inputDeviceListView = findViewById(R.id.inputDeviceListView)

    }

    private fun connectionSetup() {
        // Enable javaScript
        controlWebView.settings.javaScriptEnabled = true
        // Create a web view for the web control page
        controlWebView.webViewClient = WebViewClient()
        // Load the web control page
        loadWebControlPage()
    }

    private fun loadWebControlPage() {
        val url: String = controlURLView.text.toString()
        //  controlWebView.loadUrl("http://192.168.0.100:12121/");
        // to avoid having to use the IPs internet permissions should be granted to be able to resolve the IP
        controlWebView.loadUrl(url)

    }

    // Get the available audio device
    fun getAudioDevices() {
        // Get audio devices
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val adi = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            println(adi)
        }
    }


    fun setupCallbacks() {

    }

    //*** Callback functions section ***//

    // Connection callback function
    fun onSwitchToggleCb(view: View) {
        // Get the switch position

        // Check if all inputs are there.
        controlWebView
    }

    fun onControlURLChangeCb(view: View) {

    }

    fun onReaStreamLabelChangeCb(view: View) {}

    fun onOutputDeviceClick(view: View) {}

    fun onInputDeviceClick(view: View) {}


}