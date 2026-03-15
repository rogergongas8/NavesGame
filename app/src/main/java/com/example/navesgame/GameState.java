package com.example.navesgame;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * GameState manages the current state of the game including
 * selected map, level, score, and other game settings.
 */
public class GameState {

    // Game states
    public static final int STATE_MENU = 0;
    public static final int STATE_MAP_SELECT = 1;
    public static final int STATE_LEVEL_SELECT = 2;
    public static final int STATE_HIGH_SCORES = 3;
    public static final int STATE_PLAYING = 4;
    public static final int STATE_GAME_OVER = 5;
    public static final int STATE_SETTINGS = 6;

    // Map types
    public static final int MAP_SPACE = 0;
    public static final int MAP_SUNSET = 1;
    public static final int MAP_AURORA = 2;

    // Level types
    public static final int LEVEL_ARCADE = 0;
    public static final int LEVEL_1 = 1;
    public static final int LEVEL_2 = 2;
    public static final int LEVEL_3 = 3;

    // Current state
    private int currentState = STATE_MENU;
    private int selectedMap = MAP_SPACE;
    private int selectedLevel = LEVEL_ARCADE;

    // Player Stats
    private int playerHealth = 3;
    private int maxPlayerHealth = 3;
    private boolean isBossActive = false;
    private int nextBossScore = 500;

    // Score
    private int currentScore = 0;
    private int highScore = 0;

    // Difficulty settings per level
    private int enemySpeed = 5;
    private long enemySpawnDelay = 1500;
    private int maxEnemies = 10;

    // Power-up states
    private boolean hasShield = false;
    private boolean hasDoubleShot = false;
    private boolean hasTripleShot = false;
    private boolean hasSpeedBoost = false;
    private boolean hasSlowMotion = false;

    // Power-up timers
    private long shieldEndTime = 0;
    private long doubleShotEndTime = 0;
    private long tripleShotEndTime = 0;
    private long speedBoostEndTime = 0;
    private long slowMotionEndTime = 0;

    private Context context; // Added for SharedPreferences
    private static final String PREFS_NAME = "NavesGamePrefs"; // Added for SharedPreferences
    private static final String HIGH_SCORE_KEY = "highScore"; // Added for SharedPreferences

    public GameState(Context context) { // Modified constructor
        this.context = context;
        loadHighScore();
    }

    public void setState(int state) {
        this.currentState = state;
    }

    public int getState() {
        return currentState;
    }

    public void setMap(int map) {
        this.selectedMap = map;
    }

    public int getMap() {
        return selectedMap;
    }

    public void setLevel(int level) {
        this.selectedLevel = level;
        applyLevelSettings();
    }

    public int getLevel() {
        return selectedLevel;
    }

    private void applyLevelSettings() {
        playerHealth = 3;
        isBossActive = false;
        nextBossScore = 500;
        switch (selectedLevel) {
            case LEVEL_1:
                enemySpeed = 4;
                enemySpawnDelay = 2000;
                maxEnemies = 5;
                break;
            case LEVEL_2:
                enemySpeed = 7;
                enemySpawnDelay = 1500;
                maxEnemies = 8;
                break;
            case LEVEL_3:
                enemySpeed = 10;
                enemySpawnDelay = 1000;
                maxEnemies = 12;
                break;
            case LEVEL_ARCADE:
            default:
                enemySpeed = 5;
                enemySpawnDelay = 1500;
                maxEnemies = 10;
                break;
        }
    }

    public int getPlayerHealth() { return playerHealth; }
    public void takeDamage() { if (!hasShield) playerHealth--; }
    public void addHealth() { if (playerHealth < maxPlayerHealth) playerHealth++; }
    public boolean isDead() { return playerHealth <= 0; }
    public boolean isBossActive() { return isBossActive; }
    public void setBossActive(boolean active) { this.isBossActive = active; }
    public int getNextBossScore() { return nextBossScore; }
    public void advanceBossScore() { nextBossScore += 500; }

    public int getEnemySpeed() {
        return enemySpeed;
    }

    public void setEnemySpeed(int speed) {
        this.enemySpeed = speed;
    }

    public long getEnemySpawnDelay() {
        return enemySpawnDelay;
    }

    public void setEnemySpawnDelay(long delay) {
        this.enemySpawnDelay = delay;
    }

    public int getMaxEnemies() {
        return maxEnemies;
    }

    public void setScore(int score) {
        this.currentScore = score;
    }

    public int getScore() {
        return currentScore;
    }

    public void addScore(int points) {
        this.currentScore += points;
    }

    public void setHighScore(int score) {
        this.highScore = score;
    }

    public int getHighScore() {
        return highScore;
    }

    public void updateHighScore() {
        if (currentScore > highScore) {
            highScore = currentScore;
            saveHighScore();
        }
    }

    private void loadHighScore() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        highScore = prefs.getInt(HIGH_SCORE_KEY, 0);
    }

    private void saveHighScore() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(HIGH_SCORE_KEY, highScore).apply();
    }

    // Power-up methods
    public void activateShield(long duration) {
        hasShield = true;
        shieldEndTime = System.currentTimeMillis() + duration;
    }

    public void activateDoubleShot(long duration) {
        hasDoubleShot = true;
        hasTripleShot = false;
        doubleShotEndTime = System.currentTimeMillis() + duration;
    }

    public void activateTripleShot(long duration) {
        hasTripleShot = true;
        hasDoubleShot = false;
        tripleShotEndTime = System.currentTimeMillis() + duration;
    }

    public void activateSpeedBoost(long duration) {
        hasSpeedBoost = true;
        speedBoostEndTime = System.currentTimeMillis() + duration;
    }

    public void activateSlowMotion(long duration) {
        hasSlowMotion = true;
        slowMotionEndTime = System.currentTimeMillis() + duration;
    }

    public void updatePowerUps() {
        long currentTime = System.currentTimeMillis();

        if (hasShield && currentTime > shieldEndTime) {
            hasShield = false;
        }
        if (hasDoubleShot && currentTime > doubleShotEndTime) {
            hasDoubleShot = false;
        }
        if (hasTripleShot && currentTime > tripleShotEndTime) {
            hasTripleShot = false;
        }
        if (hasSpeedBoost && currentTime > speedBoostEndTime) {
            hasSpeedBoost = false;
        }
        if (hasSlowMotion && currentTime > slowMotionEndTime) {
            hasSlowMotion = false;
        }
    }

    public boolean hasShield() {
        return hasShield && System.currentTimeMillis() <= shieldEndTime;
    }

    public boolean hasDoubleShot() {
        return hasDoubleShot && System.currentTimeMillis() <= doubleShotEndTime;
    }

    public boolean hasTripleShot() {
        return hasTripleShot && System.currentTimeMillis() <= tripleShotEndTime;
    }

    public boolean hasSpeedBoost() {
        return hasSpeedBoost && System.currentTimeMillis() <= speedBoostEndTime;
    }

    public boolean hasSlowMotion() {
        return hasSlowMotion && System.currentTimeMillis() <= slowMotionEndTime;
    }

    public void useShield() {
        hasShield = false;
    }

    public void resetForNewGame() {
        currentScore = 0;
        playerHealth = 3;
        hasShield = false;
        hasDoubleShot = false;
        hasTripleShot = false;
        hasSpeedBoost = false;
        hasSlowMotion = false;
        isBossActive = false;
        nextBossScore = 500;
        applyLevelSettings();
    }

    public String getLevelName() {
        switch (selectedLevel) {
            case LEVEL_1:
                return "Nivel 1 - Facil";
            case LEVEL_2:
                return "Nivel 2 - Medio";
            case LEVEL_3:
                return "Nivel 3 - Dificil";
            case LEVEL_ARCADE:
            default:
                return "Arcade - Infinito";
        }
    }

    public String getMapName() {
        switch (selectedMap) {
            case MAP_SUNSET:
                return "Atardecer";
            case MAP_AURORA:
                return "Aurora Boreal";
            case MAP_SPACE:
            default:
                return "Espacio";
        }
    }
}