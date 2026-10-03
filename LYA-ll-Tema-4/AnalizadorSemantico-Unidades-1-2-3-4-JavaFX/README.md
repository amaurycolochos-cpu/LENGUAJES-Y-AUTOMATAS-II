# Analizador semántico · Unidades 1, 2, 3 y 4

Proyecto JavaFX para **Lenguajes y Autómatas II**. Conserva el analizador semántico de la Unidad 1 y lo amplía de forma acumulativa con **generación de código intermedio (Unidad 2)**, **optimización (Unidad 3)** y **generación de código objeto (Unidad 4)**.

## Requisitos

- JDK 17.
- Maven 3.8 o posterior.
- Visual Studio Code con Extension Pack for Java, IntelliJ IDEA o un IDE compatible con Maven/JavaFX.

## Ejecutar la interfaz

```bash
mvn clean javafx:run
```
o & "C:\apache-maven-3.9.16\bin\mvn.cmd" clean javafx:run
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

## Qué agrega para Unidad 4

La Unidad 4 toma el **código intermedio optimizado** y agrega la etapa final del compilador:

- Administración de memoria para variables, temporales y constantes.
- Uso de registros `R0` y `R1`.
- Selección de instrucciones de bajo nivel.
- Generación de ensamblador didáctico.
- Generación de palabras de código objeto de 32 bits.
- Visualización hexadecimal y binaria de cada instrucción.

Las pestañas nuevas mantienen el mismo diseño existente:

- `U4 · Memoria`
- `U4 · Ensamblador`
- `U4 · Código objeto`

## Ejemplos preparados

- `ejemplos/unidad_2/actividad_2_2/`: 5 pruebas recomendadas para el reporte de práctica.
- `ejemplos/unidad_2/actividad_2_3/`: 10 ejercicios de código intermedio.
- `ejemplos/unidad_3/actividad_3_2/`: 5 pruebas recomendadas para el reporte de optimización.
- `ejemplos/unidad_3/actividad_3_3/`: 10 ejercicios de optimización.
- `ejemplos/unidad_4/proyecto/`: 4 pruebas del proyecto final de generación de código objeto.

Todos aparecen también en el selector lateral de la interfaz.

## Validar los ejercicios sin abrir JavaFX

Después de compilar el proyecto se puede ejecutar `ValidadorUnidades23`, que recorre los 30 archivos de Unidades 2 y 3 y comprueba que no tengan errores semánticos y que produzcan código intermedio.

## Validar Unidad 4 sin abrir JavaFX

Después de compilar, puede ejecutarse `ValidadorUnidad4`, que comprueba los cuatro casos preparados y verifica que produzcan código objeto sin errores semánticos.
