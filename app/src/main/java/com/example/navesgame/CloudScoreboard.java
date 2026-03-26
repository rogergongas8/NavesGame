package com.example.navesgame;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CloudScoreboard {
    private static final String DATABASE_URL = "https://navesgame-default-rtdb.europe-west1.firebasedatabase.app/";
    private static final DatabaseReference database = FirebaseDatabase.getInstance(DATABASE_URL).getReference();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void postHighScore(String name, int points, ScoreCallback callback) {
        DatabaseReference scoresRef = database.child("scores").push();
        ScoreEntry entry = new ScoreEntry(name, points, System.currentTimeMillis());
        scoresRef.setValue(entry).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (callback != null) mainHandler.post(() -> callback.onSuccess("Puntuación guardada"));
            } else {
                if (callback != null) mainHandler.post(() -> callback.onError(task.getException().getMessage()));
            }
        });
    }

    public static void postBossRun(String name, long time, int points, ScoreCallback callback) {
        DatabaseReference runsRef = database.child("boss_runs").push();
        BossRunEntry entry = new BossRunEntry(name, time, points, System.currentTimeMillis());
        runsRef.setValue(entry).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (callback != null) mainHandler.post(() -> callback.onSuccess("Tiempo de jefe guardado"));
            } else {
                if (callback != null) mainHandler.post(() -> callback.onError(task.getException().getMessage()));
            }
        });
    }

    public static void getTopScores(ScoreCallback callback) {
        Query topScores = database.child("scores").orderByChild("points").limitToLast(10);
        topScores.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder sb = new StringBuilder("🏆 GLOBAL TOP 10 🏆\n\n");
                List<ScoreEntry> entries = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    entries.add(0, ds.getValue(ScoreEntry.class)); // Reversa para que el mayor esté arriba
                }
                if (entries.isEmpty()) sb.append("(No scores yet)");
                for (int i = 0; i < entries.size(); i++) {
                    ScoreEntry e = entries.get(i);
                    sb.append(String.format("%d. %s: %d pts\n", i + 1, e.name, e.points));
                }
                mainHandler.post(() -> callback.onSuccess(sb.toString()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mainHandler.post(() -> callback.onError(error.getMessage()));
            }
        });
    }

    public static void getTopBossRuns(ScoreCallback callback) {
        Query topRuns = database.child("boss_runs").orderByChild("time").limitToFirst(10);
        topRuns.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder sb = new StringBuilder("⏱️ BOSS SPEEDRUNS ⏱️\n\n");
                int count = 1;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    BossRunEntry e = ds.getValue(BossRunEntry.class);
                    sb.append(String.format("%d. %s: %.2fs\n", count++, e.name, e.time / 1000.0));
                }
                if (count == 1) sb.append("(No runs yet)");
                mainHandler.post(() -> callback.onSuccess(sb.toString()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mainHandler.post(() -> callback.onError(error.getMessage()));
            }
        });
    }

    public static void testConnection(ScoreCallback callback) {
        database.child("test").setValue("ping").addOnCompleteListener(task -> {
            if (task.isSuccessful()) mainHandler.post(() -> callback.onSuccess("¡Conexión exitosa con Firebase!"));
            else mainHandler.post(() -> callback.onError("Fallo de conexión: " + task.getException().getMessage()));
        });
    }

    public static void postScore(String name, int points, long time, ScoreCallback callback) {
        postHighScore(name, points, callback);
        if (time > 0) postBossRun(name, time, points, callback);
    }

    public static class ScoreEntry {
        public String name;
        public int points;
        public long timestamp;
        public ScoreEntry() {}
        public ScoreEntry(String name, int points, long timestamp) {
            this.name = name; this.points = points; this.timestamp = timestamp;
        }
    }

    public static class BossRunEntry {
        public String name;
        public long time;
        public int points;
        public long timestamp;
        public BossRunEntry() {}
        public BossRunEntry(String name, long time, int points, long timestamp) {
            this.name = name; this.time = time; this.points = points; this.timestamp = timestamp;
        }
    }

    public interface ScoreCallback {
        void onSuccess(String result);
        void onError(String error);
    }
}
