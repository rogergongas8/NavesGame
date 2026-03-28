package com.example.navesgame;

import java.util.Random;

public class Enemigo {
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_FAST = 1;
    public static final int TYPE_TANK = 2;
    public static final int TYPE_ZIGZAG = 3;

    public static final int[][] SPRITE_NORMAL = {{0,0,1,1,1,0,0},{0,1,1,1,1,1,0},{1,1,2,2,2,1,1},{1,1,2,1,2,1,1},{1,1,1,1,1,1,1},{0,1,0,1,0,1,0},{0,0,1,0,1,0,0}};
    public static final int[][] SPRITE_FAST = {{0,0,0,1,0,0,0},{0,0,1,1,1,0,0},{0,1,1,1,1,1,0},{1,1,2,1,2,1,1},{0,1,1,1,1,1,0},{0,0,1,1,1,0,0},{0,0,0,1,0,0,0}};
    public static final int[][] SPRITE_TANK = {{1,1,1,1,1,1,1},{1,2,2,2,2,2,1},{1,2,1,1,1,2,1},{1,2,1,2,1,2,1},{1,2,1,1,1,2,1},{1,2,2,2,2,2,1},{1,1,1,1,1,1,1}};
    public static final int[][] SPRITE_ZIGZAG = {{0,1,1,0,1,1,0},{1,1,1,1,1,1,1},{1,2,1,2,1,2,1},{0,1,1,1,1,1,0},{0,0,1,1,1,0,0},{0,1,1,1,1,1,0},{1,1,0,1,0,1,1}};
    public static final int[][] SPRITE_FINAL_BOSS = {
        {0,0,0,1,1,1,1,1,0,0,0},
        {0,0,1,2,2,2,2,2,1,0,0},
        {0,1,2,1,1,2,1,1,2,1,0},
        {1,2,2,2,2,1,2,2,2,2,1},
        {1,2,1,2,2,1,2,2,1,2,1},
        {1,1,1,1,1,1,1,1,1,1,1},
        {1,2,2,2,2,2,2,2,2,2,1},
        {0,1,1,0,1,1,1,0,1,1,0},
        {0,0,1,0,0,1,0,0,1,0,0}
    };

    public float x, y;
    public int velocidad, tipo, color, hp, maxHp;
    public boolean isBoss = false;
    public int bossType = 0;
    public long bossTime = 0;
    private int direction = 1;
    private float sinTime = 0;
    private Random random = new Random();

    public Enemigo(int screenX, int screenY, boolean isLandscape, GameState gameState) {
        this.tipo = random.nextInt(4);
        
        if (isLandscape) {
            this.x = -100;
            this.y = random.nextInt(Math.max(1, screenY - 100));
        } else {
            this.x = random.nextInt(Math.max(1, screenX - 100));
            this.y = -100;
        }

        this.velocidad = (tipo == TYPE_FAST) ? gameState.getEnemySpeed() + 5 : gameState.getEnemySpeed() + random.nextInt(4);
        this.hp = (tipo == TYPE_TANK) ? 3 : 1; 
        
        if (tipo == TYPE_NORMAL) this.color = 0xFFFF4444;
        else if (tipo == TYPE_FAST) this.color = 0xFFFF8800;
        else if (tipo == TYPE_TANK) this.color = 0xFFAA00FF;
        else if (tipo == TYPE_ZIGZAG) this.color = 0xFF44FF44; // Verde para zigzag
        
        this.maxHp = this.hp;
        this.sinTime = random.nextFloat() * 10;
    }

    public Enemigo(int screenX, int screenY, boolean isBoss, boolean isLandscape, GameState gameState) {
        this.isBoss = true;
        this.bossType = (gameState.getBossesKilled() % 4);
        
        if (isLandscape) {
            this.x = -400;
            this.y = screenY / 2 - 150;
        } else {
            this.x = screenX / 2 - 200;
            this.y = -400;
        }

        // Stats por Tipo de Jefe
        switch (bossType) {
            case 1: // Boss Púrpura (Rápido)
                this.hp = 35; this.color = 0xFFAA00FF; this.velocidad = 6; break;
            case 2: // Boss Dorado (Tanque)
                this.hp = 65; this.color = 0xFFFFD700; this.velocidad = 3; break;
            case 3: // MEGA BOSS FINAL (Estático)
                this.hp = 150; this.color = 0xFF00FF00; this.velocidad = 2; break;
            default: // Boss Rojo (Equilibrado)
                this.hp = 45; this.color = 0xFFFF0000; this.velocidad = 4; break;
        }
        this.maxHp = this.hp;
    }

    public void updateOriented(int screenX, int screenY, boolean isLandscape, float deltaTime) {
        float speedScale = velocidad * deltaTime * 60f; // Escalar velocidad base a segundos
        if (!isBoss) {
            if (isLandscape) {
                x += speedScale;
                if (tipo == TYPE_ZIGZAG) {
                    sinTime += 9f * deltaTime;
                    y += (float)(Math.sin(sinTime) * 12);
                }
            } else {
                y += speedScale;
                if (tipo == TYPE_ZIGZAG) {
                    sinTime += 9f * deltaTime;
                    x += (float)(Math.sin(sinTime) * 12);
                }
            }
        } else {
            // IA del Boss Adaptativa
            float bossSpeed = speedScale;
            if (isLandscape) {
                if (bossType == 3) {
                    if (x < 200) x += bossSpeed; // Boss final se queda al principio
                } else {
                    if (x < 300) x += bossSpeed;
                    y += direction * 8 * deltaTime * 60f;
                    if (y < 50 || y > screenY - 250) direction *= -1;
                }
            } else {
                if (bossType == 3) {
                    if (y < 150) y += bossSpeed; // Boss final se queda arriba
                } else {
                    if (y < 200) y += bossSpeed;
                    x += direction * 8 * deltaTime * 60f;
                    if (x < 50 || x > screenX - 350) direction *= -1;
                }
            }
        }
    }
}