package com.example.navesgame;

public class GameState {
    public static final int STATE_MENU = 0;
    public static final int STATE_PLAYING = 4;
    public static final int STATE_GAME_OVER = 5;

    public static final int MAP_SPACE = 0;
    public static final int MAP_SUNSET = 1;
    public static final int MAP_AURORA = 2;
    public static final int MAP_COUNT = 3;

    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_NORMAL = 1;
    public static final int DIFFICULTY_HARD = 2;

    private int currentState = STATE_MENU;
    private int selectedMap = MAP_SPACE;
    private int difficulty = DIFFICULTY_NORMAL;
    private int currentScore = 0;
    private int highScore = 0;
    private int playerHealth = 3;
    private final int maxHealth = 3;
    private int bossesKilled = 0;
    private boolean manualMapSelection = false;
    private String playerName = "Player";
    private boolean infiniteMode = false;
    private long lastBossSpawnTime = 0;

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

    public void setState(int state) { this.currentState = state; }
    public int getState() { return currentState; }
    public void setMap(int map) { this.selectedMap = map; this.manualMapSelection = true; }
    public int getMap() { return selectedMap; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
    public int getDifficulty() { return difficulty; }

    public int getScore() { return currentScore; }
    public void addScore(int points) { 
        this.currentScore += points; 
        updateProgression();
    }
    
    private void updateProgression() {
        if (!manualMapSelection) {
            if (currentScore >= 2000) selectedMap = MAP_AURORA;
            else if (currentScore >= 1000) selectedMap = MAP_SUNSET;
            else selectedMap = MAP_SPACE;
        }
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

    private int lastBossScore = 0;

    public boolean isBossActive() { return bossActive; }
    public void setBossActive(boolean active) { this.bossActive = active; }
    public int getLastBossScore() { return lastBossScore; }
    public void setLastBossScore(int score) { this.lastBossScore = score; }
    public int getBossesKilled() { return bossesKilled; }
    public boolean isManualMapSelection() { return manualMapSelection; }

    public void resetForNewGame() {
        currentScore = 0;
        playerHealth = maxHealth;
        bossesKilled = 0;
        infiniteMode = false;
        if (!manualMapSelection) selectedMap = MAP_SPACE;
        hasShield = hasDoubleShot = hasTripleShot = hasSpeedBoost = hasSlowMotion = false;
        bossActive = false;
        lastBossScore = 0;
    }

    public void setPlayerName(String name) { this.playerName = name; }
    public String getPlayerName() { return playerName; }
    public void setInfiniteMode(boolean active) { this.infiniteMode = active; }
    public boolean isInfiniteMode() { return infiniteMode; }
    public void setLastBossSpawnTime(long time) { this.lastBossSpawnTime = time; }
    public long getLastBossSpawnTime() { return lastBossSpawnTime; }

    public int getPlayerHealth() { return playerHealth; }
    public void addHealth(int amount) { playerHealth = Math.min(maxHealth, playerHealth + amount); }
    public void takeDamage() { playerHealth--; }
    public boolean isPlayerDead() { return playerHealth <= 0; }

    public int getEnemySpeed() { 
        int baseSpeed = 4;
        if (difficulty == DIFFICULTY_EASY) baseSpeed = 3;
        if (difficulty == DIFFICULTY_HARD) baseSpeed = 6;
        int scale = (infiniteMode) ? (bossesKilled * 3) : (bossesKilled * 2);
        return baseSpeed + scale + (currentScore / 5000);
    }
    
    public long getEnemySpawnDelay() { 
        long baseDelay = 1800;
        if (difficulty == DIFFICULTY_EASY) baseDelay = 2200;
        if (difficulty == DIFFICULTY_HARD) baseDelay = 1200;
        long reduction = (infiniteMode) ? (bossesKilled * 200) : (bossesKilled * 150);
        return Math.max(300, baseDelay - reduction - (currentScore / 20)); 
    }

    public float getSpawnChance(float timeScale) {
        float baseChance = 4 + (bossesKilled * 0.5f) + (currentScore / 2000f);
        if (difficulty == DIFFICULTY_EASY) baseChance *= 0.7f;
        if (difficulty == DIFFICULTY_HARD) baseChance *= 1.4f;
        if (infiniteMode) baseChance += 2.0f;
        return baseChance * timeScale;
    }

    public int getLevel() { return 1 + bossesKilled; }
    public void incrementBossesKilled() { this.bossesKilled++; }
}