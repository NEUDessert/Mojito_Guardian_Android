package com.dessert.mojito;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.Looper;
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
    private TextView nameTextView, statusTextView, rateTextView, recordTextView;
    private AMap aMap;
    private UiSettings mUiSettings;
    private LatLng latLng;
    private OkHttpClient mOkHttpClient;
    private TagAliasCallback mTagAliasCallback;
    private JPushReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nameTextView = (TextView)findViewById(R.id.nameText);
        statusTextView = (TextView)findViewById(R.id.statusText);
        rateTextView = (TextView)findViewById(R.id.rateText);
        recordTextView = (TextView)findViewById(R.id.recordText);

        JPushInterface.setDebugMode(true); // TODO: Change Mode when Publish
        JPushInterface.init(this);

        mOkHttpClient = new OkHttpClient();
        mTagAliasCallback = new TagAliasCallback() {
            @Override
            public void gotResult(int i, String s, Set<String> set) {
                if(i == 0) {
                    Log.i("JPush", "Alias: " + s);
                } else {
                    Log.w("JPush", "Error Code: " + i);
                }
            }
        };
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

                    JSONObject result = new JSONObject(response.body().string());
                    nameTextView.setText(result.get("name") + "");
                    statusTextView.setText(result.get("status") + "");
                    rateTextView.setText(result.get("heartRate") + " RPM");
                    recordTextView.setText(result.get("recordCount") + " 条记录");
                    JPushInterface.setAlias(getApplicationContext(), result.get("phoneNumber") + "", mTagAliasCallback);

                    mReceiver = new JPushReceiver();
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(JPushInterface.ACTION_NOTIFICATION_RECEIVED);
                    registerReceiver(mReceiver, filter);
                    mReceiver.SetOnUpdateUIListenner(new UpdateUIListenner() {
                        @Override
                        public void UpdateUI(String str) {
                            statusTextView.setText("异常");
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_SHORT ).show();
                    Looper.loop();
                }
            }
        });
//        */

        setContentView(R.layout.activity_monitor);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        init();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        unregisterReceiver(mReceiver);
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

    private void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            mUiSettings = aMap.getUiSettings();

        }
        latLng = new LatLng(41.656321, 123.425986, true); // LatLng( y, x)
        final Marker marker = aMap.addMarker(new MarkerOptions().
                position(latLng).
                title("用户当前位置").
                snippet("DefaultMarker"));
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
