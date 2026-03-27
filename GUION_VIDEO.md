# GUION - Video de Presentacion: Naves Game
## Juego de Naves Espacial 2D Retro para Android

**Duracion estimada:** 8-12 minutos
**Estructura:** Demo del juego -> Base de datos -> Codigo destacado

---

## PARTE 1: INTRODUCCION (30 segundos)

**Di:** "Hola, soy [tu nombre] y voy a presentar mi proyecto Naves Game, un juego de naves espacial 2D retro desarrollado en Android con Java. El juego tiene sistema de niveles, jefes finales, power-ups, efectos de particulas y un scoreboard online con Firebase."

**Muestra:** La pantalla principal de la app (MainActivity) con el campo de nickname y los botones.

---

## PARTE 2: DEMO DEL JUEGO (3-4 minutos)

### 2.1 - Menu principal
**Di:** "Al abrir la app, vemos la pantalla principal donde el jugador introduce su nickname. Este nombre se guarda en SharedPreferences para que no tenga que escribirlo cada vez."

**Muestra:** Escribe un nickname y pulsa PLAY.

### 2.2 - Menu de seleccion en el juego
**Di:** "Dentro del juego tenemos un menu donde podemos elegir entre 3 mapas diferentes: Espacio, Atardecer y Aurora, cada uno con su propio fondo visual. Tambien podemos seleccionar la dificultad: Facil, Normal o Dificil."

**Muestra:** El menu in-game con los botones de mapa y dificultad.

### 2.3 - Gameplay basico
**Di:** "El jugador controla la nave arrastrando el dedo por la pantalla. Los disparos son automaticos mientras mantienes pulsado. La nave tambien se puede controlar con teclado usando WASD y Espacio para disparar."

**Muestra:** Juega una partida, mueve la nave y dispara a enemigos normales. Muestra como sube el score.

### 2.4 - Tipos de enemigos
**Di:** "Hay 4 tipos de enemigos: los normales en rojo, los rapidos en naranja que van a mayor velocidad, los tanque en morado que aguantan 3 disparos, y los zigzag en verde que se mueven con un patron sinusoidal."

**Muestra:** Intenta que aparezcan los distintos tipos de enemigos. Dispara a un tanque para que se vea que aguanta varios golpes.

### 2.5 - Power-ups
**Di:** "A lo largo de la partida aparecen power-ups aleatorios: el escudo que absorbe un golpe, disparo doble, disparo triple, boost de velocidad, camara lenta que reduce la velocidad de spawn de enemigos, bomba que elimina todos los enemigos en pantalla, y vida extra."

**Muestra:** Recoge algun power-up durante el gameplay. Idealmente el escudo (S azul) para que se vea el circulo cyan alrededor de la nave.

### 2.6 - Sistema de combos
**Di:** "Cada vez que eliminamos un enemigo sin pausa, se incrementa el combo. Cada nivel de combo da 4 puntos extra por kill. Si no eliminamos a nadie en 1.5 segundos, el combo se resetea."

**Muestra:** Elimina varios enemigos seguidos para que se vea el indicador "COMBO x3, x4..."

### 2.7 - Boss
**Di:** "Cada 1500 puntos aparece un jefe con patrones de ataque unicos. El boss rojo dispara en circulos, el purpura tiene disparos que persiguen al jugador, el dorado bombardea en 5 direcciones, y el jefe final lanza espirales infinitas. Cada boss tiene una barra de vida visible."

**Muestra:** Si llegas a un boss, ensenalo. Si no, puedes grabar esto por separado.

### 2.8 - Game Over y Scoreboard
**Di:** "Al morir, la puntuacion se envia automaticamente a Firebase. Desde el menu principal podemos ver el scoreboard global con el top 10 de puntuaciones y los mejores tiempos de boss run."

**Muestra:** Muerete, vuelve al menu principal y abre el scoreboard.

---

## PARTE 3: BASE DE DATOS - Firebase (2 minutos)

### 3.1 - Estructura de la base de datos
**Di:** "Para el backend utilizo Firebase Realtime Database. La estructura tiene dos colecciones principales: 'scores' para las puntuaciones generales y 'boss_runs' para los tiempos de speedrun del jefe final."

**Muestra:** Abre la consola de Firebase en el navegador y ensenala estructura:
```
navesgame-default-rtdb/
  scores/
    [auto-id]/
      name: "jugador1"
      points: 2350
      timestamp: 1711545600000
  boss_runs/
    [auto-id]/
      name: "jugador1"
      time: 45000       (milisegundos)
      points: 3200
      timestamp: 1711545600000
```

### 3.2 - Codigo de conexion
**Di:** "La clase CloudScoreboard gestiona toda la comunicacion con Firebase."

**Muestra:** Fichero `CloudScoreboard.java`

**Di:** "En la linea 19 definimos la URL de la base de datos y obtenemos la referencia. En la linea 23, el metodo postHighScore recibe el nombre del jugador y los puntos, crea un objeto ScoreEntry y lo sube con push() que genera un ID unico automaticamente."

**Muestra:** Lineas 18-33 de `CloudScoreboard.java`

**Di:** "Para obtener el top 10, en la linea 47 usamos una Query con orderByChild para ordenar por puntos y limitToLast para coger solo los 10 mejores. Firebase nos devuelve los datos de forma asincrona a traves de un ValueEventListener."

**Muestra:** Lineas 47-70 de `CloudScoreboard.java`

---

## PARTE 4: CODIGO DESTACADO (3-4 minutos)

### 4.1 - Game Loop (El corazon del juego)
**Di:** "Lo mas importante en cualquier juego es el game loop. En la linea 124 de GameView tenemos el metodo run que se ejecuta en un hilo separado. Usamos System.nanoTime para controlar los FPS con precision de nanosegundos. La constante TIME_PER_FRAME esta calculada para 60 FPS, es decir, 16.67 milisegundos por frame. En cada frame llamamos a update() para la logica y drawFrame() para el renderizado. Cuando no toca frame, hacemos Thread.sleep(1) para no quemar CPU innecesariamente."

**Muestra:** Fichero `GameView.java`, lineas 124-136

### 4.2 - Sistema de sprites con pixel art
**Di:** "En lugar de usar imagenes externas, los sprites estan definidos como arrays 2D de enteros. En la linea 67 vemos SPRITE_PLAYER: un array donde 0 es transparente, 1 es el color principal y 2 es el secundario. El metodo drawPixelSprite en la linea 492 recorre este array y pinta cada celda como un rectangulo en el Canvas."

**Muestra:** Primero la linea 67 de `GameView.java` (definicion del sprite), luego las lineas 492-497 (metodo de renderizado)

### 4.3 - Deteccion de colisiones AABB
**Di:** "Para las colisiones usamos el algoritmo AABB, Axis-Aligned Bounding Box. En la linea 623 se ve el metodo checkCollision que recibe posicion y tamano de dos rectangulos y comprueba si se solapan. Es simple pero eficiente para un juego 2D. Lo usamos en la linea 217 para colision bala-jugador, en la 290 para enemigo-jugador, y en la 302 para bala-enemigo."

**Muestra:** Linea 623-625 de `GameView.java`, luego brevemente las lineas 217 y 290 donde se usa

### 4.4 - Iteracion inversa segura
**Di:** "Un patron importante que usamos en todo el juego es la iteracion inversa. En la linea 208 recorremos la lista de balas desde el final hasta el inicio. Esto es necesario porque si eliminamos un elemento mientras recorremos la lista hacia adelante, los indices se desplazan y podemos saltarnos elementos o causar errores. Al recorrer hacia atras, las eliminaciones no afectan a los indices que aun no hemos visitado."

**Muestra:** Lineas 208-212 de `GameView.java`

### 4.5 - Patrones de ataque del Boss
**Di:** "Los jefes tienen patrones de ataque avanzados usando trigonometria. Por ejemplo, en la linea 238 el Boss Rojo cada 4 segundos dispara en circulo: recorre angulos de 0 a 360 grados en pasos de 45, convierte a radianes y usa coseno y seno para calcular la direccion de cada bala. En la linea 271, el Boss Final crea una espiral infinita con dos brazos opuestos que rotan constantemente."

**Muestra:** Lineas 238-250 de `GameView.java` (Boss Rojo), luego lineas 271-278 (Boss Final)

### 4.6 - Escalado de dificultad progresivo
**Di:** "La dificultad escala de forma dinamica. En GameState, linea 125, el metodo getEnemySpeed calcula la velocidad base segun la dificultad elegida, le suma un factor por jefes eliminados, y otro por puntuacion acumulada. Asi el juego se vuelve progresivamente mas dificil de forma natural sin saltos bruscos."

**Muestra:** Fichero `GameState.java`, lineas 125-147

---

## PARTE 5: CIERRE (30 segundos)

**Di:** "En resumen, Naves Game es un juego retro completo con game loop a 60 FPS, 4 tipos de enemigos, 4 jefes con patrones unicos, 7 power-ups, sistema de combos, 3 mapas visuales, escalado de dificultad progresivo y un scoreboard online con Firebase. Gracias por ver la presentacion."

**Muestra:** Una ultima jugada rapida o la pantalla del scoreboard con puntuaciones.

---

## RESUMEN RAPIDO DE FICHEROS A MOSTRAR

| Orden | Fichero | Lineas | Que se ensena |
|-------|---------|--------|---------------|
| 1 | `CloudScoreboard.java` | 18-33 | Conexion Firebase y postHighScore |
| 2 | `CloudScoreboard.java` | 47-70 | Query top 10 scores |
| 3 | `GameView.java` | 124-136 | Game Loop 60 FPS |
| 4 | `GameView.java` | 67, 492-497 | Sistema de sprites pixel art |
| 5 | `GameView.java` | 623-625 | Colisiones AABB |
| 6 | `GameView.java` | 208-212 | Iteracion inversa segura |
| 7 | `GameView.java` | 238-250, 271-278 | Patrones de ataque Boss |
| 8 | `GameState.java` | 125-147 | Escalado de dificultad |

---

## CONSEJOS PARA LA GRABACION

- Graba el gameplay primero por separado, asi puedes elegir los mejores momentos
- Usa un emulador de Android o graba directamente desde el movil con screen recording
- Para mostrar el codigo, usa Android Studio con el tema oscuro y haz zoom para que se lea bien
- Cuando muestres Firebase, ten la consola abierta y haz una partida para que se vea como aparecen los datos en tiempo real
- Habla despacio y con calma al explicar el codigo, da tiempo a que se lea en pantalla
