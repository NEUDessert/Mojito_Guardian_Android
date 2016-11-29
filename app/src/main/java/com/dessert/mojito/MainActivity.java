package com.dessert.mojito;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
        setContentView(R.layout.activity_main);

    }
    private void initialize() {
        SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean logged = mSharedPreference.getBoolean("logged", false);
        if(!logged) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(intent);
        } else {
            String phoneNumber = mSharedPreference.getString("phoneNumber", "0");
            String protectCode = mSharedPreference.getString("protectCode", "0");
            OkHttpClient mOkHttpClient = OkHttpUtils.getInstance().getOkHttpClient();
            final Request request = new Request.Builder()
                    .url(OkHttpUtils.DOMAIN + "user/contactsLogin.do?custodyCode=" + protectCode + "&phoneNumber=" + phoneNumber)
                    .build();
            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(), "Internal Error.", Toast.LENGTH_SHORT).show();
                    System.exit(1);
                    Looper.loop();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject result = new JSONObject(response.body().string());
                        if (result.get("error").toString().equals("0")) {
                            startMonitor();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(), "Internal Error.", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }

                }
            });
        }
        finish();
    }

    private void startMonitor() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, MonitorActivity.class);
        MainActivity.this.startActivity(intent);
    }
}
