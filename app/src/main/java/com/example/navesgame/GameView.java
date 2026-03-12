package com.example.navesgame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isPlaying;
    private boolean isGameOver;
    private int screenX, screenY;
    private Paint paint;
    private SurfaceHolder surfaceHolder;
    private ToneGenerator toneGenerator;

    // Player ship
    private int playerX, playerY;
    private int playerSize = 80;
    private int playerSpeed = 15;

    // Bullets
    private List<Bullet> bullets = new ArrayList<>();
    private long lastShotTime = 0;
    private long shotDelay = 250; // Milliseconds between shots

    // Enemies
    private List<Enemy> enemies = new ArrayList<>();
    private long lastEnemySpawnTime = 0;
    private long enemySpawnDelay = 1500; // Milliseconds between spawns
    private int enemySpeed = 5;
    private Random random = new Random();

    // Score
    private int score = 0;
    private int highScore = 0;

    // Game dimensions
    private static final int BULLET_WIDTH = 10;
    private static final int BULLET_HEIGHT = 30;
    private static final int ENEMY_WIDTH = 80;
    private static final int ENEMY_HEIGHT = 60;

    public GameView(Context context, int screenX, int screenY) {
        super(context);
        this.screenX = screenX;
        this.screenY = screenY;
        this.surfaceHolder = getHolder();
        this.paint = new Paint();

        // Initialize sound
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        // Player starts at bottom center
        this.playerX = screenX / 2 - playerSize / 2;
        this.playerY = screenY - 150;
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            sleep();
        }
    }

    private void update() {
        if (isGameOver) return;

        long currentTime = System.currentTimeMillis();

        // Spawn enemies
        if (currentTime - lastEnemySpawnTime > enemySpawnDelay) {
            spawnEnemy();
            lastEnemySpawnTime = currentTime;

            // Increase difficulty over time
            if (enemySpawnDelay > 500) {
                enemySpawnDelay -= 10;
            }
            if (enemySpeed < 15) {
                enemySpeed += 0.1;
            }
        }

        // Update bullets
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.y -= 20; // Bullet speed

            // Remove bullets that go off screen
            if (bullet.y < 0) {
                bullets.remove(i);
            }
        }

        // Update enemies
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.y += enemySpeed;

            // Check collision with player
            if (checkCollision(enemy.x, enemy.y, ENEMY_WIDTH, ENEMY_HEIGHT,
                    playerX, playerY, playerSize, playerSize)) {
                gameOver();
                return;
            }

            // Check collision with bullets
            for (int j = bullets.size() - 1; j >= 0; j--) {
                Bullet bullet = bullets.get(j);
                if (checkCollision(enemy.x, enemy.y, ENEMY_WIDTH, ENEMY_HEIGHT,
                        bullet.x, bullet.y, BULLET_WIDTH, BULLET_HEIGHT)) {
                    // Enemy destroyed
                    enemies.remove(i);
                    bullets.remove(j);
                    score += 10;
                    // Play explosion sound
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100);
                    break;
                }
            }

            // Remove enemies that go off screen
            if (enemy.y > screenY) {
                enemies.remove(i);
            }
        }
    }

    private boolean checkCollision(int x1, int y1, int w1, int h1,
                                   int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 &&
               x1 + w1 > x2 &&
               y1 < y2 + h2 &&
               y1 + h1 > y2;
    }

    private void spawnEnemy() {
        int enemyX = random.nextInt(screenX - ENEMY_WIDTH);
        enemies.add(new Enemy(enemyX, -ENEMY_HEIGHT));
    }

    private void shoot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime > shotDelay) {
            // Shoot from center of player
            bullets.add(new Bullet(playerX + playerSize / 2 - BULLET_WIDTH / 2, playerY));
            lastShotTime = currentTime;
            // Play shooting sound
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50);
        }
    }

    private void gameOver() {
        isGameOver = true;
        if (score > highScore) {
            highScore = score;
        }
        // Play game over sound
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 300);
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.parseColor("#1a1a2e")); // Dark blue-black background

            if (isGameOver) {
                drawGameOver(canvas);
            } else {
                drawGame(canvas);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawGame(Canvas canvas) {
        // Draw stars background effect
        paint.setColor(Color.WHITE);
        for (int i = 0; i < 50; i++) {
            int starX = (i * 37) % screenX;
            int starY = (i * 73) % screenY;
            canvas.drawCircle(starX, starY, 2, paint);
        }

        // Draw player (triangle shape for plane)
        paint.setColor(Color.parseColor("#00d4ff")); // Cyan
            // Main body
            canvas.drawRect(playerX + 25, playerY + 20, playerX + 55, playerY + 80, paint);
            // Left wing
            canvas.drawRect(playerX, playerY + 40, playerX + 30, playerY + 60, paint);
            // Right wing
            canvas.drawRect(playerX + 50, playerY + 40, playerX + 80, playerY + 60, paint);
            // Cockpit
            paint.setColor(Color.parseColor("#ffffff"));
            canvas.drawRect(playerX + 30, playerY + 30, playerX + 50, playerY + 45, paint);

        // Draw bullets
        paint.setColor(Color.parseColor("#ffeb3b")); // Yellow
        for (Bullet bullet : bullets) {
            canvas.drawRect(bullet.x, bullet.y, bullet.x + BULLET_WIDTH, bullet.y + BULLET_HEIGHT, paint);
        }

        // Draw enemies
        for (Enemy enemy : enemies) {
            // Enemy body (red)
            paint.setColor(Color.parseColor("#ff4444"));
            canvas.drawRect(enemy.x + 20, enemy.y + 10, enemy.x + 60, enemy.y + 50, paint);
            // Left wing
            paint.setColor(Color.parseColor("#cc0000"));
            canvas.drawRect(enemy.x, enemy.y + 25, enemy.x + 25, enemy.y + 40, paint);
            // Right wing
            canvas.drawRect(enemy.x + 55, enemy.y + 25, enemy.x + 80, enemy.y + 40, paint);
        }

        // Draw score
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        canvas.drawText("Score: " + score, 20, 60, paint);

        // Draw high score
        paint.setTextSize(30);
        canvas.drawText("High: " + highScore, 20, 100, paint);
    }

    private void drawGameOver(Canvas canvas) {
        // Darken background
        paint.setColor(Color.parseColor("#80000000"));
        canvas.drawRect(0, 0, screenX, screenY, paint);

        // Game Over text
        paint.setColor(Color.RED);
        paint.setTextSize(80);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("GAME OVER", screenX / 2, screenY / 2 - 50, paint);

        // Score
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        canvas.drawText("Score: " + score, screenX / 2, screenY / 2 + 30, paint);

        // High Score
        paint.setColor(Color.parseColor("#ffd700")); // Gold
        paint.setTextSize(40);
        canvas.drawText("High Score: " + highScore, screenX / 2, screenY / 2 + 90, paint);

        // Restart instruction
        paint.setColor(Color.parseColor("#00d4ff"));
        paint.setTextSize(30);
        canvas.drawText("Tap to restart", screenX / 2, screenY / 2 + 160, paint);

        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void restartGame() {
        score = 0;
        enemies.clear();
        bullets.clear();
        enemySpeed = 5;
        enemySpawnDelay = 1500;
        playerX = screenX / 2 - playerSize / 2;
        playerY = screenY - 150;
        isGameOver = false;
    }

    private void sleep() {
        try {
            Thread.sleep(17); // ~60 FPS
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Cleanup method to be called when game view is destroyed
    public void cleanup() {
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isGameOver) {
                    restartGame();
                } else {
                    shoot();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (!isGameOver) {
                    // Move player with finger (horizontal only)
                    playerX = (int) event.getX() - playerSize / 2;

                    // Keep player within screen bounds
                    if (playerX < 0) playerX = 0;
                    if (playerX > screenX - playerSize) playerX = screenX - playerSize;

                    // Also shoot while moving
                    shoot();
                }
                break;
        }
        return true;
    }

    // Inner classes for game objects
    private static class Bullet {
        int x, y;
        Bullet(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class Enemy {
        int x, y;
        Enemy(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}