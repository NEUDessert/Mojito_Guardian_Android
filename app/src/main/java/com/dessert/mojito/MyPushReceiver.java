package com.dessert.mojito;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;


/**
 * Created by Lawrence on 23/11/2016.
 *
 */

public class MyPushReceiver extends BroadcastReceiver {
    public MyPushReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Intent renderIntent = new Intent();
            renderIntent.setAction("com.dessert.mojito.CHANGE_STATUS");
            context.sendBroadcast(renderIntent);

        }
    }
}