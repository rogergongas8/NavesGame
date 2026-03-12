package com.example.navesgame;


import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtener tamaño de pantalla
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        // Inicializar la vista del juego
        gameView = new GameView(this, size.x, size.y);
        setContentView(gameView); // Esto es lo que quita el "Hello World"
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
}