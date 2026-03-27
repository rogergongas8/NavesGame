package com.example.navesgame;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowInsetsController;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pantalla completa sin título ni barras
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Mantener la pantalla encendida mientras juegas
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        WindowMetrics metrics = getWindowManager().getCurrentWindowMetrics();
        int screenX = metrics.getBounds().width();
        int screenY = metrics.getBounds().height();

        String playerName = getIntent().getStringExtra("PLAYER_NAME");
        if (playerName == null) playerName = "Guest";

        gameView = new GameView(this, screenX, screenY, playerName);
        setContentView(gameView);

        // Modo inmersivo para ocultar botones de navegación
        hideSystemUI();
    }

    private void hideSystemUI() {
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUI();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
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
    protected void onResume() {
        super.onResume();
        if (gameView != null) gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) gameView.pause();
    }
}
