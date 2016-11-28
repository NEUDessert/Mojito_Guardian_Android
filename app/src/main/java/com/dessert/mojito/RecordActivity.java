package com.dessert.mojito;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.*;

/**
 * Created by Lawrence on 26/11/2016.
 *
 */
public class RecordActivity extends Activity {
    private ListView recordList;
    private SimpleAdapter mAdapter;
    private OkHttpClient mOkHttpClient;
    private List<Map <String, Object>> list;
    private final int RENDER_LIST = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if(message.what == RENDER_LIST) {
                mAdapter = new SimpleAdapter(RecordActivity.this, list, R.layout.record, new String[] {"time", "content"}, new int[] {R.id.time, R.id.content});
                recordList.setAdapter(mAdapter);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list = new ArrayList<>();
        setContentView(R.layout.activity_record);
        recordList = (ListView)findViewById(R.id.recordList);
        mOkHttpClient = OkHttpUtils.getInstance().getOkHttpClient();
        Request request = new Request.Builder()
                .url("http://192.168.50.197:8082/Mojito/user/getAllAlert.do")
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "Internal Error", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                 try {
                     JSONArray result = new JSONArray(response.body().string());

                     if(result.length() != 0) {
                         Map<String, Object> map;

                         for(int i = 0; i < result.length(); i++) {
                             JSONObject piece = result.getJSONObject(i);
                             map = new HashMap<>();
                             DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
                             Date time = new Date(Long.parseLong(piece.getString("alertTime")));
                             map.put("time", format.format(time));
                             map.put("content", piece.get("type").toString());
                             list.add(map);
                         }

                         Message message = new Message();
                         message.what = RENDER_LIST;
                         handler.sendMessage(message);
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
//    private List<Map<String, Object>> getData() {
//        List<Map <String, Object>> list = new ArrayList<>();
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("time", "9:30");
//        map.put("content", "心律不齐");
//        list.add(map);
//
//        map = new HashMap<>();
//        map.put("time", "9:32");
//        map.put("content", "心律不齐");
//        list.add(map);
//
//        return list;
//    }
}
