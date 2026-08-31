# Actividad 1.3: banco de ejercicios de análisis semántico

Esta actividad utiliza el mismo analizador semántico de la actividad 1.2. Las clases y los archivos anteriores se conservaron; únicamente se agregó `BancoEjercicios.java` y la carpeta `ejemplos/banco_ejercicios`.

## Ejercicios incluidos

1. Generación de la tabla de símbolos.
2. Evaluación y prioridad de expresiones aritméticas.
3. Comprobación de tipos `int` y `float`.
4. Concatenación de cadenas con otros tipos.
5. Detección de una variable duplicada.
6. Detección de una variable no declarada.
7. Detección de una asignación incompatible.
8. Detección de una división entre cero.
9. Identificación de varios errores semánticos.
10. Caso integrador semánticamente correcto.

## Cómo ejecutar en Visual Studio Code

1. Descomprimir el proyecto.
2. Abrir en Visual Studio Code la carpeta `AnalizadorSemantico`.
3. Esperar a que Java y Maven terminen de cargar el proyecto.
4. Abrir el archivo `BancoEjercicios.java`.
5. Presionar **Run Java** encima del método `main`.
6. Escribir un número del 1 al 10 en la terminal y presionar **Enter**.
7. Tomar una captura donde se vean el número del ejercicio, el código analizado, la tabla de símbolos y el resultado.

También se puede ejecutar un ejercicio específico desde la terminal de PowerShell. Primero se compila el proyecto:

```powershell
mvn compile
```

Después se ejecuta el ejercicio deseado, cambiando el número final del 1 al 10:

```powershell
mvn exec:java -Dexec.mainClass="mx.edu.tecnm.semantico.BancoEjercicios" -Dexec.args="1"
```

## Resultados esperados

| Ejercicio | Resultado principal |
|---|---|
| 1 | Cuatro variables registradas y análisis correcto. |
| 2 | `resultado` vale `20`. |
| 3 | `total` vale `62.0`. |
| 4 | Se muestran producto, precio y disponibilidad. |
| 5 | Un error por declaración duplicada. |
| 6 | Dos errores por usar `total` sin declararla. |
| 7 | Un error por asignar `String` a `int`; `edad` conserva el valor `20`. |
| 8 | Un error por división entre cero y otro porque `resultado` quedó sin valor. |
| 9 | Se detectan seis errores semánticos diferentes. |
| 10 | Subtotal de `25001.0` y análisis correcto. |

## Capturas para el reporte

Para documentar por completo el banco, se recomienda enviar una captura de cada ejercicio. Si se desea reducir la cantidad de imágenes en el PowerPoint, las evidencias principales serán los ejercicios 1, 2, 3, 5, 8, 9 y 10.

Antes de tomar cada captura, conviene maximizar la terminal y dejar visible desde el encabezado `Ejercicio` hasta el apartado `Resultado del análisis`.
