package com.example.navesgame;

import java.util.Random;

public class Enemigo {
    public int x, y, velocidad, tipo, color, hp, maxHp;
    public boolean isBoss = false;
    private int direction = 1;
    private Random random = new Random();

    public Enemigo(int screenX, int screenY, boolean isLandscape, GameState gameState) {
        this.tipo = random.nextInt(3);
        
        if (isLandscape) {
            this.x = screenX + 100;
            this.y = random.nextInt(Math.max(1, screenY - 100));
        } else {
            this.x = random.nextInt(Math.max(1, screenX - 100));
            this.y = -100;
        }

        this.velocidad = gameState.getEnemySpeed() + random.nextInt(4);
        this.hp = (tipo == 2) ? 3 : 1; 
        this.color = (tipo == 0) ? 0xFFFF4444 : (tipo == 1) ? 0xFFFF8800 : 0xFFAA00FF;
        this.maxHp = this.hp;
    }

    public Enemigo(int screenX, int screenY, boolean isBoss, boolean isLandscape, GameState gameState) {
        this.isBoss = true;
        if (isLandscape) {
            this.x = screenX + 300;
            this.y = screenY / 2 - 100;
        } else {
            this.x = screenX / 2 - 150;
            this.y = -300;
        }
        this.hp = 40;
        this.maxHp = 40;
        this.color = 0xFFFF0000;
        this.velocidad = 4;
    }

    public void updateOriented(int screenX, int screenY, boolean isLandscape) {
        if (!isBoss) {
            if (isLandscape) {
                x -= velocidad; // Vienen de DERECHA a IZQUIERDA
            } else {
                y += velocidad; // Vienen de ARRIBA a ABAJO
            }
        } else {
            // IA del Boss Adaptativa
            if (isLandscape) {
                if (x > screenX - 600) x -= velocidad; // Entra desde el lado derecho
                y += direction * 8; // Se mueve de arriba a abajo
                if (y < 50 || y > screenY - 250) direction *= -1;
            } else {
                if (y < 200) y += velocidad; // Entra hasta 200px
                x += direction * 8; // Se mueve de izquierda a derecha
                if (x < 50 || x > screenX - 350) direction *= -1;
            }
        }
    }
}