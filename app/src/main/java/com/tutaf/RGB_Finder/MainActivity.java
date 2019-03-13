package com.tutaf.RGB_Finder;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RGBFinder";
    private boolean mWasOpened = false;

    @Bind(R.id.spinner)
    protected View vSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isOnline(this)) {
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);
            vSpinner.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Нет подключения к сети", Toast.LENGTH_LONG).show();
            finishAffinity();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isOnline(this)) {
        new UPnPDeviceFinder().observe()
                .filter(new Func1<UPnPDevice, Boolean>() {
                    @Override
                    public Boolean call(UPnPDevice device) {
                        try {
                            device.downloadSpecs();
                        }
                        catch (Exception e) {
                            // Ignore errors
                            Log.w(TAG, "Error: " + e);
                        }
                        return true;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UPnPDevice>() {
                    @Override
                    public void call(UPnPDevice device) {

                        Log.i(TAG, device.getHost());
                        Log.i(TAG, device.getServer());
                        //Log.i("Tag_device_found", device.toString());
                        if (device.getServer().contains("Arduino")) {
                            if (!mWasOpened) {
                                String ip = device.getHost();
                                /*Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + ip + "/"));
                                Intent chooser = Intent.createChooser(sendIntent, "Choose Your Browser");

                                mWasOpened = true;
                                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(chooser);
                                    finishAffinity();
                                }*/
                                /*WebView mywebview = findViewById(R.id.webview);
                                mywebview.setVisibility(View.VISIBLE);
                                mywebview.loadUrl("http://" + ip + "/");

                                WebSettings webSettings = mywebview.getSettings();
                                webSettings.setBuiltInZoomControls(true);
                                webSettings.setJavaScriptEnabled(true);*/

                                Intent intent = new
                                        Intent("com.tutaf.RGB_Finger.Browser");
                                intent.setData(Uri.parse("http://" + ip + "/"));
                                startActivity(intent);

                                mWasOpened = true;
                            }
                        }
                    }
                });
        } else {
            Toast.makeText(this, "Нет подключения к сети", Toast.LENGTH_LONG).show();
            finishAffinity();
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

}
