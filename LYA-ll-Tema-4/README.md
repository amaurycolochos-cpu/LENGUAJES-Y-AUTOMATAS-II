# Tema 4 · Lenguajes y Autómatas II

Material del **Tema 4** de la materia Lenguajes y Autómatas II, correspondiente a las prácticas de análisis semántico, generación de código intermedio, optimización y generación de código objeto.

## Actividades

| Actividad | Entregable |
| --- | --- |
| 4.1 | [Mapa conceptual](Actividad%204.1%20Mapa%20Conceptual_AmauryGordillo_LYA-II_7ISCM.docx) |
| 4.2 | [Proyecto analizador semántico](Actividad%204.2%20Proyecto%20Analizador%20Semantico_AmauryGordillo_LYA-II_7ISCM.pptx) |

## Proyecto

| Carpeta | Descripción |
| --- | --- |
| [`CompiladorTECNM-JavaFX-Final`](CompiladorTECNM-JavaFX-Final) | Compilador JavaFX de un lenguaje propio en español: análisis léxico, sintáctico y semántico, traducción a Java, compilación real con `javac` y ejecución desde la misma interfaz. |

## Ejecutar el compilador

```bash
cd CompiladorTECNM-JavaFX-Final
mvn clean javafx:run
```

Requiere JDK 17 completo (incluye `javac`) y Maven 3.8 o posterior. Los detalles del proyecto, los ejemplos de prueba y la guía de las unidades están en el [README del compilador](CompiladorTECNM-JavaFX-Final/README.md).
