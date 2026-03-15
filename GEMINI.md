# Naves Game - Android Arcade Shooter

## Project Overview

"Naves Game" is an Android arcade-style 2D shooter where players control a spaceship to destroy various types of enemies, collect power-ups, and achieve a high score. The game features:

*   **Diverse Enemies:** Multiple enemy types, including normal, fast, zigzag, and boss enemies, each with unique behaviors.
*   **Power-Ups:** A variety of power-ups such as shields, double/triple shot, speed boost, slow motion, health, and bombs, enhancing gameplay dynamically.
*   **Game States:** Manages different stages of the game, including a main menu, map selection, difficulty level selection, high scores display, active gameplay, and game over screen.
*   **Custom Rendering:** Utilizes Android's `SurfaceView` for direct rendering, allowing for custom graphics and animations like particle effects and dynamic backgrounds.
*   **Score Persistence:** High scores are saved locally using `SharedPreferences`.
*   **Technology Stack:** Developed primarily in Java for game logic, with Gradle Kotlin DSL for build management.

The game's entry point is `MainActivity`, which immediately transitions to `GameActivity` to host the `GameView`, where the core game loop, rendering, and interaction logic reside.

## Building and Running

This project is a standard Android Gradle project.

*   **Build the project:**
    ```bash
    ./gradlew build
    ```
*   **Install and run on a connected device or emulator:**
    ```bash
    ./gradlew installDebug
    ```
    Alternatively, the project can be opened and run directly from Android Studio.

## Development Conventions

*   **Language:** Core game logic is implemented in Java.
*   **Build System:** Uses Gradle with Kotlin DSL for build scripts (`.gradle.kts` files).
*   **UI/Rendering:** Custom game rendering is handled using Android's `SurfaceView` to draw game elements directly onto a canvas.
*   **Data Persistence:** `SharedPreferences` are used for lightweight data storage, specifically for saving the high score.
*   **Game Structure:** Game entities (Enemies, Power-Ups, Particles) are structured as separate classes, and a `GameState` class manages the overall state and game settings.
