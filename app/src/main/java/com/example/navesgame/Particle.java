package com.example.navesgame;

import android.graphics.Color;
import java.util.Random;

public class Particle {
    private float x, y, vx, vy, alpha = 255;
    private int color, size;
    private boolean active = true;
    private static final Random random = new Random();

    public Particle(float x, float y, int color) {
        this(x, y, color, 0f, 0f);
        float angle = (float)(random.nextFloat() * 2f * Math.PI);
        float speed = (float)(random.nextFloat() * 10f + 2f);
        this.vx = (float)(Math.cos(angle) * speed);
        this.vy = (float)(Math.sin(angle) * speed);
    }

    public Particle(float x, float y, int color, float ivx, float ivy) {
        this.x = x; this.y = y; this.color = color;
        this.vx = ivx + (random.nextFloat() * 4 - 2);
        this.vy = ivy + (random.nextFloat() * 4 - 2);
        this.size = random.nextInt(10) + 5;
    }

    public void update(float deltaTime) {
        float speedScale = deltaTime * 60f;
        x += vx * speedScale; y += vy * speedScale; 
        if (vx != 0 || vy != 0) { 
            vx *= (float)Math.pow(0.95, speedScale); 
            vy *= (float)Math.pow(0.95, speedScale); 
        } // Fricción para estelas
        alpha -= 600 * deltaTime; // Desvanece en ~0.4s
        if (alpha <= 0) active = false;
    }

    public boolean isActive() { return active; }
    public float getX() { return x; }
    public float getY() { return y; }
    public int getSize() { return size; }
    public int getColor() { 
        return Color.argb((int)alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    public static Particle[] createEnemyExplosion(float x, float y, int color) {
        Particle[] p = new Particle[15];
        for (int i = 0; i < 15; i++) p[i] = new Particle(x, y, color);
        return p;
    }
}