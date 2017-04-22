package net.raffy.mp3alarmclock.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import net.raffy.mp3alarmclock.R;



public class Bluetooth extends Activity {

    WebView webHtmlCss;

    @Override
    public void onCreate(Bundle state)

    {
        super.onCreate(state);
        setContentView(R.layout.bluetooth);

        webHtmlCss= (WebView) findViewById(R.id.webview);
        WebSettings ws= webHtmlCss.getSettings();
        ws.setJavaScriptEnabled(true);
        webHtmlCss.loadUrl("file:android_asset/index.html");
    }


}
