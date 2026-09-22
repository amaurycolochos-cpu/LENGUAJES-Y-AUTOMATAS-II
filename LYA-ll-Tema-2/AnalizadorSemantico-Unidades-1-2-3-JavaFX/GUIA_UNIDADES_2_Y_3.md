# Guía de trabajo · Unidades 2 y 3

Esta guía organiza el proyecto para que puedas hacer las pruebas y tomar capturas sin volver a modificar el código.

> Nota: los nombres **Actividad 2.2 / 2.3 y 3.2 / 3.3** se usan aquí como organización del trabajo que ya veníamos manejando. La instrumentación enumera las evidencias como mapa, reporte de práctica y ejercicios, pero no les asigna esos números de actividad.

## Unidad 2 · Generación de código intermedio

### Evidencias a preparar

1. Mapa conceptual sobre generación de código intermedio.
2. Reporte de práctica codificado en Java.
3. Ejercicios de generación de código intermedio.

### Para el reporte que estamos llamando Actividad 2.2

Usa primero estas cinco pruebas del selector:

1. **Operación simple**: comprueba una suma y una asignación.
2. **Precedencia de operadores**: demuestra que `*` se genera antes que `+`.
3. **Uso de paréntesis**: demuestra que `(a + b)` cambia el orden.
4. **Asociatividad**: demuestra `a - b - c` de izquierda a derecha.
5. **Caso integrador**: combina varias expresiones y tipos numéricos.

### Capturas recomendadas para 2.2

- Captura 1: código fuente de la prueba de precedencia.
- Captura 2: pestaña **U2 · Notaciones**.
- Captura 3: pestaña **U2 · Tres direcciones**.
- Captura 4: pestaña **U2 · Cuádruplos**.
- Captura 5: pestaña **U2 · Triplos**.
- Captura 6: pestaña **U2 · Código P**.
- Captura 7: tabla de símbolos y estado **Análisis correcto**.

Con la prueba `a + b * c`, el punto clave que debes explicar es que primero aparece un temporal para `b * c`, después otro para `a + temporal` y finalmente la asignación al destino.

### Banco de ejercicios de Unidad 2

La carpeta `ejemplos/unidad_2/actividad_2_3/` contiene 10 ejercicios ya listos: notaciones, polaca, Código P, triplos, cuádruplos, temporales, asignaciones consecutivas, `float`, signo negativo e integrador.

## Unidad 3 · Optimización

### Evidencias a preparar

1. Mapa mental sobre optimización.
2. Reporte de práctica de optimización.
3. Ejercicios de optimización.

### Para el reporte que estamos llamando Actividad 3.2

Ejecuta estas cinco pruebas:

1. **Plegado de constantes**.
2. **Simplificación algebraica**.
3. **Propagación de constantes y copias**.
4. **Reducción de fuerza**.
5. **Caso integrador**.

### Capturas recomendadas para 3.2

- Captura 1: código fuente de una prueba de optimización.
- Captura 2: **U2 · Tres direcciones**, para documentar el código original.
- Captura 3: **U3 · Optimización**, mostrando código original y optimizado.
- Captura 4: parte **Cambios aplicados**.
- Captura 5: parte **Resumen**, mostrando instrucciones antes y después.
- Captura 6: otro caso, por ejemplo simplificación algebraica o reducción de fuerza.

### Banco de ejercicios de Unidad 3

La carpeta `ejemplos/unidad_3/actividad_3_3/` contiene 10 ejercicios: optimización local, identidades algebraicas, constantes, copias, subexpresiones comunes, reducción de fuerza, redundancias, temporales, mirilla e integrador.

## Flujo que conviene seguir en cada captura

1. Selecciona el ejemplo en el panel izquierdo.
2. Presiona **Analizar código**.
3. Comprueba que arriba diga **Análisis correcto**.
4. Abre la pestaña que quieras documentar.
5. Toma la captura completa procurando que se vea el código fuente y el resultado.
6. En el reporte, debajo de la captura explica qué entrada usaste, qué produjo el analizador y por qué el resultado es correcto.

## Archivos importantes del código

- `AnalizadorSemantico.java`: conserva el análisis semántico e integra las nuevas etapas.
- `ParserExpresionesIntermedias.java`: construye el árbol respetando precedencia y paréntesis.
- `GeneradorCodigoIntermedio.java`: produce las representaciones de Unidad 2.
- `Cuadruplo.java` y `Triplo.java`: modelos de representaciones intermedias.
- `OptimizadorCodigoIntermedio.java`: aplica las reglas de Unidad 3.
- `FormateadorResultados.java`: prepara las salidas que se ven en las pestañas.
- `AnalizadorController.java`: conecta los ejemplos y resultados con JavaFX.
- `ValidadorUnidades23.java`: valida en lote los ejemplos preparados.
