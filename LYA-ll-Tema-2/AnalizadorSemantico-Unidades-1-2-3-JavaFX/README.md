# Analizador semántico · Unidades 1, 2 y 3

Proyecto JavaFX para **Lenguajes y Autómatas II**. Conserva el analizador semántico de la Unidad 1 y lo amplía para las prácticas de **generación de código intermedio (Unidad 2)** y **optimización (Unidad 3)**.

## Requisitos

- JDK 17.
- Maven 3.8 o posterior.
- Visual Studio Code con Extension Pack for Java, IntelliJ IDEA o un IDE compatible con Maven/JavaFX.

## Ejecutar la interfaz

```bash
mvn clean javafx:run
```

## Qué conserva de Unidad 1

- Tabla de símbolos.
- Tipos `int`, `float`, `String` y `boolean`.
- Declaraciones y asignaciones.
- Evaluación de expresiones con `+`, `-`, `*`, `/` y paréntesis.
- Detección de variable duplicada/no declarada, incompatibilidad de tipos y división entre cero.
- Instrucción `mostrar(...)`.
- Banco original de ejercicios.

## Qué agrega para Unidad 2

Para cada declaración con inicialización o asignación válida se muestran:

- Notación infija.
- Notación prefija.
- Notación postfija.
- Notación polaca (prefija).
- Código P basado en pila (`LIT`, `LOD`, `ADD`, `SUB`, `MUL`, `DIV`, `NEG`, `STO`).
- Código de tres direcciones con temporales `t1`, `t2`, etc.
- Triplos.
- Cuádruplos.

La precedencia y los paréntesis se obtienen del árbol de expresión, por lo que una entrada como `x = a + b * c` genera primero la multiplicación y después la suma.

## Qué agrega para Unidad 3

La pestaña **U3 · Optimización** compara el código intermedio original contra el optimizado y documenta las reglas aplicadas:

- Plegado de constantes.
- Simplificación algebraica (`x + 0`, `x * 1`, etc.).
- Propagación de constantes y copias.
- Eliminación de temporales sin uso.
- Eliminación de asignaciones redundantes (mirilla).
- Detección/reutilización de subexpresiones comunes.
- Reducción de fuerza para multiplicación por 2.

Además muestra cantidad de instrucciones antes/después y reducción neta.

## Ejemplos preparados

- `ejemplos/unidad_2/actividad_2_2/`: 5 pruebas recomendadas para el reporte de práctica.
- `ejemplos/unidad_2/actividad_2_3/`: 10 ejercicios de código intermedio.
- `ejemplos/unidad_3/actividad_3_2/`: 5 pruebas recomendadas para el reporte de optimización.
- `ejemplos/unidad_3/actividad_3_3/`: 10 ejercicios de optimización.

Todos aparecen también en el selector lateral de la interfaz.

## Validar los ejercicios sin abrir JavaFX

Después de compilar el proyecto se puede ejecutar `ValidadorUnidades23`, que recorre los 30 archivos de Unidades 2 y 3 y comprueba que no tengan errores semánticos y que produzcan código intermedio.
