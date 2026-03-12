package com.example.navesgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
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
    private GameState gameState;
    private int screenX, screenY;
    private Paint paint;
    private SurfaceHolder surfaceHolder;
    private ToneGenerator toneGenerator;
    private Random random = new Random();

    private int playerX, playerY;
    private int playerSize = 80;
    private int basePlayerSpeed = 15;
    private int playerSpeed = 15;

    private List<Bullet> bullets = new ArrayList<>();
    private long lastShotTime = 0;
    private long shotDelay = 250;

    // Se usa la clase Enemigo (del archivo Enemigo.java)
    private List<Enemigo> enemies = new ArrayList<>();
    private long lastEnemySpawnTime = 0;
    private long enemySpawnDelay = 1500;
    private int enemySpeed = 5;

    private List<PowerUp> powerUps = new ArrayList<>();
    private long lastPowerUpSpawnTime = 0;
    private long powerUpSpawnDelay = 10000;

    private List<Particle> particles = new ArrayList<>();
    private Star[] stars;
    private static final int NUM_STARS = 100;

    private long gameTime = 0;
    private int thrusterFrame = 0;

    private static final String PREFS_NAME = "NavesGamePrefs";
    private static final String HIGH_SCORE_KEY = "highScore";

    private static final int BULLET_WIDTH = 10;
    private static final int BULLET_HEIGHT = 30;
    private static final int ENEMY_WIDTH = 80;
    private static final int ENEMY_HEIGHT = 60;

    private RectF[] menuButtons;
    private String[] menuButtonTexts;

    public GameView(Context context, int screenX, int screenY) {
        super(context);
        this.screenX = screenX;
        this.screenY = screenY;
        this.surfaceHolder = getHolder();
        this.paint = new Paint();
        this.gameState = new GameState();

        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        } catch (Exception e) {
            toneGenerator = null;
        }

        initStars();
        this.playerX = screenX / 2 - playerSize / 2;
        this.playerY = screenY - 150;
        initMenuButtons();
        loadHighScore();
    }

    private void initStars() {
        stars = new Star[NUM_STARS];
        for (int i = 0; i < NUM_STARS; i++) {
            stars[i] = new Star(screenX, screenY);
        }
    }

    private void initMenuButtons() {
        menuButtonTexts = new String[]{"JUGAR", "SELECCIONAR MAPA", "NIVELES", "HIGH SCORES"};
        menuButtons = new RectF[4];
    }

    private void loadHighScore() {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int highScore = prefs.getInt(HIGH_SCORE_KEY, 0);
        gameState.setHighScore(highScore);
    }

    private void saveHighScore() {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(HIGH_SCORE_KEY, gameState.getHighScore()).apply();
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
        if (gameState.getState() == GameState.STATE_PLAYING && !isGameOver()) {
            updateGame();
        }
        for (int i = particles.size() - 1; i >= 0; i--) {
            particles.get(i).update();
            if (!particles.get(i).isActive()) {
                particles.remove(i);
            }
        }
        gameTime += 16;
    }

    private void updateGame() {
        long currentTime = System.currentTimeMillis();
        gameState.updatePowerUps();
        float timeScale = gameState.hasSlowMotion() ? 0.5f : 1.0f;

        if (currentTime - lastEnemySpawnTime > enemySpawnDelay * timeScale) {
            spawnEnemy();
            lastEnemySpawnTime = currentTime;
            if (gameState.getLevel() == GameState.LEVEL_ARCADE) {
                if (enemySpawnDelay > 500) enemySpawnDelay -= 10;
                if (enemySpeed < 15) enemySpeed += 0.1f;
            }
        }

        if (currentTime - lastPowerUpSpawnTime > powerUpSpawnDelay) {
            spawnPowerUp();
            lastPowerUpSpawnTime = currentTime;
        }

        playerSpeed = gameState.hasSpeedBoost() ? (int)(basePlayerSpeed * 1.5f) : basePlayerSpeed;

        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.y -= 20;
            if (bullet.y < 0) bullets.remove(i);
        }

        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemigo enemy = enemies.get(i);
            enemy.y += enemySpeed * timeScale;

            if (checkCollision(enemy.x, enemy.y, ENEMY_WIDTH, ENEMY_HEIGHT, playerX, playerY, playerSize, playerSize)) {
                if (gameState.hasShield()) {
                    gameState.useShield();
                    enemies.remove(i);
                    playSound(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100);
                } else {
                    gameOver();
                    return;
                }
            }

            for (int j = bullets.size() - 1; j >= 0; j--) {
                Bullet bullet = bullets.get(j);
                if (checkCollision(enemy.x, enemy.y, ENEMY_WIDTH, ENEMY_HEIGHT, bullet.x, bullet.y, BULLET_WIDTH, BULLET_HEIGHT)) {
                    Particle[] explosion = Particle.createEnemyExplosion(enemy.x + ENEMY_WIDTH / 2, enemy.y + ENEMY_HEIGHT / 2);
                    for (Particle p : explosion) particles.add(p);
                    enemies.remove(i);
                    bullets.remove(j);
                    gameState.addScore(10);
                    playSound(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100);
                    break;
                }
            }

            if (enemy.y > screenY) enemies.remove(i);
        }

        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            powerUp.update();
            if (checkCollision(powerUp.getX(), powerUp.getY(), PowerUp.POWER_UP_SIZE, PowerUp.POWER_UP_SIZE, playerX, playerY, playerSize, playerSize)) {
                powerUp.applyEffect(gameState);
                playSound(ToneGenerator.TONE_PROP_BEEP, 100);
                Particle[] effects = Particle.createPowerUpCollection(powerUp.getX() + PowerUp.POWER_UP_SIZE / 2, powerUp.getY() + PowerUp.POWER_UP_SIZE / 2, powerUp.getColor());
                for (Particle p : effects) particles.add(p);
                powerUps.remove(i);
            } else if (powerUp.isOffScreen(screenY)) {
                powerUps.remove(i);
            }
        }
        thrusterFrame = (thrusterFrame + 1) % 10;
    }

    private boolean isGameOver() {
        return gameState.getState() == GameState.STATE_GAME_OVER;
    }

    private boolean checkCollision(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    private void spawnEnemy() {
        // Importante: Asegúrate que el constructor de Enemigo.java reciba (int, int)
        enemies.add(new Enemigo(screenX, screenY));
    }

    private void spawnPowerUp() {
        powerUps.add(new PowerUp(screenX, screenY, 150));
    }

    private void shoot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime > shotDelay) {
            bullets.add(new Bullet(playerX + playerSize / 2 - BULLET_WIDTH / 2, playerY));
            if (gameState.hasDoubleShot()) {
                bullets.add(new Bullet(playerX + playerSize / 2 - BULLET_WIDTH / 2 - 20, playerY + 10));
                bullets.add(new Bullet(playerX + playerSize / 2 - BULLET_WIDTH / 2 + 20, playerY + 10));
            }
            lastShotTime = currentTime;
            playSound(ToneGenerator.TONE_PROP_BEEP, 50);
        }
    }

    private void gameOver() {
        gameState.setState(GameState.STATE_GAME_OVER);
        gameState.updateHighScore();
        saveHighScore();
        playSound(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 300);
    }

    private void restartGame() {
        gameState.resetForNewGame();
        enemies.clear();
        bullets.clear();
        powerUps.clear();
        particles.clear();
        enemySpeed = gameState.getEnemySpeed();
        enemySpawnDelay = gameState.getEnemySpawnDelay();
        playerX = screenX / 2 - playerSize / 2;
        playerY = screenY - 150;
        lastEnemySpawnTime = System.currentTimeMillis();
        lastPowerUpSpawnTime = System.currentTimeMillis();
        gameState.setState(GameState.STATE_PLAYING);
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            switch (gameState.getState()) {
                case GameState.STATE_MENU: drawMenu(canvas); break;
                case GameState.STATE_MAP_SELECT: drawMapSelect(canvas); break;
                case GameState.STATE_LEVEL_SELECT: drawLevelSelect(canvas); break;
                case GameState.STATE_HIGH_SCORES: drawHighScores(canvas); break;
                case GameState.STATE_PLAYING: drawGame(canvas); break;
                case GameState.STATE_GAME_OVER: drawGame(canvas); drawGameOver(canvas); break;
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        int map = gameState.getMap();
        if (map == GameState.MAP_SUNSET) {
            int[] sunsetColors = {Color.parseColor("#1a0533"), Color.parseColor("#4a1942"), Color.parseColor("#ff6b35")};
            LinearGradient gradient = new LinearGradient(0, 0, 0, screenY, sunsetColors, new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP);
            paint.setShader(gradient);
            canvas.drawRect(0, 0, screenX, screenY, paint);
            paint.setShader(null);
            paint.setColor(Color.parseColor("#ff9f1c"));
            paint.setAlpha(100);
            canvas.drawCircle(screenX / 2f, screenY / 2f + 100, 150, paint);
            paint.setAlpha(255);
        } else if (map == GameState.MAP_AURORA) {
            canvas.drawColor(Color.parseColor("#0a1628"));
            paint.setColor(Color.parseColor("#00ff87"));
            paint.setAlpha(30);
            Path auroraPath = new Path();
            auroraPath.moveTo(0, screenY * 0.3f);
            for (float x = 0; x <= screenX; x += 50) {
                float y = screenY * 0.3f + (float) Math.sin(x * 0.01 + gameTime * 0.001) * 50;
                auroraPath.lineTo(x, y);
            }
            auroraPath.lineTo(screenX, screenY); auroraPath.lineTo(0, screenY); auroraPath.close();
            canvas.drawPath(auroraPath, paint);
            paint.setAlpha(255);
        } else {
            canvas.drawColor(Color.parseColor("#1a1a2e"));
        }

        if (map != GameState.MAP_SUNSET) {
            paint.setColor(Color.WHITE);
            for (Star star : stars) {
                star.update(screenY);
                paint.setAlpha(star.alpha);
                canvas.drawCircle(star.x, star.y, star.size, paint);
            }
            paint.setAlpha(255);
        }
    }

    private void drawMenu(Canvas canvas) {
        drawBackground(canvas);
        paint.setColor(Color.parseColor("#00d4ff"));
        paint.setTextSize(80);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("NAVES GAME", screenX / 2f, screenY / 4f, paint);
        float buttonWidth = screenX * 0.6f;
        float buttonHeight = 80;
        for (int i = 0; i < menuButtons.length; i++) {
            float x = (screenX - buttonWidth) / 2;
            float y = (screenY / 2f) + i * (buttonHeight + 20);
            menuButtons[i] = new RectF(x, y, x + buttonWidth, y + buttonHeight);
            paint.setColor(Color.parseColor("#2a2a4a"));
            canvas.drawRoundRect(menuButtons[i], 15, 15, paint);
            paint.setColor(Color.WHITE);
            paint.setTextSize(35);
            canvas.drawText(menuButtonTexts[i], screenX / 2f, y + buttonHeight / 2 + 12, paint);
        }
    }

    private void drawMapSelect(Canvas canvas) { drawBackground(canvas); }
    private void drawLevelSelect(Canvas canvas) { drawBackground(canvas); }
    private void drawHighScores(Canvas canvas) { drawBackground(canvas); }

    private void drawGame(Canvas canvas) {
        drawBackground(canvas);
        for (PowerUp powerUp : powerUps) {
            paint.setColor(powerUp.getColor());
            canvas.drawCircle(powerUp.getX() + PowerUp.POWER_UP_SIZE / 2f, powerUp.getY() + PowerUp.POWER_UP_SIZE / 2f, PowerUp.POWER_UP_SIZE / 2f, paint);
        }
        for (Particle particle : particles) {
            paint.setColor(particle.getColor());
            canvas.drawCircle(particle.getX(), particle.getY(), particle.getSize(), paint);
        }

        paint.setColor(Color.parseColor("#00d4ff"));
        canvas.drawRect(playerX + 25, playerY + 20, playerX + 55, playerY + 80, paint);

        paint.setColor(Color.parseColor("#ffeb3b"));
        for (Bullet bullet : bullets) canvas.drawRect(bullet.x, bullet.y, bullet.x + BULLET_WIDTH, bullet.y + BULLET_HEIGHT, paint);

        for (Enemigo enemy : enemies) {
            paint.setColor(Color.RED);
            canvas.drawRect(enemy.x, enemy.y, enemy.x + ENEMY_WIDTH, enemy.y + ENEMY_HEIGHT, paint);
        }

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        canvas.drawText("Score: " + gameState.getScore(), 20, 60, paint);

        // INDICADORES CORREGIDOS (indicatorY como float)
        float indicatorY = 140f;
        if (gameState.hasShield()) {
            paint.setColor(Color.CYAN); paint.setTextSize(25);
            canvas.drawText("[ESCUDO]", 20f, indicatorY, paint);
            indicatorY += 30;
        }
        if (gameState.hasDoubleShot()) {
            paint.setColor(Color.MAGENTA);
            canvas.drawText("[DOBLE DISPARO]", 20f, indicatorY, paint);
            indicatorY += 30;
        }
        if (gameState.hasSpeedBoost()) {
            paint.setColor(Color.GREEN);
            canvas.drawText("[VELOCIDAD+]", 20f, indicatorY, paint);
            indicatorY += 30;
        }
        if (gameState.hasSlowMotion()) {
            paint.setColor(Color.YELLOW);
            canvas.drawText("[SLOW MO]", 20f, indicatorY, paint);
        }
    }

    private void drawGameOver(Canvas canvas) {
        paint.setColor(Color.parseColor("#80000000"));
        canvas.drawRect(0, 0, screenX, screenY, paint);
        paint.setColor(Color.RED); paint.setTextSize(80); paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("GAME OVER", screenX / 2f, screenY / 2f, paint);
    }

    private void sleep() { try { Thread.sleep(17); } catch (InterruptedException e) {} }
    private void playSound(int tone, int duration) { if (toneGenerator != null) toneGenerator.startTone(tone, duration); }
    public void resume() { isPlaying = true; thread = new Thread(this); thread.start(); }
    public void pause() { isPlaying = false; try { thread.join(); } catch (Exception e) {} }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) handleTouch(event.getX(), event.getY());
        if (event.getAction() == MotionEvent.ACTION_MOVE && gameState.getState() == GameState.STATE_PLAYING) {
            playerX = (int) event.getX() - playerSize / 2;
            shoot();
        }
        return true;
    }

    private void handleTouch(float x, float y) {
        if (gameState.getState() == GameState.STATE_MENU) handleMenuTouch(x, y);
        else if (gameState.getState() == GameState.STATE_PLAYING) shoot();
        else if (gameState.getState() == GameState.STATE_GAME_OVER) restartGame();
    }

    private void handleMenuTouch(float x, float y) {
        if (menuButtons[0].contains(x, y)) restartGame();
    }

    private static class Bullet { int x, y; Bullet(int x, int y) { this.x = x; this.y = y; } }

    private static class Star {
        float x, y, size, speed; int alpha, screenWidth;
        Star(int sw, int sh) { screenWidth = sw; x = (float)Math.random()*sw; y = (float)Math.random()*sh; size = (float)Math.random()*2+1; alpha = (int)(Math.random()*155+100); speed = (float)Math.random()*2+0.5f; }
        void update(int sh) { y += speed; if (y > sh) { y = 0; x = (float)Math.random()*screenWidth; } }
    }
}