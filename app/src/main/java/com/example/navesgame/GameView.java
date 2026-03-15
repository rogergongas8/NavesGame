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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Log;
import androidx.core.content.res.ResourcesCompat;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
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
    private SoundPool soundPool;
    private int soundLaser1, soundLaser2, soundLose, soundZap, soundShieldUp, soundShieldDown;
    private Typeface gameFont;
    private Random random = new Random();

    private int playerX, playerY;
    private int playerSize = 80;
    private int basePlayerSpeed = 15;
    private int playerSpeed = 15;

    private SvgManager svgManager;
    private Bitmap playerBitmap;

    private List<Rock> rocks = new ArrayList<>();
    private Bitmap meteorBrownBig1Bitmap;
    private Bitmap meteorBrownMed1Bitmap;
    private Bitmap meteorBrownSmall1Bitmap;

    // Enemy Bitmaps
    private Bitmap enemyNormalBitmap;
    private Bitmap enemyFastBitmap;
    private Bitmap enemyZigzagBitmap;
    private Bitmap enemyBossBitmap;

    // PowerUp Bitmaps
    private Bitmap powerupShieldBitmap;
    private Bitmap powerupBoltBitmap; // For double/triple shot
    private Bitmap powerupSpeedBitmap;
    private Bitmap powerupSlowBitmap;
    private Bitmap powerupBombBitmap;
    private Bitmap powerupHealthBitmap;

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

    private long lastRockSpawnTime = 0;
    private long rockSpawnDelay = 3000; // Adjust as needed
    private long minRockSpawnDelay = 500; // Minimum delay for rocks


    private List<Particle> particles = new ArrayList<>();
    private Star[] stars;
    private static final int NUM_STARS = 100;

    private long gameTime = 0;
    private int thrusterFrame = 0;



    private static final int BULLET_WIDTH = 10;
    private static final int BULLET_HEIGHT = 30;
    private static final int ENEMY_WIDTH = 80;
    private static final int ENEMY_HEIGHT = 60;

    private RectF[] menuButtons;
    private String[] menuButtonTexts;
    
    private RectF[] mapButtons;
    private String[] mapButtonTexts;
    private RectF[] levelButtons;
    private String[] levelButtonTexts;

    private float screenShake = 0;
    private float shipTilt = 0;
    
    // Transition overlay
    private float transitionAlpha = 0;
    private int pendingState = -1;
    private boolean transitionOut = false;

    // Hit flash
    private long lastHitTime = 0;
    private static final int HIT_FLASH_DURATION = 100;

    public GameView(Context context, int screenX, int screenY) {
        super(context);
        this.screenX = screenX;
        this.screenY = screenY;
        this.surfaceHolder = getHolder();
        this.paint = new Paint();
        this.gameState = new GameState(context);

        this.svgManager = new SvgManager(context);

        playerBitmap = safeGetSprite("playerShip1_blue.png", playerSize, playerSize);

        // Load and scale rock bitmaps
        meteorBrownBig1Bitmap = safeGetSprite("meteorBrown_big1.png", 80, 80);
        meteorBrownMed1Bitmap = safeGetSprite("meteorBrown_med1.png", 50, 50);
        meteorBrownSmall1Bitmap = safeGetSprite("meteorBrown_small1.png", 30, 30);

        // Load and scale enemy bitmaps
        enemyNormalBitmap = safeGetSprite("enemyBlack1.png", ENEMY_WIDTH, ENEMY_HEIGHT);
        enemyFastBitmap = safeGetSprite("enemyBlue2.png", (int)(ENEMY_WIDTH * 0.8f), (int)(ENEMY_HEIGHT * 0.8f));
        enemyZigzagBitmap = safeGetSprite("enemyGreen3.png", (int)(ENEMY_WIDTH * 1.2f), (int)(ENEMY_HEIGHT * 1.2f));
        enemyBossBitmap = safeGetSprite("enemyRed5.png", 300, 200);

        // Load and scale power-up bitmaps
        powerupShieldBitmap = safeGetSprite("powerupBlue_shield.png", PowerUp.POWER_UP_SIZE, PowerUp.POWER_UP_SIZE);
        powerupBoltBitmap = safeGetSprite("powerupBlue_bolt.png", PowerUp.POWER_UP_SIZE, PowerUp.POWER_UP_SIZE);
        powerupSpeedBitmap = safeGetSprite("powerupGreen_bolt.png", PowerUp.POWER_UP_SIZE, PowerUp.POWER_UP_SIZE);
        powerupSlowBitmap = safeGetSprite("powerupRed_star.png", PowerUp.POWER_UP_SIZE, PowerUp.POWER_UP_SIZE);
        powerupBombBitmap = safeGetSprite("powerupYellow.png", PowerUp.POWER_UP_SIZE, PowerUp.POWER_UP_SIZE);
        powerupHealthBitmap = safeGetSprite("pill_red.png", PowerUp.POWER_UP_SIZE, PowerUp.POWER_UP_SIZE);

        initAudio();
        gameFont = ResourcesCompat.getFont(context, R.font.kenvector_future);
        paint.setTypeface(gameFont);

        initStars();
        this.playerX = screenX / 2 - playerSize / 2;
        this.playerY = screenY - 150;
        initMenuButtons();
        initMapButtons();
        initLevelButtons();

    }

    private Bitmap safeGetSprite(String name, int width, int height) {
        Bitmap b = svgManager.getSprite(name);
        if (b == null) {
            Log.e("GameView", "Failed to load sprite: " + name + ". Creating placeholder.");
            b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            Paint p = new Paint();
            p.setColor(Color.MAGENTA); // Visibility for missing sprites
            c.drawRect(0, 0, width, height, p);
            return b;
        }
        return Bitmap.createScaledBitmap(b, width, height, false);
    }

    private void initStars() {
        stars = new Star[NUM_STARS];
        for (int i = 0; i < NUM_STARS; i++) {
            stars[i] = new Star(screenX, screenY, i % 3); // 3 layers
        }
    }

    private void initMenuButtons() {
        menuButtonTexts = new String[]{"JUGAR", "MAPA", "DIFICULTAD", "MEJORES PUNTUACIONES"};
        menuButtons = new RectF[4];
    }

    private void initMapButtons() {
        mapButtonTexts = new String[]{"ESPACIO", "ATARDECER", "AURORA", "VOLVER"};
        mapButtons = new RectF[4];
    }

    private void initLevelButtons() {
        levelButtonTexts = new String[]{"ARCADE", "NIVEL 1", "NIVEL 2", "NIVEL 3", "VOLVER"};
        levelButtons = new RectF[5];
    }

    private void initAudio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }

        soundLaser1 = soundPool.load(getContext(), R.raw.sfx_laser1, 1);
        soundLaser2 = soundPool.load(getContext(), R.raw.sfx_laser2, 1);
        soundLose = soundPool.load(getContext(), R.raw.sfx_lose, 1);
        soundZap = soundPool.load(getContext(), R.raw.sfx_zap, 1);
        soundShieldUp = soundPool.load(getContext(), R.raw.sfx_shieldup, 1);
        soundShieldDown = soundPool.load(getContext(), R.raw.sfx_shielddown, 1);
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
        updateTransition();
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

    private void updateTransition() {
        if (pendingState != -1) {
            if (transitionOut) {
                transitionAlpha += 15;
                if (transitionAlpha >= 255) {
                    transitionAlpha = 255;
                    if (pendingState == -2) restartGame(); // Special case for restart
                    else gameState.setState(pendingState);
                    pendingState = -1;
                    transitionOut = false;
                }
            }
        } else {
            if (transitionAlpha > 0) {
                transitionAlpha -= 15;
                if (transitionAlpha < 0) transitionAlpha = 0;
            }
        }
    }

    private void transitionTo(int newState) {
        pendingState = newState;
        transitionOut = true;
    }

    private void updateGame() {
        long currentTime = System.currentTimeMillis();
        gameState.updatePowerUps();
        float timeScale = gameState.hasSlowMotion() ? 0.5f : 1.0f;

        if (screenShake > 0) screenShake -= 1;

        // Boss check
        if (!gameState.isBossActive() && gameState.getScore() >= gameState.getNextBossScore()) {
            spawnBoss();
        }

        if (currentTime - lastEnemySpawnTime > enemySpawnDelay * timeScale && !gameState.isBossActive()) {
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

        if (currentTime - lastRockSpawnTime > rockSpawnDelay) {
            spawnRock();
            lastRockSpawnTime = currentTime;
            if (gameState.getLevel() == GameState.LEVEL_ARCADE) {
                if (rockSpawnDelay > minRockSpawnDelay) rockSpawnDelay -= 5;
            }
            // Optionally, decrease rockSpawnDelay over time to increase difficulty
        }

        playerSpeed = gameState.hasSpeedBoost() ? (int)(basePlayerSpeed * 1.5f) : basePlayerSpeed;

        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.y -= 25;
            if (bullet.y < -100) bullets.remove(i);
        }

        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemigo enemy = enemies.get(i);
            enemy.update(timeScale);

            if (checkCollision((int)enemy.x, (int)enemy.y, enemy.width, enemy.height, playerX, playerY, playerSize, playerSize)) {
                if (gameState.hasShield()) {
                    gameState.useShield();
                } else {
                    lastHitTime = System.currentTimeMillis();
                    gameState.takeDamage();
                    screenShake = 30;
                    if (gameState.isDead()) {
                        gameOver();
                        return;
                    }
                }
                Particle[] explosion = Particle.createEnemyExplosion((int)enemy.x + enemy.width / 2, (int)enemy.y + enemy.height / 2);
                for (Particle p : explosion) particles.add(p);
                enemies.remove(i);
                playSound(soundZap);
                continue;
            }

            for (int j = bullets.size() - 1; j >= 0; j--) {
                Bullet bullet = bullets.get(j);
                if (checkCollision((int)enemy.x, (int)enemy.y, enemy.width, enemy.height, bullet.x, bullet.y, BULLET_WIDTH, BULLET_HEIGHT)) {
                    enemy.takeDamage(1);
                    bullets.remove(j);
                    if (enemy.isDead()) {
                        Particle[] explosion = Particle.createEnemyExplosion((int)enemy.x + enemy.width / 2, (int)enemy.y + enemy.height / 2);
                        for (Particle p : explosion) particles.add(p);
                        
                        if (enemy.type == Enemigo.TYPE_BOSS) {
                            gameState.setBossActive(false);
                            gameState.addScore(500);
                            gameState.advanceBossScore();
                            // Explosion massive
                            for (int k = 0; k < 5; k++) {
                                Particle[] extra = Particle.createEnemyExplosion((int)enemy.x + random.nextInt(enemy.width), (int)enemy.y + random.nextInt(enemy.height));
                                for (Particle p : extra) particles.add(p);
                            }
                        } else {
                            gameState.addScore(10);
                        }
                        
                        enemies.remove(i);
                        playSound(soundZap);
                    }
                    break;
                }
            }

            if (enemy.y > screenY + 200) {
                if (enemy.type == Enemigo.TYPE_BOSS) gameState.setBossActive(false);
                enemies.remove(i);
            }
        }

        // Update and check collisions for rocks
        for (int i = rocks.size() - 1; i >= 0; i--) {
            Rock rock = rocks.get(i);
            rock.update();

            if (checkCollision((int)rock.x, (int)rock.y, rock.width, rock.height, playerX, playerY, playerSize, playerSize)) {
                if (gameState.hasShield()) {
                    gameState.useShield();
                } else {
                    gameState.takeDamage();
                    screenShake = 20;
                    if (gameState.isDead()) {
                        gameOver();
                        return;
                    }
                }
                Particle[] explosion = Particle.createEnemyExplosion((int)rock.x + rock.width / 2, (int)rock.y + rock.height / 2);
                for (Particle p : explosion) particles.add(p);
                rocks.remove(i);
                playSound(soundZap);
                continue;
            }

            if (rock.isOffScreen(screenY)) {
                rocks.remove(i);
            }
        }

        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            powerUp.update();
            if (checkCollision(powerUp.getX(), powerUp.getY(), PowerUp.POWER_UP_SIZE, PowerUp.POWER_UP_SIZE, playerX, playerY, playerSize, playerSize)) {
                if (powerUp.getType() == PowerUp.TYPE_BOMB) {
                    handleBomb();
                } else {
                    powerUp.applyEffect(gameState);
                }
                playSound(soundShieldUp);
                // Determine color for particle effect based on powerUp type
                int particleColor;
                switch (powerUp.getType()) {
                    case PowerUp.TYPE_SHIELD: particleColor = Color.CYAN; break;
                    case PowerUp.TYPE_DOUBLE_SHOT:
                    case PowerUp.TYPE_TRIPLE_SHOT: particleColor = Color.MAGENTA; break; // Or YELLOW
                    case PowerUp.TYPE_SPEED_BOOST: particleColor = Color.GREEN; break;
                    case PowerUp.TYPE_SLOW_MOTION: particleColor = Color.parseColor("#FFA500"); break; // Orange
                    case PowerUp.TYPE_HEALTH: particleColor = Color.RED; break;
                    case PowerUp.TYPE_BOMB: particleColor = Color.WHITE; break;
                    default: particleColor = Color.WHITE; break;
                }
                Particle[] effects = Particle.createPowerUpCollection(powerUp.getX() + PowerUp.POWER_UP_SIZE / 2, powerUp.getY() + PowerUp.POWER_UP_SIZE / 2, particleColor);
                for (Particle p : effects) particles.add(p);
                powerUps.remove(i);
            } else if (powerUp.isOffScreen(screenY)) {
                powerUps.remove(i);
            }
        }
        thrusterFrame = (thrusterFrame + 1) % 10;
    }

    private void spawnBoss() {
        gameState.setBossActive(true);
        enemies.add(new Enemigo(screenX, screenY, enemyBossBitmap, Enemigo.TYPE_BOSS));
    }

    private void handleBomb() {
        screenShake = 30;
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemigo enemy = enemies.get(i);
            if (enemy.type != Enemigo.TYPE_BOSS) {
                Particle[] explosion = Particle.createEnemyExplosion((int)enemy.x + enemy.width / 2, (int)enemy.y + enemy.height / 2);
                for (Particle p : explosion) particles.add(p);
                enemies.remove(i);
                gameState.addScore(5);
            } else {
                enemy.takeDamage(10);
            }
        }
    }

    private boolean isGameOver() {
        return gameState.getState() == GameState.STATE_GAME_OVER;
    }

    private boolean checkCollision(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    private void spawnEnemy() {
        Bitmap enemyBitmap;
        int enemyType;

        float r = random.nextFloat();
        if (r < 0.7f) {
            enemyType = Enemigo.TYPE_NORMAL;
            enemyBitmap = enemyNormalBitmap;
        } else if (r < 0.85f) {
            enemyType = Enemigo.TYPE_FAST;
            enemyBitmap = enemyFastBitmap;
        } else {
            enemyType = Enemigo.TYPE_ZIGZAG;
            enemyBitmap = enemyZigzagBitmap;
        }
        enemies.add(new Enemigo(screenX, screenY, enemyBitmap, enemyType));
    }

    private void spawnPowerUp() {
        Bitmap powerUpBitmap;
        int type = random.nextInt(7); // Get a random type first

        switch (type) {
            case PowerUp.TYPE_SHIELD:
                powerUpBitmap = powerupShieldBitmap;
                break;
            case PowerUp.TYPE_DOUBLE_SHOT:
            case PowerUp.TYPE_TRIPLE_SHOT:
                powerUpBitmap = powerupBoltBitmap;
                break;
            case PowerUp.TYPE_SPEED_BOOST:
                powerUpBitmap = powerupSpeedBitmap;
                break;
            case PowerUp.TYPE_SLOW_MOTION:
                powerUpBitmap = powerupSlowBitmap;
                break;
            case PowerUp.TYPE_HEALTH:
                powerUpBitmap = powerupHealthBitmap;
                break;
            case PowerUp.TYPE_BOMB:
                powerUpBitmap = powerupBombBitmap;
                break;
            default:
                powerUpBitmap = powerupBoltBitmap; // Fallback
                break;
        }
        powerUps.add(new PowerUp(screenX, screenY, powerUpBitmap, type));
    }

    private void spawnRock() {
        Bitmap rockBitmap;
        int choice = random.nextInt(3); // 0, 1, or 2
        if (choice == 0) {
            rockBitmap = meteorBrownBig1Bitmap;
        } else if (choice == 1) {
            rockBitmap = meteorBrownMed1Bitmap;
        } else {
            rockBitmap = meteorBrownSmall1Bitmap;
        }
        rocks.add(new Rock(screenX, screenY, rockBitmap));
    }

    private void shoot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime > shotDelay) {
            if (gameState.hasTripleShot()) {
                bullets.add(new Bullet(playerX + playerSize / 2 - BULLET_WIDTH / 2, playerY));
                bullets.add(new Bullet(playerX + playerSize / 2 - BULLET_WIDTH / 2 - 30, playerY + 20));
                bullets.add(new Bullet(playerX + playerSize / 2 - BULLET_WIDTH / 2 + 30, playerY + 20));
            } else if (gameState.hasDoubleShot()) {
                bullets.add(new Bullet(playerX + playerSize / 2 - BULLET_WIDTH / 2 - 15, playerY));
                bullets.add(new Bullet(playerX + playerSize / 2 - BULLET_WIDTH / 2 + 15, playerY));
            } else {
                bullets.add(new Bullet(playerX + playerSize / 2 - BULLET_WIDTH / 2, playerY));
            }
            lastShotTime = currentTime;
            playSound(gameState.hasDoubleShot() || gameState.hasTripleShot() ? soundLaser2 : soundLaser1);
        }
    }

    private void gameOver() {
        gameState.setState(GameState.STATE_GAME_OVER);
        gameState.updateHighScore();
        playSound(soundLose);
    }

    private void restartGame() {
        gameState.resetForNewGame();
        enemies.clear();
        bullets.clear();
        powerUps.clear();
        particles.clear();
        rocks.clear();
        enemySpeed = gameState.getEnemySpeed();
        enemySpawnDelay = gameState.getEnemySpawnDelay();
        playerX = screenX / 2 - playerSize / 2;
        playerY = screenY - 150;
        lastEnemySpawnTime = System.currentTimeMillis();
        lastPowerUpSpawnTime = System.currentTimeMillis();
        rockSpawnDelay = 3000; // Reset rock spawn delay
        gameState.setState(GameState.STATE_PLAYING);
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            if (screenShake > 0) {
                canvas.translate((random.nextFloat() - 0.5f) * screenShake, (random.nextFloat() - 0.5f) * screenShake);
            }
            switch (gameState.getState()) {
                case GameState.STATE_MENU: drawMenu(canvas); break;
                case GameState.STATE_MAP_SELECT: drawMapSelect(canvas); break;
                case GameState.STATE_LEVEL_SELECT: drawLevelSelect(canvas); break;
                case GameState.STATE_HIGH_SCORES: drawHighScores(canvas); break;
                case GameState.STATE_PLAYING: drawGame(canvas); break;
                case GameState.STATE_GAME_OVER: drawGame(canvas); drawGameOver(canvas); break;
            }

            // Draw Transition Overlay
            if (transitionAlpha > 0) {
                paint.setColor(Color.BLACK);
                paint.setAlpha((int) transitionAlpha);
                canvas.drawRect(0, 0, screenX, screenY, paint);
                paint.setAlpha(255);
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
            for (Star star : stars) {
                star.update(screenY);
                paint.setColor(star.color);
                paint.setAlpha(star.alpha);
                canvas.drawCircle(star.x, star.y, star.size, paint);
            }
            paint.setAlpha(255);
        }
    }

    private void drawMenu(Canvas canvas) {
        drawBackground(canvas);
        paint.setColor(Color.parseColor("#00d4ff"));
        paint.setTextSize(100);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("NAVES GAME", screenX / 2f, screenY / 4f, paint);
        
        float buttonWidth = screenX * 0.7f;
        float buttonHeight = 110;
        for (int i = 0; i < menuButtons.length; i++) {
            float x = (screenX - buttonWidth) / 2;
            float y = (screenY / 3f) + i * (buttonHeight + 40);
            menuButtons[i] = new RectF(x, y, x + buttonWidth, y + buttonHeight);
            drawStyledButton(canvas, menuButtons[i], menuButtonTexts[i], false);
        }
    }

    private void drawStyledButton(Canvas canvas, RectF rect, String text, boolean isSelected) {
        paint.setStyle(Paint.Style.FILL);
        // Shadow/Glow
        paint.setColor(isSelected ? Color.parseColor("#4400d4ff") : Color.parseColor("#44000000"));
        canvas.drawRoundRect(new RectF(rect.left + 5, rect.top + 5, rect.right + 5, rect.bottom + 5), 20, 20, paint);

        // Body Gradient
        int topColor = isSelected ? Color.parseColor("#00d4ff") : Color.parseColor("#2a2a4a");
        int bottomColor = isSelected ? Color.parseColor("#0088aa") : Color.parseColor("#1a1a2e");
        LinearGradient gradient = new LinearGradient(rect.left, rect.top, rect.left, rect.bottom, 
                topColor, bottomColor, Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        canvas.drawRoundRect(rect, 20, 20, paint);
        paint.setShader(null);

        // Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(isSelected ? Color.WHITE : Color.parseColor("#4a4a8a"));
        canvas.drawRoundRect(rect, 20, 20, paint);

        // Text
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(45);
        paint.setFakeBoldText(true);
        canvas.drawText(text, rect.centerX(), rect.centerY() + 15, paint);
        paint.setFakeBoldText(false);
    }

    private void drawMapSelect(Canvas canvas) {
        drawBackground(canvas);
        paint.setColor(Color.WHITE);
        paint.setTextSize(60);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("SELECCIONAR MAPA", screenX / 2f, screenY / 4f, paint);
        
        float buttonWidth = screenX * 0.7f;
        float buttonHeight = 100;
        for (int i = 0; i < mapButtons.length; i++) {
            float x = (screenX - buttonWidth) / 2;
            float y = (screenY / 3f) + i * (buttonHeight + 30);
            mapButtons[i] = new RectF(x, y, x + buttonWidth, y + buttonHeight);
            drawStyledButton(canvas, mapButtons[i], mapButtonTexts[i], i == gameState.getMap());
        }
    }

    private void drawLevelSelect(Canvas canvas) {
        drawBackground(canvas);
        paint.setColor(Color.WHITE);
        paint.setTextSize(60);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("DIFICULTAD", screenX / 2f, screenY / 4f, paint);
        
        float buttonWidth = screenX * 0.7f;
        float buttonHeight = 100;
        for (int i = 0; i < levelButtons.length; i++) {
            float x = (screenX - buttonWidth) / 2;
            float y = (screenY / 3f) + i * (buttonHeight + 30);
            levelButtons[i] = new RectF(x, y, x + buttonWidth, y + buttonHeight);
            drawStyledButton(canvas, levelButtons[i], levelButtonTexts[i], i == gameState.getLevel());
        }
    }
    private void drawHighScores(Canvas canvas) {
        drawBackground(canvas);
        paint.setColor(Color.WHITE);
        paint.setTextSize(80);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("HIGH SCORE", screenX / 2f, screenY / 4f, paint);
        paint.setTextSize(120);
        paint.setColor(Color.YELLOW);
        canvas.drawText("" + gameState.getHighScore(), screenX / 2f, screenY / 2f, paint);
        
        paint.setTextSize(40);
        paint.setColor(Color.WHITE);
        canvas.drawText("Toca para volver", screenX / 2f, screenY * 0.8f, paint);
    }

    private void handleTouch(float x, float y) {
        if (gameState.getState() == GameState.STATE_MENU) handleMenuTouch(x, y);
        else if (gameState.getState() == GameState.STATE_MAP_SELECT) handleMapTouch(x, y);
        else if (gameState.getState() == GameState.STATE_LEVEL_SELECT) handleLevelTouch(x, y);
        else if (gameState.getState() == GameState.STATE_HIGH_SCORES) transitionTo(GameState.STATE_MENU);
        else if (gameState.getState() == GameState.STATE_PLAYING) shoot();
        else if (gameState.getState() == GameState.STATE_GAME_OVER) transitionTo(-2); // Restart
    }

    private void handleMenuTouch(float x, float y) {
        if (menuButtons[0].contains(x, y)) transitionTo(-2);
        else if (menuButtons[1].contains(x, y)) transitionTo(GameState.STATE_MAP_SELECT);
        else if (menuButtons[2].contains(x, y)) transitionTo(GameState.STATE_LEVEL_SELECT);
        else if (menuButtons[3].contains(x, y)) transitionTo(GameState.STATE_HIGH_SCORES);
    }

    private void handleMapTouch(float x, float y) {
        for (int i = 0; i < 3; i++) {
            if (mapButtons[i].contains(x, y)) gameState.setMap(i);
        }
        if (mapButtons[3].contains(x, y)) transitionTo(GameState.STATE_MENU);
    }

    private void handleLevelTouch(float x, float y) {
        for (int i = 0; i < 4; i++) {
            if (levelButtons[i].contains(x, y)) gameState.setLevel(i);
        }
        if (levelButtons[4].contains(x, y)) transitionTo(GameState.STATE_MENU);
    }

    private void drawGame(Canvas canvas) {
        drawBackground(canvas);

        for (Particle particle : particles) {
            paint.setColor(particle.getColor());
            canvas.drawCircle(particle.getX(), particle.getY(), particle.getSize(), paint);
        }

        // Draw Player
        drawPlayer(canvas);

        // Draw Bullets with Glow
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#ffeb3b"));
        for (Bullet bullet : bullets) {
            // Glow effect
            paint.setShadowLayer(15, 0, 0, Color.parseColor("#FFFF00"));
            canvas.drawRect(bullet.x, bullet.y, bullet.x + BULLET_WIDTH, bullet.y + BULLET_HEIGHT, paint);
            paint.clearShadowLayer();
        }

        for (Enemigo enemy : enemies) {
            drawEnemy(canvas, enemy);
        }

        // Draw rocks
        for (Rock rock : rocks) {
            canvas.save();
            // Simple rotation based on time and a random-ish factor
            float rotation = (float)((gameTime * 0.1) + (rock.x % 360));
            canvas.rotate(rotation, rock.x + rock.width / 2f, rock.y + rock.height / 2f);
            canvas.drawBitmap(rock.getBitmap(), rock.x, rock.y, paint);
            canvas.restore();
        }

        for (PowerUp powerUp : powerUps) {
            canvas.drawBitmap(powerUp.getBitmap(), powerUp.getX(), powerUp.getY(), paint);
        }

        drawUI(canvas);
    }

    private void drawPlayer(Canvas canvas) {
        // Hit flash
        boolean isHit = System.currentTimeMillis() - lastHitTime < HIT_FLASH_DURATION;
        if (isHit) {
            paint.setColorFilter(new android.graphics.LightingColorFilter(Color.WHITE, 0));
        }

        // Tilt effect
        canvas.save();
        canvas.rotate(shipTilt, playerX + playerSize / 2f, playerY + playerSize / 2f);
        
        // Draw the player bitmap
        canvas.drawBitmap(playerBitmap, playerX, playerY, paint);
        
        if (isHit) {
            paint.setColorFilter(null);
        }

        // Thruster
        if (thrusterFrame < 5) {
            paint.setColor(Color.parseColor("#ff6b35"));
            canvas.drawCircle(playerX + playerSize / 2f, playerY + playerSize, playerSize * (0.15f + random.nextFloat() * 0.1f), paint);
        }
        
        canvas.restore();

        if (gameState.hasShield()) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setColor(Color.CYAN);
            canvas.drawCircle(playerX + playerSize / 2f, playerY + playerSize / 2f, playerSize * 0.8f, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private void drawEnemy(Canvas canvas, Enemigo enemy) {
        canvas.drawBitmap(enemy.getBitmap(), enemy.x, enemy.y, paint);
        if (enemy.type == Enemigo.TYPE_BOSS) {
            // Boss Health bar
            paint.setColor(Color.DKGRAY);
            canvas.drawRect(enemy.x, enemy.y - 30, enemy.x + enemy.width, enemy.y - 10, paint);
            paint.setColor(Color.RED);
            float healthWidth = (enemy.width * enemy.health) / (float) enemy.maxHealth;
            canvas.drawRect(enemy.x, enemy.y - 30, enemy.x + healthWidth, enemy.y - 10, paint);
        }
    }

    private void drawUI(Canvas canvas) {
        // Top HUD panel
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#AA000000"));
        canvas.drawRoundRect(10, 10, 450, 150, 20, 20, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.parseColor("#44FFFFFF"));
        canvas.drawRoundRect(10, 10, 450, 150, 20, 20, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        paint.setFakeBoldText(true);
        canvas.drawText("Puntos: " + gameState.getScore(), 30, 70, paint);
        paint.setFakeBoldText(false);

        // Health icons (Hearts)
        for (int i = 0; i < 3; i++) { // Max health
            paint.setColor(i < gameState.getPlayerHealth() ? Color.parseColor("#FF4444") : Color.DKGRAY);
            drawHeart(canvas, 50 + i * 60, 110, 20);
        }

        // Power-up indicators (Bottom side)
        float indicatorY = screenY - 100;
        paint.setTextSize(30);
        paint.setFakeBoldText(true);
        
        if (gameState.hasShield()) {
            drawPowerUpLabel(canvas, "ESCUDO", Color.CYAN, indicatorY); indicatorY -= 45;
        }
        if (gameState.hasTripleShot()) {
            drawPowerUpLabel(canvas, "TRIPLE DISPARO", Color.YELLOW, indicatorY); indicatorY -= 45;
        } else if (gameState.hasDoubleShot()) {
            drawPowerUpLabel(canvas, "DOBLE DISPARO", Color.MAGENTA, indicatorY); indicatorY -= 45;
        }
        if (gameState.hasSpeedBoost()) {
            drawPowerUpLabel(canvas, "VELOCIDAD+", Color.GREEN, indicatorY); indicatorY -= 45;
        }
        if (gameState.hasSlowMotion()) {
            drawPowerUpLabel(canvas, "SLOW MO", Color.parseColor("#FFA500"), indicatorY);
        }
        paint.setFakeBoldText(false);
    }

    private void drawHeart(Canvas canvas, float x, float y, float size) {
        Path path = new Path();
        path.moveTo(x, y + size / 4);
        path.cubicTo(x - size, y - size, x - size, y + size, x, y + size * 1.5f);
        path.cubicTo(x + size, y + size, x + size, y - size, x, y + size / 4);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawPowerUpLabel(Canvas canvas, String text, int color, float y) {
        paint.setColor(Color.parseColor("#AA000000"));
        canvas.drawRoundRect(10, y - 35, 350, y + 15, 10, 10, paint);
        paint.setColor(color);
        canvas.drawRect(10, y - 35, 20, y + 15, paint);
        canvas.drawText(text, 40, y + 5, paint);
    }

    private void drawGameOver(Canvas canvas) {
        paint.setColor(Color.parseColor("#80000000"));
        canvas.drawRect(0, 0, screenX, screenY, paint);
        paint.setColor(Color.RED); paint.setTextSize(80); paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("GAME OVER", screenX / 2f, screenY / 2f, paint);
    }

    private void sleep() { try { Thread.sleep(17); } catch (InterruptedException e) {} }
    private void playSound(int soundId) {
        if (soundPool != null) {
            soundPool.play(soundId, 1, 1, 0, 0, 1);
        }
    }
    public void resume() { isPlaying = true; thread = new Thread(this); thread.start(); }
    public void pause() { isPlaying = false; try { thread.join(); } catch (Exception e) {} }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) handleTouch(event.getX(), event.getY());
        if (event.getAction() == MotionEvent.ACTION_MOVE && gameState.getState() == GameState.STATE_PLAYING) {
            float targetX = event.getX() - playerSize / 2f;
            float diff = targetX - playerX;
            shipTilt = Math.max(-15, Math.min(15, diff * 0.5f)); // Dynamic tilt
            playerX = (int) targetX;
            shoot();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            shipTilt = 0;
        }
        return true;
    }

    private static class Bullet { 
        int x, y; 
        Bullet(int x, int y) { this.x = x; this.y = y; } 
    }

    private static class Star {
        float x, y, size, speed; 
        int alpha, screenWidth, color;
        
        Star(int sw, int sh, int layer) { 
            screenWidth = sw; 
            x = (float)Math.random()*sw; 
            y = (float)Math.random()*sh; 
            
            // Layer 0: Far (small, slow, dim)
            // Layer 1: Mid
            // Layer 2: Near (large, fast, bright)
            if (layer == 0) {
                size = (float)Math.random()*1+1;
                speed = (float)Math.random()*1+0.5f;
                alpha = (int)(Math.random()*100+50);
                color = Color.parseColor("#88aaff"); // Bluish tint
            } else if (layer == 1) {
                size = (float)Math.random()*1+2;
                speed = (float)Math.random()*2+1.5f;
                alpha = (int)(Math.random()*155+100);
                color = Color.WHITE;
            } else {
                size = (float)Math.random()*2+3;
                speed = (float)Math.random()*3+3f;
                alpha = (int)(Math.random()*55+200);
                color = Color.parseColor("#fff4e0"); // Warm tint
            }
        }
        
        void update(int sh) { 
            y += speed; 
            if (y > sh) { 
                y = -50; 
                x = (float)Math.random()*screenWidth; 
            } 
        } 
    }
}