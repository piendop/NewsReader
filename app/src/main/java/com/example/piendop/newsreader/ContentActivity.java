package com.example.piendop.newsreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ContentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        //create intent to get content from Main Activity
        Intent intent= getIntent();
        String htmlContent = intent.getStringExtra("content");
        //create webview to open web's content
        WebView webView = findViewById(R.id.webView);
        //set java script
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(htmlContent);
    }
}
