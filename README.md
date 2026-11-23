# Simulador de la Jerarquía de Chomsky

**Asignatura:** Autómatas, Gramáticas y Lenguajes  
**Lenguaje de programación:** Java  
**Interfaz gráfica:** Java Swing

## ¿De qué trata el proyecto?

Este proyecto presenta una aplicación con interfaz gráfica para observar cómo funcionan cuatro modelos de la Jerarquía de Chomsky. Se escogió un ejercicio por nivel para mostrar los conceptos principales sin agregar otras opciones que no son necesarias para la actividad.

El usuario puede elegir un ejemplo de prueba o escribir una cadena propia. Después, el simulador muestra el recorrido del modelo: los estados del autómata, los cambios de la pila o el movimiento del cabezal, según el nivel seleccionado.

## Objetivo del proyecto

El objetivo es representar de manera visual los cuatro niveles de la Jerarquía de Chomsky mediante ejemplos sencillos. Se busca que, al ejecutar una cadena paso a paso, sea más fácil relacionar lo visto en clase con los estados, la pila y las cintas que utiliza cada modelo.

## Tecnologías utilizadas

El proyecto se desarrolló en **Java**, que permite organizar el programa en clases y escribir las reglas de cada simulación. Para construir las ventanas, los botones y las representaciones visuales se utilizó **Java Swing**. Se eligieron estas herramientas porque permiten hacer una aplicación de escritorio sin instalar librerías adicionales.

## Requisitos

- Java Development Kit (**JDK 11 o posterior**).
- **Visual Studio Code**, con la extensión **Extension Pack for Java**.

La aplicación usa Java Swing, por lo que **no requiere librerías externas**.

## Cómo ejecutar el proyecto

1. Descargar el repositorio o el archivo ZIP y, si corresponde, descomprimirlo.
2. Abrir **Visual Studio Code** y seleccionar **Archivo > Abrir carpeta**.
3. Elegir la carpeta principal, donde se encuentran `src` y `README.md`.
4. Abrir `src/app/Main.java` y seleccionar **Run**.
5. Cuando aparezca la ventana, escoger un nivel en el menú de la izquierda.

Si la opción **Run** no aparece, conviene comprobar que el JDK esté instalado y que VS Code haya cargado la extensión de Java.

## Cómo utilizar el simulador

Al entrar a un nivel se puede seleccionar uno de los ejemplos disponibles o escribir una cadena en el campo de entrada. Se presiona **Cargar** y luego se revisa la ejecución con los botones de la pantalla.

| Botón | ¿Qué hace? |
|---|---|
| **Cargar** | Prepara la entrada y los pasos de la simulación. |
| **Paso** | Muestra el siguiente movimiento. |
| **Atrás** | Regresa al movimiento anterior. |
| **Ejecutar / Pausar** | Reproduce los pasos automáticamente o detiene la reproducción. |
| **Reiniciar** | Vuelve al inicio de la simulación cargada. |
| **Ir al final** | Lleva directamente al último paso. |

Debajo de la representación se encuentra un **registro de pasos**. Este ayuda a saber qué hizo el modelo en cada movimiento. Para probar otra cadena, se escribe la nueva entrada y se vuelve a presionar **Cargar**.

## Ejemplo de ejecución: nivel 2

Para observar cómo funciona la pila, se puede seleccionar el **nivel 2** y escribir `aabb`. Después de presionar **Cargar**, se utiliza **Paso** para seguir el recorrido:

1. Al leer la primera `a`, se agrega una marca a la pila.
2. Al leer la segunda `a`, se agrega otra marca.
3. Al leer cada `b`, se retira una marca de la pila.
4. Al terminar la entrada, la pila queda sin las marcas agregadas y la cadena se acepta porque tiene dos `a` seguidas de dos `b`.

También se puede probar `aab`. En ese caso, queda una marca sin retirar y la cadena no cumple la regla. Este ejemplo sirve para comparar cómo cambia la pila en cada paso.

## Los cuatro niveles y sus ejemplos

| Nivel | Ejercicio | Entrada que cumple | Entrada que no cumple |
|---|---|---|---|
| **3 — Regular** | Cadenas binarias que terminan en `01`. | `1101` | `1010` |
| **2 — Libre de contexto** | `aⁿbⁿ`, con `n ≥ 1`: la misma cantidad de `a` y `b`, en ese orden. | `aabb` | `aab` |
| **1 — Sensible al contexto** | `aⁿbⁿcⁿ`, con `n ≥ 1`: la misma cantidad de `a`, `b` y `c`, en ese orden. | `aabbcc` | `aabbc` |
| **0 — Máquina de Turing** | Sumar 1 a un número binario. | `1011` → `1100` | `abc` (entrada inválida) |

Los ejemplos aparecen también dentro de la interfaz. Las entradas personalizadas deben respetar el alfabeto del ejercicio: `{0,1}` en los niveles 3 y 0, `{a,b}` en el nivel 2 y `{a,b,c}` en el nivel 1.

**Importante:** una cadena *rechazada* puede utilizar símbolos válidos, pero no cumplir la regla del lenguaje. Una entrada *inválida* contiene símbolos que ese ejercicio no permite.

## ¿Qué muestra cada nivel?

**Nivel 3:** un diagrama de estados. Al avanzar, se resalta el estado al que llega el autómata después de leer cada símbolo.

**Nivel 2:** el diagrama de estados y una **pila LIFO** al lado. Cuando se lee una `a`, se agrega una marca; cuando se lee una `b`, se retira una marca. Así se puede observar la comparación de cantidades.

**Nivel 1:** una **cinta acotada**, con sus fronteras, los símbolos marcados y el cabezal que puede moverse a la izquierda y a la derecha.

**Nivel 0:** una **cinta extendible** para sumar 1 en binario. Se observan los símbolos leídos y escritos, la posición del cabezal y el estado de la máquina.

## Organización de los archivos

```text
Proyecto Chomsky Final/
├── src/
│   ├── app/       # Inicio, ventana principal y elementos gráficos
│   ├── nivel3/    # Autómata finito
│   ├── nivel2/    # Autómata de pila
│   ├── nivel1/    # Máquina linealmente acotada
│   ├── nivel0/    # Operación de incremento binario
│   └── maquina/   # Reglas, cinta y pasos de las máquinas
└── README.md
```

`Main.java` inicia la aplicación. Las clases están separadas por nivel para encontrar las reglas de cada ejercicio. Algunas clases pequeñas se agruparon en un mismo archivo para que sea más sencillo revisar el código.

## Alcance del proyecto

El simulador se realizó con fines educativos. **No crea lenguajes ni autómatas nuevos**: trabaja con los cuatro ejercicios establecidos, pero permite probar otras entradas dentro de cada uno. Los botones de avanzar y retroceder muestran configuraciones que el programa ya calculó al cargar la cadena.

El documento de implementación, que se entrega por separado, explica con más detalle las clases y los conceptos utilizados. Este archivo README se concentra en cómo abrir y utilizar el programa.

> Nota de organización: en `src/app/PanelControles.java` se reúnen los controles, los colores (`Estilo`), la parte inferior (`PanelInferior`), la cadena (`PanelCadena`) y la interfaz `Modulo`. Se mantuvieron como clases internas para facilitar la revisión.

La tabla visual de reglas (`PanelReglas`) se encuentra dentro de `PanelMaquina.java`, porque solo ese módulo la utiliza.
