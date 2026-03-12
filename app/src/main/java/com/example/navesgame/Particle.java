package com.example.navesgame;

import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

/**
 * Particle represents visual effects when enemies are destroyed.
 */
public class Particle {

    private float x;
    private float y;
    private float vx;
    private float vy;
    private int color;
    private float size;
    private float alpha = 255;
    private float decay;
    private boolean active = true;

    private static final Random random = new Random();

    public Particle(float x, float y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;

        // Random velocity in all directions
        float angle = (float) (random.nextFloat() * 2 * Math.PI);
        float speed = random.nextFloat() * 8 + 2;
        this.vx = (float) (Math.cos(angle) * speed);
        this.vy = (float) (Math.sin(angle) * speed);

        this.size = random.nextFloat() * 8 + 4;
        this.decay = random.nextFloat() * 10 + 5;
    }

    public void update() {
        x += vx;
        y += vy;

        // Add gravity effect
        vy += 0.2f;

        // Fade out
        alpha -= decay;
        if (alpha <= 0) {
            alpha = 0;
            active = false;
        }
    }

    public boolean isActive() {
        return active;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSize() {
        return size;
    }

    public int getColor() {
        // Apply alpha to color
        int a = (int) alpha;
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(a, r, g, b);
    }

    /**
     * Creates an explosion effect at the given position
     */
    public static Particle[] createExplosion(int x, int y, int color, int count) {
        Particle[] particles = new Particle[count];
        for (int i = 0; i < count; i++) {
            particles[i] = new Particle(x, y, color);
        }
        return particles;
    }

    /**
     * Creates an explosion with default enemy color
     */
    public static Particle[] createEnemyExplosion(int x, int y) {
        int[] colors = {
            Color.parseColor("#ff4444"),
            Color.parseColor("#ff8800"),
            Color.parseColor("#ffff00"),
            Color.parseColor("#ff0000")
        };
        int color = colors[random.nextInt(colors.length)];
        return createExplosion(x, y, color, 15);
    }

    /**
     * Creates a power-up collection effect
     */
    public static Particle[] createPowerUpCollection(int x, int y, int powerUpColor) {
        return createExplosion(x, y, powerUpColor, 20);
    }
}