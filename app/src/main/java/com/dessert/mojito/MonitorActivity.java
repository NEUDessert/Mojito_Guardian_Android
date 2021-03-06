package com.dessert.mojito;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import com.amap.api.maps.*;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;


/**
 * Created by Lawrence on 20/11/2016.
 *
 */

public class MonitorActivity extends Activity {

    MapView mMapView = null;
    private static final int PUSH_RECEIVED = 0;
    private static final int GET_BASIC_INFO = 1;
    private static final int GET_LOCATION = 2;
    private static final int GET_HEART_RATE = 3;
    private TextView nameTextView, statusTextView, rateTextView, recordTextView;
    private String nameText, statusText, rateText, recordText;
    private AMap aMap;
    private UiSettings mUiSettings;
    private OkHttpClient mOkHttpClient;
    private TagAliasCallback mTagAliasCallback;
    private BroadcastReceiver mReceiver;
    private double locX, locY;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if(message.what == PUSH_RECEIVED) {
                statusTextView.setText("异常");
                statusTextView.setTextColor(0xFFFF0000);
            }
            if(message.what == GET_BASIC_INFO) {
                nameTextView.setText(nameText);
                if(statusText.equals("")) statusText = "未知";
                statusTextView.setText(statusText);
                rateTextView.setText(rateText);
                recordTextView.setText(recordText);
            }
            if(message.what == GET_LOCATION) {
                setLocation(locX, locY);
            }
            if(message.what == GET_HEART_RATE) {
                rateTextView.setText(rateText);
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, 10000);
            Log.i("REQUEST", "123");
            final Request requestRate = new Request.Builder()
                    .url(OkHttpUtils.DOMAIN + "user/getHeartRate.do")
                    .build();
            Call callRate = mOkHttpClient.newCall(requestRate);
            callRate.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(), "无法连接到服务器。", Toast.LENGTH_LONG ).show();
                    Looper.loop();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject result = new JSONObject(response.body().string());
                        rateText = getResources().getString(R.string.rate_text);
                        rateText = String.format(rateText, Integer.parseInt(result.get("heartRate").toString()));
                        Message message = new Message();
                        message.what = GET_HEART_RATE;
                        handler.sendMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
            });

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_monitor);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        nameTextView = (TextView)findViewById(R.id.nameText);
        statusTextView = (TextView)findViewById(R.id.statusText);
        rateTextView = (TextView)findViewById(R.id.rateText);
        recordTextView = (TextView)findViewById(R.id.recordText);

        nameText = getResources().getString(R.string.name_text);
        statusText = getResources().getString(R.string.status_text);
        rateText = getResources().getString(R.string.rate_text);
        recordText = getResources().getString(R.string.record_text);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("com.dessert.mojito.CHANGE_STATUS")) {
                    Message message = new Message();
                    message.what = PUSH_RECEIVED;
                    handler.sendMessage(message);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.dessert.mojito.CHANGE_STATUS");
        registerReceiver(mReceiver, filter);

        // Location.

        JPushInterface.setDebugMode(true); // TODO: Change Mode when Publish
        JPushInterface.init(this);

        mOkHttpClient = OkHttpUtils.getInstance().getOkHttpClient();
        mTagAliasCallback = new TagAliasCallback() {
            @Override
            public void gotResult(int i, String s, Set<String> set) {
                if(i == 0) {
                    Log.i("JPush", "Alias: " + s);
                } else {
                    Log.w("JPush", "Error Code: " + i);
                }
            }
        }; // for debug
        final Request request = new Request.Builder()
                .url(OkHttpUtils.DOMAIN + "user/getBasicInfo.do")
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "无法连接到服务器。", Toast.LENGTH_LONG ).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String str = response.body().string();
                    Log.i("JSON", str);
                    JSONObject result = new JSONObject(str);

                    nameText = String.format(nameText, result.get("name").toString());
                    statusText = String.format(statusText, result.get("status").toString());
                    rateText = String.format(rateText, Integer.parseInt(result.get("heartRate").toString()));
                    recordText = String.format(recordText, Integer.parseInt(result.get("recordCount").toString()));
                    JPushInterface.setAlias(getApplicationContext(), result.get("phoneNumber").toString(), mTagAliasCallback);

                    Message message = new Message();
                    message.what = GET_BASIC_INFO;
                    handler.sendMessage(message);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_SHORT ).show();
                    Looper.loop();
                }
            }
        });
        final Request requestPos = new Request.Builder()
                .url(OkHttpUtils.DOMAIN + "user/getLocation.do")
                .build();
        Call callPos = mOkHttpClient.newCall(requestPos);
        callPos.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String res = response.body().string();
                    Log.i("RES", res);
                    JSONObject result = new JSONObject(res);
                    if(result.getString("error").equals("0")) {
                        locX = Double.parseDouble(result.get("locX").toString());
                        locY = Double.parseDouble(result.get("locY").toString());
                        Message message = new Message();
                        message.what = GET_LOCATION;
                        handler.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        handler.postDelayed(runnable, 10000);
    }

    @Override
    protected void onDestroy() {
        if(mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        handler.removeCallbacks(runnable);
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        JPushInterface.resumePush(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    private void setLocation(double locX, double locY) {
        if (aMap == null) {
            aMap = mMapView.getMap();
            mUiSettings = aMap.getUiSettings();

        }
        LatLng latLng = new LatLng(locY, locX);
        final Marker marker = aMap.addMarker(new MarkerOptions().
                position(latLng).
                title("用户当前位置"));
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setZoomPosition(0);
        mUiSettings.setScaleControlsEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);

    }

    public void showRecord(View view) {
        Intent intent = new Intent();
        intent.setClass(MonitorActivity.this, RecordActivity.class);
        MonitorActivity.this.startActivity(intent);
    }

    public void logout(View view) {
        final Request request = new Request.Builder()
                .url(OkHttpUtils.DOMAIN + "user/cLogout.do")
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
                    if(result.get("error").toString().equals("0")) {
                        // Clean Preferences
                        SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(MonitorActivity.this);
                        SharedPreferences.Editor mEditor = mSharedPreference.edit();
                        if (mEditor.clear().commit()) {
                            Intent intent = new Intent();
                            intent.setClass(MonitorActivity.this, LoginActivity.class);
                            JPushInterface.stopPush(getApplicationContext());
                            MonitorActivity.this.startActivity(intent);
                            MonitorActivity.this.finish();
                        }
                    } else {
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(), "注销失败。", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        });
    }
}
