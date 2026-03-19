package com.example.navesgame;

import android.graphics.Color;
import java.util.Random;

public class Particle {
    private float x, y, vx, vy, alpha = 255;
    private int color, size;
    private boolean active = true;
    private static final Random random = new Random();

    public Particle(float x, float y, int color) {
        this(x, y, color, 0, 0);
        float angle = (float)(random.nextFloat() * 2 * Math.PI);
        float speed = random.nextFloat() * 10 + 2;
        this.vx = (float)(Math.cos(angle) * speed);
        this.vy = (float)(Math.sin(angle) * speed);
    }

    public Particle(float x, float y, int color, float ivx, float ivy) {
        this.x = x; this.y = y; this.color = color;
        this.vx = ivx + (random.nextFloat() * 4 - 2);
        this.vy = ivy + (random.nextFloat() * 4 - 2);
        this.size = random.nextInt(10) + 5;
    }

    public void update() {
        x += vx; y += vy; 
        if (vx != 0 || vy != 0) { vx *= 0.95f; vy *= 0.95f; } // Fricción para estelas
        alpha -= 10;
        if (alpha <= 0) active = false;
    }

    public boolean isActive() { return active; }
    public float getX() { return x; }
    public float getY() { return y; }
    public int getSize() { return size; }
    public int getColor() { 
        return Color.argb((int)alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    public static Particle[] createEnemyExplosion(int x, int y, int color) {
        Particle[] p = new Particle[15];
        for (int i = 0; i < 15; i++) p[i] = new Particle(x, y, color);
        return p;
    }
}