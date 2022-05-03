package com.example.reastreamreceiverandroidapp

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initial loading of the web control page
        val controlWebView: WebView = findViewById(R.id.webControlView)
        val controlURLView: TextView = findViewById(R.id.webControlURLinputView)

        controlWebView.webViewClient = WebViewClient()
        controlWebView.settings.javaScriptEnabled = true
        val url: String = controlURLView.text.toString()
//        controlWebView.loadUrl("http://192.168.0.100:12121/");
        // to avoid having to use the IPs internet permissions should be granted to be able to resolve the IP
        // "<uses-permission android:name="android.permission.INTERNET"/>"
        //
        controlWebView.loadUrl(url)


    }
}