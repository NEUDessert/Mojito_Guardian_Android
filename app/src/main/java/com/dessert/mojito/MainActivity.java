package com.dessert.mojito;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import cn.jpush.android.api.JPushInterface;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        JPushInterface.setDebugMode(true); // TODO: Change Mode when Publish
        JPushInterface.init(this);
    }
    private void initialize() {
        SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean logged = mSharedPreference.getBoolean("logged", false);
        if(!logged) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(intent);
        } else {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, MonitorActivity.class);
            MainActivity.this.startActivity(intent);
        }
        finish();
    }
}
