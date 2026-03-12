package com.example.navesgame;

import java.util.Random;

public class Enemigo {
    public int x, y, velocidad;
    private int screenX;

    public Enemigo(int screenX, int screenY) {
        this.screenX = screenX;
        Random random = new Random();
        // Aparece en una X aleatoria
        this.x = random.nextInt(screenX - 100);
        // Empieza justo arriba, fuera de la pantalla
        this.y = -100;
        // Velocidad inicial aleatoria
        this.velocidad = random.nextInt(10) + 5;
    }

    public void update() {
        // Cae hacia abajo
        y += velocidad;
    }
}