package com.example.navesgame;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pantalla completa sin título ni barras
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Mantener la pantalla encendida mientras juegas
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Compatibilidad API 24: Obtener tamaño real de pantalla
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(size);

        String playerName = getIntent().getStringExtra("PLAYER_NAME");
        if (playerName == null) playerName = "Guest";

        gameView = new GameView(this, size.x, size.y, playerName);
        setContentView(gameView);
        
        // Modo inmersivo para ocultar botones de navegación
        hideSystemUI();
    }

    private void hideSystemUI() {
        // Compatibilidad API 24: Usar SystemUiVisibility
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Log para depuración de teclado
        Log.d("KEYBOARD", "dispatchKeyEvent action=" + event.getAction() + " keyCode=" + event.getKeyCode());
        
        if (gameView != null) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (gameView.onKeyDown(event.getKeyCode(), event)) return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                if (gameView.onKeyUp(event.getKeyCode(), event)) return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) gameView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gameView != null) gameView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameView != null) gameView.releaseResources();
    }
}
