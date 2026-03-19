package com.example.navesgame;

import android.graphics.Bitmap;
import java.util.Random;

public class Rock {

    public float x, y;
    public int width, height;
    public int speed;
    private Bitmap bitmap;
    private Random random;

    public Rock(int screenX, int screenY, Bitmap bitmap) {
        random = new Random();
        this.bitmap = bitmap;

        // Randomize initial position and speed
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        x = random.nextInt(screenX - width);
        y = -height - random.nextInt(screenY); // Start off-screen at various heights
        speed = 5 + random.nextInt(10); // Random speed

    }

    public void update() {
        y += speed;
    }

    public boolean isOffScreen(int screenY) {
        return y > screenY + height;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
