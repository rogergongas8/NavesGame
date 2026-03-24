package com.example.navesgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editNickname;
    private TextView txtLeaderboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editNickname = findViewById(R.id.editNickname);
        txtLeaderboard = findViewById(R.id.txtLeaderboard);
        Button btnPlay = findViewById(R.id.btnPlay);

        // Cargar último nombre usado
        SharedPreferences prefs = getSharedPreferences("NavesGame", MODE_PRIVATE);
        editNickname.setText(prefs.getString("lastPlayerName", "Player"));

        btnPlay.setOnClickListener(v -> {
            String name = editNickname.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Introduce un nombre", Toast.LENGTH_SHORT).show();
            } else {
                prefs.edit().putString("lastPlayerName", name).apply();
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra("PLAYER_NAME", name);
                startActivity(intent);
            }
        });

        loadGlobalScores();
    }

    private void loadGlobalScores() {
        CloudScoreboard.getTopScores(new CloudScoreboard.ScoreCallback() {
            @Override
            public void onSuccess(String result) {
                txtLeaderboard.setText(result);
            }

            @Override
            public void onError(String error) {
                txtLeaderboard.setText("Global Scoreboard Offline\n\nModo Local Activo.");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGlobalScores();
    }
}