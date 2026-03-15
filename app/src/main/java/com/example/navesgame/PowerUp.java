package com.example.navesgame;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Random;

public class PowerUp {

    // Power-up types
    public static final int TYPE_SHIELD = 0;
    public static final int TYPE_DOUBLE_SHOT = 1;
    public static final int TYPE_SPEED_BOOST = 2;
    public static final int TYPE_SLOW_MOTION = 3;
    public static final int TYPE_TRIPLE_SHOT = 4;
    public static final int TYPE_HEALTH = 5;
    public static final int TYPE_BOMB = 6;

    // Duration in milliseconds
    public static final long DURATION_SHIELD = 10000; // 10 seconds
    public static final long DURATION_DOUBLE_SHOT = 8000; // 8 seconds
    public static final long DURATION_SPEED_BOOST = 10000; // 10 seconds
    public static final long DURATION_SLOW_MOTION = 5000; // 5 seconds
    public static final long DURATION_TRIPLE_SHOT = 6000; // 6 seconds

    // Size
    public static final int POWER_UP_SIZE = 40;

    private int x;
    private int y;
    private int type;
    private int speed = 3;
    private Bitmap bitmap; // Added bitmap field
    private Random random = new Random();

    public PowerUp(int screenWidth, int screenHeight, Bitmap bitmap, int type) {
        this.bitmap = bitmap;
        this.type = type;
        this.x = random.nextInt(screenWidth - POWER_UP_SIZE);
        this.y = -POWER_UP_SIZE; // Spawn above the screen
    }

    public void update() {
        y += speed;
    }

    public boolean isOffScreen(int screenHeight) {
        return y > screenHeight;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getType() {
        return type;
    }

    public int getSize() {
        return POWER_UP_SIZE;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    /**
     * Returns the duration for this power-up type
     */
    public long getDuration() {
        switch (type) {
            case TYPE_SHIELD: return DURATION_SHIELD;
            case TYPE_DOUBLE_SHOT: return DURATION_DOUBLE_SHOT;
            case TYPE_SPEED_BOOST: return DURATION_SPEED_BOOST;
            case TYPE_SLOW_MOTION: return DURATION_SLOW_MOTION;
            case TYPE_TRIPLE_SHOT: return DURATION_TRIPLE_SHOT;
            default: return 5000;
        }
    }

    /**
     * Apply the power-up effect to the game state
     */
    public void applyEffect(GameState gameState) {
        switch (type) {
            case TYPE_SHIELD: gameState.activateShield(DURATION_SHIELD); break;
            case TYPE_DOUBLE_SHOT: gameState.activateDoubleShot(DURATION_DOUBLE_SHOT); break;
            case TYPE_SPEED_BOOST: gameState.activateSpeedBoost(DURATION_SPEED_BOOST); break;
            case TYPE_SLOW_MOTION: gameState.activateSlowMotion(DURATION_SLOW_MOTION); break;
            case TYPE_TRIPLE_SHOT: gameState.activateTripleShot(DURATION_TRIPLE_SHOT); break;
            case TYPE_HEALTH: gameState.addHealth(); break;
            case TYPE_BOMB: /* Handled in GameView */ break;
        }
    }
}
