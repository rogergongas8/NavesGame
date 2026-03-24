package com.example.navesgame;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CloudScoreboard {
    private static final String APP_ID = "YOUR_APP_ID"; // Rellenar
    private static final String API_KEY = "YOUR_API_KEY"; // Rellenar
    private static final String BASE_URL = "https://eu-central-1.aws.data.mongodb-api.com/app/" + APP_ID + "/endpoint/data/v1/action/";
    private static final String CLUSTER = "Cluster0";
    private static final String DATABASE = "NavesGame";
    private static final String COLLECTION = "Scoreboard";

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public interface ScoreCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public static void postScore(String name, int points, long time, ScoreCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("dataSource", CLUSTER);
            json.put("database", DATABASE);
            json.put("collection", COLLECTION);
            
            JSONObject doc = new JSONObject();
            doc.put("name", name);
            doc.put("points", points);
            doc.put("time", time);
            doc.put("timestamp", System.currentTimeMillis());
            
            json.put("document", doc);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "insertOne")
                    .addHeader("api-key", API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess("Score Saved!"));
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    public static void getTopScores(ScoreCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("dataSource", CLUSTER);
            json.put("database", DATABASE);
            json.put("collection", COLLECTION);
            
            JSONObject sort = new JSONObject();
            sort.put("points", -1); // De mayor a menor
            json.put("sort", sort);
            json.put("limit", 10);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "find")
                    .addHeader("api-key", API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String bodyStr = response.body().string();
                        JSONObject res = new JSONObject(bodyStr);
                        JSONArray docs = res.getJSONArray("documents");
                        StringBuilder sb = new StringBuilder("🏆 GLOBAL TOP 10 🏆\n\n");
                        for (int i = 0; i < docs.length(); i++) {
                            JSONObject d = docs.getJSONObject(i);
                            sb.append(String.format("%d. %s - %d pts (%ds)\n", 
                                i + 1, d.getString("name"), d.getInt("points"), d.getLong("time")/1000));
                        }
                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(sb.toString()));
                    } catch (Exception e) {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onError("Empty scoreboard"));
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
}
