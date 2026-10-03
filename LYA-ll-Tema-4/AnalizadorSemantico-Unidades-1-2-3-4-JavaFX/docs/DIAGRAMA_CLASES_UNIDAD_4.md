# Diagrama de clases · Proyecto final Unidad 4

El proyecto mantiene las clases de las Unidades 1, 2 y 3 y agrega la etapa de generación de código objeto.

```mermaid
classDiagram
    class AnalizadorSemantico {
        -TablaSimbolos tabla
        -GeneradorCodigoIntermedio generadorIntermedio
        -ResultadoOptimizacion optimizacion
        -ResultadoCodigoObjeto codigoObjeto
        +analizar(List~String~ lineas)
        +getCodigoObjeto() ResultadoCodigoObjeto
    }

    class TablaSimbolos {
        -Map simbolos
        -int siguienteDireccion
        +agregar(nombre, tipo, linea) Simbolo
        +buscar(nombre) Simbolo
        +obtenerTodos() Collection~Simbolo~
    }

    class GeneradorCodigoIntermedio {
        +generar(linea, destino, expresion) ResultadoCodigoIntermedio
    }

    class OptimizadorCodigoIntermedio {
        +optimizar(List~Cuadruplo~ entrada) ResultadoOptimizacion
    }

    class GeneradorCodigoObjeto {
        +generar(ResultadoOptimizacion, Collection~Simbolo~) ResultadoCodigoObjeto
    }

    class ResultadoCodigoObjeto {
        +String arquitectura
        +List~String~ mapaMemoria
        +List~String~ ensamblador
        +List~InstruccionObjeto~ instrucciones
    }

    class InstruccionObjeto {
        +int direccion
        +String ensamblador
        +int palabra
        +hexadecimal() String
        +binario() String
    }

    AnalizadorSemantico --> TablaSimbolos
    AnalizadorSemantico --> GeneradorCodigoIntermedio
    AnalizadorSemantico --> OptimizadorCodigoIntermedio
    AnalizadorSemantico --> GeneradorCodigoObjeto
    GeneradorCodigoObjeto --> ResultadoCodigoObjeto
    ResultadoCodigoObjeto "1" *-- "*" InstruccionObjeto
```

## Flujo que documenta el diagrama

1. `AnalizadorSemantico` valida tipos, declaraciones y expresiones.
2. `GeneradorCodigoIntermedio` produce cuádruplos y código de tres direcciones.
3. `OptimizadorCodigoIntermedio` aplica las reglas de la Unidad 3.
4. `GeneradorCodigoObjeto` recibe **los cuádruplos optimizados** y las direcciones de la tabla de símbolos.
5. `ResultadoCodigoObjeto` conserva el mapa de memoria, el ensamblador y las palabras máquina.
6. `InstruccionObjeto` representa cada instrucción final en ensamblador, hexadecimal y binario.
