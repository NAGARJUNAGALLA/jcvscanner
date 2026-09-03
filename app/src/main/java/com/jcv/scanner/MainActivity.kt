package com.jcv.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val CAMERA_PERMISSION_CODE = 100

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize WebView programmatically to fill the screen
        webView = WebView(this)
        setContentView(webView)

        // Request native Android Camera Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }

        // Configure WebView Settings
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        
        // Critical: Enable DOM storage so IndexedDB (ClearScannerProDB) can save documents
        settings.domStorageEnabled = true 
        settings.databaseEnabled = true
        
        // Allow loading local assets
        settings.allowFileAccess = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true
        
        // Prevent auto-pausing video streams (useful for scanner feed)
        settings.mediaPlaybackRequiresUserGesture = false

        webView.webViewClient = WebViewClient()
        
        // Handle HTML5 permissions (Camera for WebRTC stream)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                runOnUiThread {
                    // Automatically grant camera permissions to the WebView
                    if (request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                        request.grant(request.resources)
                    } else {
                        request.deny()
                    }
                }
            }
        }

        // Load the HTML file verbatim from the assets folder
        webView.loadUrl("file:///android_asset/jcvscanner.html")
    }
}
