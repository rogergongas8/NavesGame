package com.example.navesgame;

public class GameState {
    public static final int STATE_MENU = 0;
    public static final int STATE_PLAYING = 4;
    public static final int STATE_GAME_OVER = 5;

    public static final int MAP_SPACE = 0;
    public static final int MAP_SUNSET = 1;
    public static final int MAP_AURORA = 2;

    public static final int LEVEL_ARCADE = 0;

    private int currentState = STATE_MENU;
    private int selectedMap = MAP_SPACE;
    private int currentScore = 0;
    private int highScore = 0;

    // Power-ups
    private boolean hasShield = false;
    private boolean hasDoubleShot = false;
    private boolean hasTripleShot = false;
    private boolean hasSpeedBoost = false;
    private boolean hasSlowMotion = false;

    private long shieldEndTime = 0;
    private long doubleShotEndTime = 0;
    private long tripleShotEndTime = 0;
    private long speedBoostEndTime = 0;
    private long slowMotionEndTime = 0;

    // Boss status
    private boolean bossActive = false;
    private int lastBossScore = 0;

    public void setState(int state) { this.currentState = state; }
    public int getState() { return currentState; }
    public void setMap(int map) { this.selectedMap = map; }
    public int getMap() { return selectedMap; }
    public int getLevel() { return LEVEL_ARCADE; }

    public int getScore() { return currentScore; }
    public void addScore(int points) { 
        this.currentScore += points; 
        updateProgression();
    }
    
    private void updateProgression() {
        // Cambio automático de mapa según puntuación
        if (currentScore >= 2000) setMap(MAP_AURORA);
        else if (currentScore >= 1000) setMap(MAP_SUNSET);
        else setMap(MAP_SPACE);
    }

    public void setHighScore(int score) { this.highScore = score; }
    public int getHighScore() { return highScore; }
    public void updateHighScore() { if (currentScore > highScore) highScore = currentScore; }

    public void activateShield(long duration) { hasShield = true; shieldEndTime = System.currentTimeMillis() + duration; }
    public void activateDoubleShot(long duration) { hasDoubleShot = true; hasTripleShot = false; doubleShotEndTime = System.currentTimeMillis() + duration; }
    public void activateTripleShot(long duration) { hasTripleShot = true; hasDoubleShot = false; tripleShotEndTime = System.currentTimeMillis() + duration; }
    public void activateSpeedBoost(long duration) { hasSpeedBoost = true; speedBoostEndTime = System.currentTimeMillis() + duration; }
    public void activateSlowMotion(long duration) { hasSlowMotion = true; slowMotionEndTime = System.currentTimeMillis() + duration; }

    public void updatePowerUps() {
        long now = System.currentTimeMillis();
        if (hasShield && now > shieldEndTime) hasShield = false;
        if (hasDoubleShot && now > doubleShotEndTime) hasDoubleShot = false;
        if (hasTripleShot && now > tripleShotEndTime) hasTripleShot = false;
        if (hasSpeedBoost && now > speedBoostEndTime) hasSpeedBoost = false;
        if (hasSlowMotion && now > slowMotionEndTime) hasSlowMotion = false;
    }

    public boolean hasShield() { return hasShield; }
    public boolean hasDoubleShot() { return hasDoubleShot; }
    public boolean hasTripleShot() { return hasTripleShot; }
    public boolean hasSpeedBoost() { return hasSpeedBoost; }
    public boolean hasSlowMotion() { return hasSlowMotion; }
    public void useShield() { hasShield = false; }

    public boolean isBossActive() { return bossActive; }
    public void setBossActive(boolean active) { this.bossActive = active; }
    public int getLastBossScore() { return lastBossScore; }
    public void setLastBossScore(int score) { this.lastBossScore = score; }

    public int getEnemySpeed() { 
        return 7 + (currentScore / 2000); 
    }
    
    public long getEnemySpawnDelay() { 
        return Math.max(500, 1500 - (currentScore / 10)); 
    }

    public float getSpawnChance(float timeScale) {
        float baseChance = 5 + (currentScore / 1000f);
        return baseChance * timeScale;
    }

    public void resetForNewGame() {
        currentScore = 0;
        setMap(MAP_SPACE);
        hasShield = hasDoubleShot = hasTripleShot = hasSpeedBoost = hasSlowMotion = false;
        bossActive = false;
        lastBossScore = 0;
    }
}