package com.dessert.mojito;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import cn.jpush.android.api.JPushInterface;

/**
 * Created by Lawrence on 23/11/2016.
 *
 */
interface UpdateUIListenner {

    void UpdateUI(String str);
}

public class JPushReceiver extends BroadcastReceiver {

    UpdateUIListenner updateUIListenner;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.w("JPush", JPushInterface.EXTRA_ALERT);
        }
    }

    public void SetOnUpdateUIListenner(UpdateUIListenner updateUIListenner) {
        this.updateUIListenner = updateUIListenner;
    }
}
