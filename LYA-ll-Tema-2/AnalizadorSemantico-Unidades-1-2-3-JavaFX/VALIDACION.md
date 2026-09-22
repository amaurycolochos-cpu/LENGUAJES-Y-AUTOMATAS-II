# Validación realizada

Se verificó la lógica del proyecto antes de empaquetarlo:

- Compilación del núcleo Java con `javac --release 17`: correcta.
- 15/15 archivos preparados de Unidad 2: correctos y con generación de código intermedio.
- 15/15 archivos preparados de Unidad 3: correctos y con generación de código intermedio.
- Regresión del `programa_correcto.txt` original de Unidad 1: análisis semántico correcto.
- Archivo FXML: XML válido.
- Todos los `fx:id` del FXML tienen su campo `@FXML` correspondiente en el controlador.

La interfaz JavaFX completa no se lanzó en el entorno de validación porque no dispone de Maven/OpenJFX instalado. El proyecto conserva configuración Maven con JavaFX 17 para ejecutarse con `mvn clean javafx:run` en el equipo de desarrollo.
