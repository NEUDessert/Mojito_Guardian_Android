package com.dessert.mojito;

import android.util.Log;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Lawrence on 24/11/2016.
 *
 */

public class OkHttpUtils {
    public static String DOMAIN = "http://192.168.0.176:8080/Mojito/";
    private OkHttpClient mOkHttpClient;
    private static OkHttpUtils mOkHttpUtils;

    private OkHttpUtils() {
        mOkHttpClient = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                }).build();
    }

    public static OkHttpUtils getInstance() {
        if (mOkHttpUtils == null) {
            mOkHttpUtils = new OkHttpUtils();
        }
        return mOkHttpUtils;
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }
}
