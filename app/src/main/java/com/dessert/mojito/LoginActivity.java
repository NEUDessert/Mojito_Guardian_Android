package com.dessert.mojito;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

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
        final String protectCode = protectCodeView.getText().toString();
        final String phoneNumber = phoneNumberView.getText().toString();
        OkHttpClient mOkHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
//                .url("http://192.168.50.183:8082/Mojito/user/contactsLogin.do?custodyCode=" + protectCode + "&phoneNumber=" + phoneNumber)
                .url("http://192.168.50.181:8081")
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "无法连接到服务器。", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                try {
                    JSONObject result = new JSONObject(responseStr);
                    int loginStatus = Integer.parseInt(result.get("error").toString());
                    if(loginStatus == 0) {
                        SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                        SharedPreferences.Editor mEditor = mSharedPreference.edit();
                        mEditor.putBoolean("logged", true);
                        mEditor.putString("protectCode", protectCode);
                        mEditor.putString("phoneNumber", phoneNumber);
//                        mEditor.apply();
                        if (mEditor.commit()) {
                            startMonitor();
                            LoginActivity.this.finish();
                        } else {
                            Looper.prepare();
                            Toast.makeText(getApplicationContext(), "登录失败!", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    } else {
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(), "请输入正确的认证信息!", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
//                    Log.d("Msg", loginStatus + "");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        });
    }
    private void startMonitor() {
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, MonitorActivity.class);
        LoginActivity.this.startActivity(intent);
    }
}
