package com.example.navesgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isPlaying;
    private boolean isPaused = false;
    private GameState gameState;
    private int screenX, screenY;
    private Paint paint;
    private SurfaceHolder surfaceHolder;
    private ToneGenerator toneGenerator;
    private Random random = new Random();

    private boolean isLandscape = false;

    private RectF[] mapButtons = new RectF[3];
    private String[] mapNames = {"ESPACIO", "ATARDECER", "AURORA"};

    private int playerX, playerY, playerSize = 80;
    private List<Bullet> bullets = new ArrayList<>();
    private List<Bullet> enemyBullets = new ArrayList<>();
    private List<Enemigo> enemies = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();
    private Star[] stars;
    
    private long lastTime;
    private static final int FPS = 60;
    private static final long TIME_PER_FRAME = 1000000000 / FPS;

    private int screenShakeFrames = 0, combo = 0;
    private long gameTime = 0, lastShotTime = 0, lastKillTime = 0;
    
    private boolean isTouching = false;
    private float lastTouchX, lastTouchY;
    private boolean moveLeft = false, moveRight = false, moveUp = false, moveDown = false;
    private boolean keyShoot = false;

    private static final int BULLET_SIZE = 20;
    private static final int ENEMY_BULLET_SIZE = 25;
    private static final int POWERUP_SIZE = 60;

    private static final int[][] SPRITE_PLAYER = {{0,0,0,2,0,0,0},{0,0,1,1,1,0,0},{0,1,1,2,1,1,0},{1,1,0,1,0,1,1},{1,1,1,1,1,1,1},{1,0,1,0,1,0,1}};
    private static final int[][] SPRITE_BOSS = {{0,0,1,1,1,1,1,0,0},{0,1,2,2,2,2,2,1,0},{1,2,1,1,2,1,1,2,1},{1,2,2,2,1,2,2,2,1},{1,1,1,1,1,1,1,1,1},{0,1,0,1,0,1,0,1,0}};

    public GameView(Context context, int screenX, int screenY) {
        super(context);
        this.screenX = screenX; this.screenY = screenY;
        this.isLandscape = screenX > screenY;
        this.surfaceHolder = getHolder();
        this.paint = new Paint();
        this.paint.setTypeface(Typeface.MONOSPACE);
        this.gameState = new GameState();
        try { toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100); } catch (Exception e) {}
        
        stars = new Star[100];
        for (int i = 0; i < 100; i++) stars[i] = new Star(screenX, screenY);
        
        resetPlayerPosition();
        initMenuButtons();
        loadHighScore();
        setFocusable(true);
        requestFocus();
    }

    private void resetPlayerPosition() {
        if (isLandscape) {
            this.playerX = 100;
            this.playerY = screenY / 2 - 40;
        } else {
            this.playerX = screenX / 2 - 40;
            this.playerY = screenY - 250;
        }
    }

    private void initMenuButtons() {
        float btnW = isLandscape ? 450 : screenX * 0.7f;
        float btnH = 100;
        float startY = screenY * 0.4f;
        for (int i = 0; i < 3; i++) {
            float x = (screenX - btnW) / 2;
            float y = startY + i * (btnH + 30);
            mapButtons[i] = new RectF(x, y, x + btnW, y + btnH);
        }
    }

    private void loadHighScore() {
        SharedPreferences prefs = getContext().getSharedPreferences("NavesGamePrefs", Context.MODE_PRIVATE);
        gameState.setHighScore(prefs.getInt("highScore", 0));
    }

    @Override
    public void run() {
        lastTime = System.nanoTime();
        while (isPlaying) {
            long now = System.nanoTime();
            if (now - lastTime >= TIME_PER_FRAME) {
                update();
                drawFrame();
                lastTime = now;
            } else {
                try { Thread.sleep(1); } catch (Exception e) {}
            }
        }
    }

    private void update() {
        boolean currentLandscape = getWidth() > getHeight();
        if (currentLandscape != isLandscape) {
            float ratioX = (float)getWidth() / screenX;
            float ratioY = (float)getHeight() / screenY;
            isLandscape = currentLandscape;
            screenX = getWidth();
            screenY = getHeight();
            
            // Reposicionar jugador y objetos proporcionalmente
            playerX *= ratioX;
            playerY *= ratioY;
            for (Enemigo e : enemies) { e.x *= ratioX; e.y *= ratioY; }
            for (Bullet b : bullets) { b.x *= ratioX; b.y *= ratioY; }
            for (Bullet b : enemyBullets) { b.x *= ratioX; b.y *= ratioY; }
            for (PowerUp p : powerUps) { p.setX((int)(p.getX() * ratioX)); p.setY((int)(p.getY() * ratioY)); }
            
            initMenuButtons();
        }

        if (gameState.getState() == GameState.STATE_PLAYING) {
            if (!isPaused) {
                handleKeyboardMovement();
                updateGame();
                if (isTouching || keyShoot) shoot();
            }
        }
        for (int i = particles.size() - 1; i >= 0; i--) {
            particles.get(i).update();
            if (!particles.get(i).isActive()) particles.remove(i);
        }
        
        // Estela del motor del jugador
        if (gameState.getState() == GameState.STATE_PLAYING) {
            float tailX = playerX + (isLandscape ? -20 : playerSize/2);
            float tailY = playerY + (isLandscape ? playerSize/2 : playerSize);
            particles.add(new Particle(tailX, tailY, Color.YELLOW, isLandscape ? -5 : 0, isLandscape ? 0 : 5));
        }
        if (screenShakeFrames > 0) screenShakeFrames--;
        gameTime += 16;
    }

    private void handleKeyboardMovement() {
        int speed = gameState.hasSpeedBoost() ? 25 : 15;
        if (!isLandscape) {
            // En VERTICAL: Solo movimiento Horizontal (X)
            if (moveLeft) playerX -= speed;
            if (moveRight) playerX += speed;
        } else {
            // En HORIZONTAL: Solo movimiento Vertical (Y)
            if (moveUp) playerY -= speed;
            if (moveDown) playerY += speed;
        }
        
        if (playerX < 0) playerX = 0;
        if (playerX > screenX - playerSize) playerX = screenX - playerSize;
        if (playerY < 0) playerY = 0;
        if (playerY > screenY - playerSize) playerY = screenY - playerSize;
    }

    private void updateGame() {
        gameState.updatePowerUps();
        long now = System.currentTimeMillis();
        float timeScale = gameState.hasSlowMotion() ? 0.4f : 1.0f;
        
        if (gameState.getScore() >= gameState.getLastBossScore() + 1500) {
            if (!gameState.isBossActive()) {
                enemies.add(new Enemigo(screenX, screenY, true, isLandscape, gameState));
                gameState.setBossActive(true);
                gameState.setLastBossScore(gameState.getScore());
            }
        }

        if (!gameState.isBossActive() && random.nextFloat() * 100 < gameState.getSpawnChance(timeScale)) {
            enemies.add(new Enemigo(screenX, screenY, isLandscape, gameState));
        }
        if (random.nextInt(1000) < 4) powerUps.add(new PowerUp(screenX, screenY, 0));

        for (int i = bullets.size()-1; i>=0; i--) {
            Bullet b = bullets.get(i);
            b.x += b.vx; b.y += b.vy;
            if (b.x < -100 || b.y < -100 || b.x > screenX + 100 || b.y > screenY + 100) bullets.remove(i);
        }

        for (int i = enemyBullets.size()-1; i>=0; i--) {
            Bullet b = enemyBullets.get(i);
            b.x += b.vx; b.y += b.vy;
            if (checkCollision((int)b.x, (int)b.y, ENEMY_BULLET_SIZE, ENEMY_BULLET_SIZE, playerX, playerY, playerSize, playerSize)) {
                if (gameState.hasShield()) { gameState.useShield(); enemyBullets.remove(i); screenShakeFrames = 15; }
                else { gameOver(); return; }
            }
            if (b.x > screenX + 100 || b.y > screenY + 100 || b.x < -100 || b.y < -100) enemyBullets.remove(i);
        }

        for (int i = enemies.size()-1; i>=0; i--) {
            Enemigo e = enemies.get(i);
            e.updateOriented(screenX, screenY, isLandscape);
            
            // Patrón de Ataque Avanzado del Boss
            if (e.isBoss) {
                // Ataque 1: Ráfaga Circular (Cada 5 segundos)
                if (gameTime % 5000 < 100) {
                    for (int angle = 0; angle < 360; angle += 45) {
                        if (angle == 180 || angle == 225) continue; // Huecos para esquivar
                        float rad = (float) Math.toRadians(angle);
                        float vx = (float) (Math.cos(rad) * 15);
                        float vy = (float) (Math.sin(rad) * 15);
                        enemyBullets.add(new Bullet(e.x + (isLandscape ? 50 : 150), e.y + (isLandscape ? 100 : 150), vx, vy));
                    }
                }
                // Ataque 2: Cadena Sinusoidal Esquivable (Cada 2 segundos)
                if (gameTime % 2000 < 800 && gameTime % 150 < 30) {
                    float offset = (float) Math.sin(gameTime / 200.0) * 10;
                    float vx = isLandscape ? -20 : offset;
                    float vy = isLandscape ? offset : 20;
                    enemyBullets.add(new Bullet(e.x + (isLandscape ? 50 : 150), e.y + (isLandscape ? 100 : 150), vx, vy));
                }
            }

            int ew = e.isBoss ? 300 : 80, eh = e.isBoss ? 200 : 80;

            if (checkCollision(e.x, e.y, ew, eh, playerX, playerY, playerSize, playerSize)) {
                if (gameState.hasShield()) { gameState.useShield(); if(!e.isBoss) enemies.remove(i); screenShakeFrames = 25; }
                else { gameOver(); return; }
            }

            for (int j = bullets.size()-1; j>=0; j--) {
                Bullet b = bullets.get(j);
                if (checkCollision(e.x, e.y, ew, eh, b.x, b.y, BULLET_SIZE, BULLET_SIZE)) {
                    bullets.remove(j); e.hp--;
                    if (e.hp <= 0) {
                        Particle[] exp = Particle.createEnemyExplosion(e.x + ew/2, e.y + eh/2, e.color);
                        for (Particle p : exp) particles.add(p);
                        if (e.isBoss) { gameState.setBossActive(false); gameState.addScore(500); screenShakeFrames = 60; }
                        else { combo++; lastKillTime = now; gameState.addScore(20 + combo*4); enemies.remove(i); }
                        playSound(ToneGenerator.TONE_PROP_BEEP2, 50);
                        break;
                    }
                }
            }
            if (isLandscape && e.x < -350) enemies.remove(i);
            else if (!isLandscape && e.y > screenY + 150) enemies.remove(i);
        }

        for (int i = powerUps.size()-1; i>=0; i--) {
            PowerUp p = powerUps.get(i); p.update();
            if (checkCollision(p.getX(), p.getY(), POWERUP_SIZE, POWERUP_SIZE, playerX, playerY, playerSize, playerSize)) {
                p.applyEffect(gameState, this); powerUps.remove(i);
                playSound(ToneGenerator.TONE_PROP_BEEP, 100);
            } else if (p.isOffScreen(screenY)) powerUps.remove(i);
        }
        if (now - lastKillTime > 1500) combo = 0;
    }

    private void shoot() {
        long now = System.currentTimeMillis();
        if (now - lastShotTime > 140) {
            int mx = playerX + playerSize/2 - 10;
            int my = playerY + playerSize/2 - 10;
            float vx = isLandscape ? 40 : 0;
            float vy = isLandscape ? 0 : -40;

            bullets.add(new Bullet(isLandscape ? playerX + playerSize : mx, isLandscape ? my : playerY, vx, vy));
            
            if (gameState.hasDoubleShot()) {
                bullets.add(new Bullet(isLandscape ? playerX + playerSize - 25 : mx - 35, isLandscape ? my - 35 : playerY + 25, vx, vy));
            }
            if (gameState.hasTripleShot()) { 
                bullets.add(new Bullet(isLandscape ? playerX + playerSize - 25 : mx + 35, isLandscape ? my + 35 : playerY + 25, vx, vy)); 
                bullets.add(new Bullet(isLandscape ? playerX + playerSize + 25 : mx, isLandscape ? my : playerY - 35, vx, vy)); 
            }
            lastShotTime = now; playSound(ToneGenerator.TONE_PROP_BEEP, 40);
        }
    }

    private void renderGame(Canvas canvas) {
        canvas.save();
        if (screenShakeFrames > 0) canvas.translate(random.nextInt(34)-17, random.nextInt(34)-17);
        drawBackground(canvas);
        
        if (gameState.getState() == GameState.STATE_PLAYING || gameState.getState() == GameState.STATE_GAME_OVER) {
            for (PowerUp p : powerUps) drawPixelSprite(canvas, p.getX(), p.getY(), 12, p.getColor(), Color.WHITE, SPRITE_PLAYER);
            for (Particle p : particles) { paint.setColor(p.getColor()); canvas.drawRect(p.getX(), p.getY(), p.getX()+p.getSize(), p.getY()+p.getSize(), paint); }
            
            if (gameState.hasShield()) {
                paint.setColor(Color.CYAN); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(8);
                canvas.drawCircle(playerX+playerSize/2, playerY+playerSize/2, 65, paint); paint.setStyle(Paint.Style.FILL);
            }
            
            canvas.save();
            if (isLandscape) canvas.rotate(-90, playerX + playerSize/2, playerY + playerSize/2);
            drawPixelSprite(canvas, playerX, playerY, 12, Color.CYAN, Color.WHITE, SPRITE_PLAYER);
            canvas.restore();
            
            paint.setColor(Color.YELLOW);
            for (Bullet b : bullets) canvas.drawRect(b.x, b.y, b.x+20, b.y+20, paint);
            paint.setColor(Color.RED);
            for (Bullet b : enemyBullets) canvas.drawRect(b.x, b.y, b.x+25, b.y+25, paint);

            for (Enemigo e : enemies) {
                if (e.isBoss) {
                    canvas.save();
                    if (isLandscape) canvas.rotate(90, e.x+150, e.y+100);
                    drawPixelSprite(canvas, e.x, e.y, 25, Color.RED, Color.YELLOW, SPRITE_BOSS);
                    canvas.restore();
                    paint.setColor(Color.DKGRAY); canvas.drawRect(100, 50, screenX-100, 80, paint);
                    paint.setColor(Color.RED); canvas.drawRect(100, 50, 100 + (screenX-200)*(e.hp/(float)e.maxHp), 80, paint);
                } else {
                    canvas.save();
                    if (isLandscape) canvas.rotate(90, e.x+40, e.y+40);
                    drawPixelSprite(canvas, e.x, e.y, 12, e.color, Color.BLACK, SPRITE_PLAYER);
                    canvas.restore();
                }
            }
            paint.setTextAlign(Paint.Align.LEFT); paint.setColor(Color.WHITE); paint.setTextSize(60);
            canvas.drawText("SCORE: " + gameState.getScore(), 60, 160, paint);
            if (combo > 1) { paint.setColor(Color.YELLOW); canvas.drawText("COMBO X" + combo, 60, 230, paint); }
        }
        
        if (gameState.getState() == GameState.STATE_MENU) {
            paint.setColor(Color.CYAN); paint.setTextSize(130); paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("PIXEL NAVES", screenX/2, screenY/4, paint);
            
            paint.setColor(Color.WHITE); paint.setTextSize(50);
            canvas.drawText("MEJOR PUNTUACIÓN: " + gameState.getHighScore(), screenX/2, screenY/4 + 100, paint);

            for (int i = 0; i < 3; i++) {
                paint.setColor(Color.parseColor("#222222")); canvas.drawRoundRect(mapButtons[i], 20, 20, paint);
                paint.setColor(Color.WHITE); paint.setTextSize(45);
                canvas.drawText(mapNames[i], screenX/2, mapButtons[i].centerY() + 15, paint);
            }
        }
        
        if (isPaused) {
            paint.setColor(Color.argb(150, 0, 0, 0));
            canvas.drawRect(0, 0, screenX, screenY, paint);
            paint.setColor(Color.WHITE); paint.setTextSize(100); paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("PAUSA", screenX/2, screenY/2, paint);
            paint.setTextSize(50);
            canvas.drawText("TOCA PARA CONTINUAR", screenX/2, screenY/2 + 100, paint);
        }
        
        if (gameState.getState() == GameState.STATE_GAME_OVER) {
            paint.setColor(Color.RED); paint.setTextSize(120); paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("FIN DE MISION", screenX/2, screenY/2, paint);
            paint.setTextSize(50); paint.setColor(Color.WHITE);
            canvas.drawText("TOCA PANTALLA PARA REINTENTAR", screenX/2, screenY/2 + 150, paint);
        }
        canvas.restore();
    }

    private void drawPixelSprite(Canvas canvas, int x, int y, int ps, int c1, int c2, int[][] s) {
        for (int r=0; r<s.length; r++) for (int c=0; c<s[r].length; c++) {
            if (s[r][c] == 0) continue; paint.setColor(s[r][c] == 1 ? c1 : c2);
            canvas.drawRect(x+c*ps, y+r*ps, x+(c+1)*ps, y+(r+1)*ps, paint);
        }
    }

    private void drawBackground(Canvas canvas) {
        int map = gameState.getMap();
        if (map == GameState.MAP_SUNSET) {
            paint.setShader(new LinearGradient(0,0,0,screenY, Color.parseColor("#1a0033"), Color.parseColor("#ff4400"), Shader.TileMode.CLAMP));
            canvas.drawRect(0,0,screenX,screenY,paint); paint.setShader(null);
        } else if (map == GameState.MAP_AURORA) {
            canvas.drawColor(Color.parseColor("#050a14"));
            paint.setColor(Color.parseColor("#00ff99")); paint.setAlpha(25);
            canvas.drawRect(0, screenY*0.5f, screenX, screenY, paint); paint.setAlpha(255);
        } else canvas.drawColor(Color.BLACK);

        paint.setColor(Color.WHITE);
        for (Star s : stars) {
            if (isLandscape) { s.x -= s.speed; if (s.x < 0) s.x = screenX; }
            else { s.y += s.speed; if (s.y > screenY) s.y = 0; }
            paint.setAlpha(random.nextInt(150)+105);
            canvas.drawRect(s.x, s.y, s.x+5, s.y+5, paint);
        }
        paint.setAlpha(255);
    }

    private void drawFrame() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas(); 
            if (canvas != null) { renderGame(canvas); surfaceHolder.unlockCanvasAndPost(canvas); }
        }
    }

    private void gameOver() { gameState.setState(GameState.STATE_GAME_OVER); gameState.updateHighScore(); isTouching = false; }
    private void sleep() { try { Thread.sleep(1); } catch (Exception e) {} }
    private void playSound(int t, int d) { if (toneGenerator != null) toneGenerator.startTone(t, d); }
    public void resume() { isPlaying = true; thread = new Thread(this); thread.start(); }
    public void pause() { isPlaying = false; try { thread.join(); } catch (Exception e) {} }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float tx = event.getX(); float ty = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (gameState.getState() == GameState.STATE_MENU) {
                    for (int i = 0; i < 3; i++) if (mapButtons[i].contains(tx, ty)) {
                        gameState.setMap(i); gameState.resetForNewGame(); gameState.setState(GameState.STATE_PLAYING);
                        enemies.clear(); bullets.clear(); powerUps.clear(); enemyBullets.clear(); return true;
                    }
                } else if (gameState.getState() == GameState.STATE_GAME_OVER) {
                    gameState.setState(GameState.STATE_MENU);
                } else if (isPaused) {
                    isPaused = false;
                } else if (tx > screenX - 150 && ty < 150) { // Esquina superior derecha para pausa
                    isPaused = true;
                } else {
                    isTouching = true;
                    lastTouchX = tx;
                    lastTouchY = ty;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (gameState.getState() == GameState.STATE_PLAYING && isTouching) {
                    float dx = tx - lastTouchX;
                    float dy = ty - lastTouchY;
                    if (!isLandscape) playerX += dx; // En vertical solo X
                    else playerY += dy; // En horizontal solo Y
                    lastTouchX = tx;
                    lastTouchY = ty;
                }
                break;
            case MotionEvent.ACTION_UP: isTouching = false; break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (gameState.getState() == GameState.STATE_PLAYING) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) moveLeft = true;
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) moveRight = true;
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) moveUp = true;
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) moveDown = true;
            if (keyCode == KeyEvent.KEYCODE_SPACE) keyShoot = true;
        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (gameState.getState() == GameState.STATE_MENU) {
                gameState.setMap(0); gameState.resetForNewGame(); gameState.setState(GameState.STATE_PLAYING);
                enemies.clear(); bullets.clear(); powerUps.clear();
            } else if (gameState.getState() == GameState.STATE_GAME_OVER) gameState.setState(GameState.STATE_MENU);
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        moveLeft = moveRight = moveUp = moveDown = keyShoot = false;
        return true;
    }

    private boolean checkCollision(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    public void triggerBomb() {
        screenShakeFrames = 45;
        for (Enemigo e : enemies) {
            Particle[] exp = Particle.createEnemyExplosion(e.x+50, e.y+50, e.color);
            for (Particle p : exp) particles.add(p);
        }
        enemies.clear(); enemyBullets.clear(); gameState.setBossActive(false);
        playSound(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500);
    }

    private static class Bullet { 
        float x, y, vx, vy; 
        Bullet(float x, float y, float vx, float vy) { this.x = x; this.y = y; this.vx = vx; this.vy = vy; } 
    }
    private static class Star { float x, y, speed; Star(int sw, int sh) { x = (float)Math.random()*sw; y = (float)Math.random()*sh; speed = (float)Math.random()*25+10; } }
}