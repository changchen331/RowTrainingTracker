package com.atrainingtracker.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpRequests {
    private static final String TAG = "HttpRequests";
    private static final OkHttpClient client;

    static {
        client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build();
    }

    // 发送 GET 同步请求（等到服务器有响应才会继续往下走）
    // 注意网络同步请求必须要有一个子线程
    public void getSync(final Context context, final String url) {
        new Thread() {
            @Override
            public void run() {
                //get 请求
                Request request = new Request.Builder().url(url).get().build();
                //请求的 call 对象
                Call call = client.newCall(request);
                try {
                    Response response = call.execute();
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "操作成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "操作失败！", Toast.LENGTH_SHORT).show();
                    }
                    Log.e(TAG, "getSync:" + (response.body() != null ? response.body().string() : ""));
                } catch (IOException e) {
                    // 输出异常信息
                    System.out.println(e.getMessage());
                }
            }
        }.start();
    }

    // 发送 GET 异步请求
    public static void getAsync(String url) {
        Request request = new Request.Builder().url(url).get().build();

        //请求的 call 对象
        Call call = client.newCall(request);
        //异步请求
        call.enqueue(new Callback() {
            //失败的请求
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            //结束的回调
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //响应码可能是 404 也可能是 200 都会走这个方法
                if (response.isSuccessful()) {
                    Log.i(TAG, "getASync:" + (response.body() != null ? response.body().string() : ""));
                } else {
                    Log.e(TAG, "请求失败");
                }
            }
        });
    }

    // 发送 POST 同步请求
    public void postSync(final Context context, final String url, final ArrayList<Double> heartBeats) {
        new Thread() {
            @Override
            public void run() {
                if (heartBeats == null || heartBeats.isEmpty()) {
                    Toast.makeText(context, "数据为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject jsonObject = getJsonObject();

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());
                Request request = new Request.Builder().url(url).post(requestBody).build();
                //请求的call对象
                Call call = client.newCall(request);
//                try {
//                    Response response = call.execute();
//                    Log.e("pot同步请求", "postSync:" + (response.body() != null ? response.body().string() : ""));
//                } catch (IOException e) {
//                    // 输出异常信息
//                    System.out.println(e.getMessage());
//                }
            }

            private @NonNull JSONObject getJsonObject() {
                return HttpRequests.getJsonObject(heartBeats);
            }
        }.start();
    }

    //发送 POST 请求
    public static void postAsync(String url, ArrayList<Double> heartBeats) {
        if (heartBeats == null || heartBeats.isEmpty()) {
            Log.e(TAG, "数据为空");
            return;
        }

        JSONObject jsonObject = getJsonObject(heartBeats);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());
        Request request = new Request.Builder().url(url).post(requestBody).build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "请求失败");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody body = response.body();
                Log.i(TAG, "返回数据：" + (body != null ? body.string() + "?" : "空的"));
            }
        });
    }

    private static @NonNull JSONObject getJsonObject(ArrayList<Double> heartBeats) {
        JSONObject jsonObject = new JSONObject();
        try {
            // 名称
            jsonObject.put("name", "章震");
            // 时间
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String date = year + "" + month + day;
            jsonObject.put("date", date);
            // 数据
            JSONArray data = new JSONArray();
            for (double heartBeat : heartBeats) {
                data.put(heartBeat);
            }
            jsonObject.put("data", data);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject;
    }

    //发送 PUT 请求
//    public static String put(String url, String json) throws IOException {
//        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//        RequestBody requestBody = RequestBody.create(json, JSON);
//        Request request = new Request.Builder().url(url).put(requestBody).build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//
//            if (response.body() != null) {
//                return response.body().string();
//            }
//        }
//        return "";
//    }

    //发送 DEL 请求
//    public static String del(String url) throws IOException {
//        Request request = new Request.Builder().url(url).delete().build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//
//            if (response.body() != null) {
//                return response.body().string();
//            }
//        }
//        return "";
//    }
}
