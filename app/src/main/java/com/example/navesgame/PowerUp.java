package com.example.navesgame;

import android.graphics.Color;

import java.util.Random;

public class PowerUp {

    // Power-up types
    public static final int TYPE_SHIELD = 0;
    public static final int TYPE_DOUBLE_SHOT = 1;
    public static final int TYPE_SPEED_BOOST = 2;
    public static final int TYPE_SLOW_MOTION = 3;

    // Duration in milliseconds
    public static final long DURATION_SHIELD = 10000; // 10 seconds
    public static final long DURATION_DOUBLE_SHOT = 8000; // 8 seconds
    public static final long DURATION_SPEED_BOOST = 10000; // 10 seconds
    public static final long DURATION_SLOW_MOTION = 5000; // 5 seconds

    // Size
    public static final int POWER_UP_SIZE = 40;

    private int x;
    private int y;
    private int type;
    private int speed = 3;
    private boolean active = true;
    private Random random = new Random();

    public PowerUp(int screenWidth, int screenHeight, int minY) {
        this.x = random.nextInt(screenWidth - POWER_UP_SIZE);
        this.y = random.nextInt(screenHeight / 3) + minY;
        this.type = random.nextInt(4);
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the color for this power-up type
     */
    public int getColor() {
        switch (type) {
            case TYPE_SHIELD:
                return Color.parseColor("#00FFFF"); // Cyan
            case TYPE_DOUBLE_SHOT:
                return Color.parseColor("#FF00FF"); // Magenta
            case TYPE_SPEED_BOOST:
                return Color.parseColor("#00FF00"); // Green
            case TYPE_SLOW_MOTION:
                return Color.parseColor("#FFA500"); // Orange
            default:
                return Color.WHITE;
        }
    }

    /**
     * Returns the icon/letter for this power-up type
     */
    public String getIcon() {
        switch (type) {
            case TYPE_SHIELD:
                return "S";
            case TYPE_DOUBLE_SHOT:
                return "D";
            case TYPE_SPEED_BOOST:
                return "V";
            case TYPE_SLOW_MOTION:
                return "T";
            default:
                return "?";
        }
    }

    /**
     * Returns the name of this power-up type
     */
    public String getName() {
        switch (type) {
            case TYPE_SHIELD:
                return "Escudo";
            case TYPE_DOUBLE_SHOT:
                return "Doble Disparo";
            case TYPE_SPEED_BOOST:
                return "Velocidad+";
            case TYPE_SLOW_MOTION:
                return "Slow Motion";
            default:
                return "Unknown";
        }
    }

    /**
     * Returns the duration for this power-up type
     */
    public long getDuration() {
        switch (type) {
            case TYPE_SHIELD:
                return DURATION_SHIELD;
            case TYPE_DOUBLE_SHOT:
                return DURATION_DOUBLE_SHOT;
            case TYPE_SPEED_BOOST:
                return DURATION_SPEED_BOOST;
            case TYPE_SLOW_MOTION:
                return DURATION_SLOW_MOTION;
            default:
                return 5000;
        }
    }

    /**
     * Apply the power-up effect to the game state
     */
    public void applyEffect(GameState gameState) {
        switch (type) {
            case TYPE_SHIELD:
                gameState.activateShield(DURATION_SHIELD);
                break;
            case TYPE_DOUBLE_SHOT:
                gameState.activateDoubleShot(DURATION_DOUBLE_SHOT);
                break;
            case TYPE_SPEED_BOOST:
                gameState.activateSpeedBoost(DURATION_SPEED_BOOST);
                break;
            case TYPE_SLOW_MOTION:
                gameState.activateSlowMotion(DURATION_SLOW_MOTION);
                break;
        }
    }
}