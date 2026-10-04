Proyecto final de **Lenguajes y Autómatas I + II** desarrollado en **Java 17 + JavaFX + Maven**.

Esta versión funciona como un mini IDE/compilador: el usuario escribe un programa en un lenguaje propio con palabras reservadas en español, presiona **Compilar** y el sistema ejecuta las fases de análisis en orden. Si alguna fase falla, las siguientes se detienen. Si todo es correcto, el programa se traduce a Java, se compila con el **javac real del JDK** y puede ejecutarse desde la misma interfaz.

## Qué demuestra

1. Análisis léxico: alfabeto, lexemas, tokens, palabras reservadas y símbolos inválidos.
2. Análisis sintáctico: estructura del programa y expresiones según una gramática definida.
3. Análisis semántico: tabla de símbolos, declaraciones, ámbitos, tipos y compatibilidad de expresiones.
4. Traducción: el AST válido se transforma a Java.
5. Compilación real: se usa `javax.tools.JavaCompiler` (javac).
6. Ejecución: el `.class` generado se ejecuta y la salida aparece dentro de JavaFX.

## Requisitos

- JDK 17 o superior. Debe ser JDK completo porque se utiliza `javac`.
- Apache Maven.
- En el equipo de desarrollo actual Maven está en:
  `C:\apache-maven-3.9.16\bin\mvn.cmd`
