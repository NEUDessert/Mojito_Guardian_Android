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
    private TextView nameTextView, statusTextView, rateTextView, recordTextView;
    private String nameText, statusText, rateText, recordText;
    private AMap aMap;
    private UiSettings mUiSettings;
    private OkHttpClient mOkHttpClient;
    private TagAliasCallback mTagAliasCallback;
    private MyPushReceiver mReceiver;
    private double locX, locY;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if(message.what == PUSH_RECEIVED) {
                Log.i("123", "123");
                statusTextView.setText("异常");
            }
            if(message.what == GET_BASIC_INFO) {
                nameTextView.setText(nameText);
                statusTextView.setText(statusText);
                rateTextView.setText(rateText);
                recordTextView.setText(recordText);
            }
            if(message.what == GET_LOCATION) {
                setLocation(locX, locY);
            }
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

        BroadcastReceiver mReceiver = new BroadcastReceiver() {
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
//        /*
        final Request request = new Request.Builder()
                .url("http://192.168.50.183:8082/Mojito/user/getBasicInfo.do")
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
                .url("http://192.168.50.183:8082/Mojito/user/getLocation.do")
                .build();
        Call callPos = mOkHttpClient.newCall(requestPos);
        callPos.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject result = new JSONObject(response.body().string());
                    locX = Double.parseDouble(result.get("locX").toString());
                    locY = Double.parseDouble(result.get("locY").toString());
                    Message message = new Message();
                    message.what = GET_LOCATION;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
//        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
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

//        locY = 41.656321;
//        locX = 123.425986;
//
//        final Request request = new Request.Builder()
//                .url("http://192.168.50.183:8082/Mojito/user/getLocation.do")
//                .build();
//        Call call = mOkHttpClient.newCall(request);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Looper.prepare();
//                Toast.makeText(getApplicationContext(), "无法连接到服务器。", Toast.LENGTH_LONG ).show();
//                Looper.loop();
//            }
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                try {
//                    JSONObject result = new JSONObject(response.body().string());
//                    locX = Double.parseDouble(result.get("locX").toString());
//                    locY = Double.parseDouble(result.get("locY").toString());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    Looper.prepare();
//                    Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_SHORT ).show();
//                    Looper.loop();
//                }
//            }
//        });
        LatLng latLng = new LatLng(locY, locX);
        final Marker marker = aMap.addMarker(new MarkerOptions().
                position(latLng).
                title("用户当前位置"));
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setZoomPosition(0);
        mUiSettings.setScaleControlsEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);

    }

    public void logout(View view) {
        final Request request = new Request.Builder()
                .url("http://192.168.50.183:8082/Mojito/user/cLogout.do")
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
                            // TODO: Go back to Login Activity
                            Intent intent = new Intent();
                            intent.setClass(MonitorActivity.this, LoginActivity.class);
                            MonitorActivity.this.startActivity(intent);
                            MonitorActivity.this.finish();
                            JPushInterface.stopPush(getApplicationContext());
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
