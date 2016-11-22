package com.dessert.mojito;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import okhttp3.*;

import java.io.IOException;

/**
 * Created by Lawrence on 22/11/2016.
 *
 */
public class LoginActivity extends Activity {

    private View loginView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
    public void login(View view) {
        TextView protectCodeView = (TextView)findViewById(R.id.protect_code);
        TextView phoneNumberView = (TextView)findViewById(R.id.phone_number);
        String protectCode = protectCodeView.getText().toString();
        String phoneNumber = phoneNumberView.getText().toString();
        OkHttpClient mOkHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("http://192.168.43.173:8081?protectCode=" + protectCode + "&phoneNumber=" + phoneNumber)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("Msg", response.body().string());

                // TODO: JSON Parser

                // If Error = 0: startMonitor & Store Info

                // Else Login Fail.
            }
        });
    }
    private void startMonitor() {
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, MonitorActivity.class);
        LoginActivity.this.startActivity(intent);
    }
}
