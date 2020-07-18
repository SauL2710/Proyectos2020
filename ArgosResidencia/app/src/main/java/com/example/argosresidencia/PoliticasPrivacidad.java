package com.example.argosresidencia;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PoliticasPrivacidad extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.politicas_privacidad);
        WebView webView = findViewById(R.id.PoliticasPrivacidad);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://argositt.000webhostapp.com/Politicas.html?fbclid=IwAR1DbXIHhasDolEmyG1om_TzQSsU6UVn01lpbYVRR4IqWc7gs9KDyT1JP0I");
    }
}