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
    private float initialSize;
    private float alpha = 255;
    private float decay;
    private float scaleSpeed;
    private boolean active = true;

    private static final Random random = new Random();

    public Particle(float x, float y, int color, float speedMult, float sizeMult) {
        this.x = x;
        this.y = y;
        this.color = color;

        // Random velocity in all directions
        float angle = (float) (random.nextFloat() * 2 * Math.PI);
        float speed = (random.nextFloat() * 6 + 2) * speedMult;
        this.vx = (float) (Math.cos(angle) * speed);
        this.vy = (float) (Math.sin(angle) * speed);

        this.initialSize = (random.nextFloat() * 6 + 4) * sizeMult;
        this.size = initialSize;
        this.decay = random.nextFloat() * 5 + 3;
        this.scaleSpeed = 0.95f + (random.nextFloat() * 0.04f); // Shrink over time
    }

    public void update() {
        x += vx;
        y += vy;

        // Friction/Air resistance
        vx *= 0.98f;
        vy *= 0.98f;

        // Add slight gravity
        vy += 0.1f;

        // Shrink
        size *= scaleSpeed;

        // Fade out
        alpha -= decay;
        if (alpha <= 0 || size < 1) {
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
    public static Particle[] createExplosion(int x, int y, int color, int count, float speedMult, float sizeMult) {
        Particle[] particles = new Particle[count];
        for (int i = 0; i < count; i++) {
            particles[i] = new Particle(x, y, color, speedMult, sizeMult);
        }
        return particles;
    }

    /**
     * Creates an explosion with default enemy color
     */
    public static Particle[] createEnemyExplosion(int x, int y) {
        int[] colors = {
            Color.parseColor("#FF4444"), // Red
            Color.parseColor("#FF8800"), // Orange
            Color.parseColor("#FFFF00"), // Yellow
            Color.parseColor("#FFFFFF")  // White for hot center
        };
        Particle[] p = new Particle[20];
        for (int i = 0; i < p.length; i++) {
            int color = colors[random.nextInt(colors.length)];
            p[i] = new Particle(x, y, color, 1.2f, 1.5f);
        }
        return p;
    }

    /**
     * Creates a spark effect (faster, smaller)
     */
    public static Particle[] createSparks(int x, int y, int color) {
        Particle[] p = new Particle[10];
        for (int i = 0; i < p.length; i++) {
            p[i] = new Particle(x, y, color, 2.0f, 0.5f);
        }
        return p;
    }

    /**
     * Creates a power-up collection effect (circular-ish expansion)
     */
    public static Particle[] createPowerUpCollection(int x, int y, int powerUpColor) {
        Particle[] p = new Particle[25];
        for (int i = 0; i < p.length; i++) {
            p[i] = new Particle(x, y, powerUpColor, 0.8f, 1.2f);
            // Influence velocity to be more uniform/outward
            float angle = (float) (i * 2 * Math.PI / p.length);
            float speed = random.nextFloat() * 4 + 4;
            p[i].vx = (float) (Math.cos(angle) * speed);
            p[i].vy = (float) (Math.sin(angle) * speed);
        }
        return p;
    }
}