package com.jcv.scanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private WebView webView;
    private static final int CAMERA_PERMISSION_CODE = 100;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Initialize WebView programmatically
            webView = new WebView(this);
            setContentView(webView);

            // Request native Android Camera Permission 
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            }

            // Configure WebView Settings
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);
            settings.setAllowFileAccess(true);
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
            settings.setMediaPlaybackRequiresUserGesture(false);

            webView.setWebViewClient(new WebViewClient());
            
            // Handle HTML5 permissions (Camera for WebRTC stream)
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onPermissionRequest(final PermissionRequest request) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boolean hasVideo = false;
                            for (String resource : request.getResources()) {
                                if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) {
                                    hasVideo = true;
                                    break;
                                }
                            }
                            if (hasVideo) {
                                request.grant(request.getResources());
                            } else {
                                request.deny();
                            }
                        }
                    });
                }
            });

            // Load the HTML file
            webView.loadUrl("file:///android_asset/jcvscanner.html");

        } catch (Exception e) {
            // Failsafe: If the app crashes, show the error on screen instead of force closing
            new AlertDialog.Builder(this)
                .setTitle("Startup Error")
                .setMessage(e.toString())
                .setPositiveButton("OK", null)
                .show();
        }
    }
}
