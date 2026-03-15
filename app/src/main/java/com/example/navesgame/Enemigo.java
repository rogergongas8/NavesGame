package com.example.navesgame;

import android.graphics.Bitmap;
import java.util.Random;

public class Enemigo {
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_FAST = 1;
    public static final int TYPE_ZIGZAG = 2;
    public static final int TYPE_BOSS = 3;

    public float x, y;
    public float velocidad;
    public int type;
    public int health;
    public int maxHealth;
    public int width, height;
    
    private Bitmap bitmap;
    private float angle = 0;
    private float centerX;
    private Random random = new Random();

    public Enemigo(int screenX, int screenY, Bitmap bitmap) {
        this(screenX, screenY, bitmap, -1);
    }

    public Enemigo(int screenX, int screenY, Bitmap bitmap, int forcedType) {
        this.bitmap = bitmap;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        if (forcedType == -1) {
            float r = random.nextFloat();
            if (r < 0.7f) type = TYPE_NORMAL;
            else if (r < 0.85f) type = TYPE_FAST;
            else type = TYPE_ZIGZAG;
        } else {
            type = forcedType;
        }

        this.x = random.nextInt(screenX - 100);
        this.y = -150;
        this.centerX = x;

        switch (type) {
            case TYPE_NORMAL:
                velocidad = random.nextInt(5) + 5;
                health = 1;
                break;
            case TYPE_FAST:
                velocidad = random.nextInt(5) + 12;
                health = 1;
                break;
            case TYPE_ZIGZAG:
                velocidad = random.nextInt(3) + 6;
                health = 2;
                break;
            case TYPE_BOSS:
                x = screenX / 2f - width / 2; // Adjusted for dynamic width
                y = -300;
                velocidad = 2;
                health = 50;
                break;
        }
        maxHealth = health;
    }

    public void update(float timeScale) {
        switch (type) {
            case TYPE_NORMAL:
            case TYPE_FAST:
                y += velocidad * timeScale;
                break;
            case TYPE_ZIGZAG:
                y += velocidad * timeScale;
                angle += 0.1f * timeScale;
                x = centerX + (float) Math.sin(angle) * 150;
                break;
            case TYPE_BOSS:
                if (y < 150) {
                    y += velocidad * timeScale;
                } else {
                    angle += 0.05f * timeScale;
                    x = (float) (centerX + Math.sin(angle) * 200);
                }
                break;
        }
    }

    public boolean isDead() {
        return health <= 0;
    }

    public void takeDamage(int damage) {
        health -= damage;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
