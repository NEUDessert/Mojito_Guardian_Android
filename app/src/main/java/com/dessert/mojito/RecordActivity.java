package com.dessert.mojito;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lawrence on 26/11/2016.
 */
public class RecordActivity extends Activity {
    private ListView recordList;
    private SimpleAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        recordList = (ListView)findViewById(R.id.recordList);
        mAdapter = new SimpleAdapter(this, getData(), R.layout.record, new String[] {"time", "content"}, new int[] {R.id.time, R.id.content});
        recordList.setAdapter(mAdapter);
    }
    private List<Map<String, Object>> getData() {
        List<Map <String, Object>> list = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("time", "9:30");
        map.put("content", "心律不齐");
        list.add(map);

        map = new HashMap<>();
        map.put("time", "9:32");
        map.put("content", "心律不齐");
        list.add(map);

        return list;
    }
}
