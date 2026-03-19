package com.example.navesgame;

import android.graphics.Color;
import java.util.Random;

public class PowerUp {
    public static final int TYPE_SHIELD = 0;
    public static final int TYPE_DOUBLE_SHOT = 1;
    public static final int TYPE_SPEED_BOOST = 2;
    public static final int TYPE_SLOW_MOTION = 3;
    public static final int TYPE_TRIPLE_SHOT = 4;
    public static final int TYPE_BOMB = 5;

    public static final int POWER_UP_SIZE = 50;

    private int x, y, type, speed = 4;
    private Random random = new Random();

    public PowerUp(int screenWidth, int screenHeight, int minY) {
        this.x = random.nextInt(Math.max(1, screenWidth - POWER_UP_SIZE));
        this.y = -50;
        this.type = random.nextInt(6);
    }

    public void update() { y += speed; }
    public boolean isOffScreen(int screenHeight) { return y > screenHeight; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public int getType() { return type; }
    public int getColor() {
        switch (type) {
            case TYPE_SHIELD: return Color.CYAN;
            case TYPE_DOUBLE_SHOT: return Color.MAGENTA;
            case TYPE_TRIPLE_SHOT: return Color.YELLOW;
            case TYPE_SPEED_BOOST: return Color.GREEN;
            case TYPE_SLOW_MOTION: return Color.BLUE;
            case TYPE_BOMB: return Color.RED;
            default: return Color.WHITE;
        }
    }

    public String getIcon() {
        switch (type) {
            case TYPE_SHIELD: return "S";
            case TYPE_DOUBLE_SHOT: return "2";
            case TYPE_TRIPLE_SHOT: return "3";
            case TYPE_SPEED_BOOST: return "V";
            case TYPE_SLOW_MOTION: return "T";
            case TYPE_BOMB: return "B";
            default: return "?";
        }
    }

    public void applyEffect(GameState gameState, GameView view) {
        switch (type) {
            case TYPE_SHIELD: gameState.activateShield(10000); break;
            case TYPE_DOUBLE_SHOT: gameState.activateDoubleShot(8000); break;
            case TYPE_TRIPLE_SHOT: gameState.activateTripleShot(6000); break;
            case TYPE_SPEED_BOOST: gameState.activateSpeedBoost(10000); break;
            case TYPE_SLOW_MOTION: gameState.activateSlowMotion(5000); break;
            case TYPE_BOMB: view.triggerBomb(); break;
        }
    }
}