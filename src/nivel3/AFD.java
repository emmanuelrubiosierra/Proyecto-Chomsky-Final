package nivel3;

import java.util.ArrayList; // Se usa para guardar los pasos, reglas o estados en una lista.


/**
 * Guarda los estados y las reglas del autómata del nivel 3.
 * Comprueba las cadenas binarias sin encargarse de los dibujos.
 */
public class AFD {

    private String nombre;
    private String definicion;
    private String alfabeto;
    private String[] estados;
    private int[][] delta;
    private int inicial;
    private boolean[] aceptacion;
    private String[] ejemplos;

    public AFD(String nombre, String definicion, String alfabeto, int cantidadEstados,
               int inicial, int[] finales, String[] ejemplos) {
        this.nombre = nombre;
        this.definicion = definicion;
        this.alfabeto = alfabeto;
        this.inicial = inicial;
        this.ejemplos = ejemplos;

        estados = new String[cantidadEstados];
        for (int i = 0; i < cantidadEstados; i++) {
            estados[i] = "q" + i;
        }
        delta = new int[cantidadEstados][alfabeto.length()];
        aceptacion = new boolean[cantidadEstados];
        // Se marcan los estados en los que se acepta la cadena.
        for (int estadoFinal : finales) {
            aceptacion[estadoFinal] = true;
        }
    }

    /** Define una transición: δ(origen, símbolo) = destino. */
    public void setTransicion(int origen, char simbolo, int destino) {
        delta[origen][alfabeto.indexOf(simbolo)] = destino;
    }


    /**
     * Lee cada símbolo y guarda los cambios de estado.
     * Si aparece una letra distinta de 0 o 1, muestra un error.
     */
    public ArrayList<Paso> simular(String cadena) {
        ArrayList<Paso> pasos = new ArrayList<Paso>();
        int actual = inicial;
        for (int i = 0; i < cadena.length(); i++) {
            char simbolo = cadena.charAt(i);
            int columna = alfabeto.indexOf(simbolo);
            if (columna < 0) {
                throw new IllegalArgumentException("El símbolo '" + simbolo
                        + "' no pertenece al alfabeto {" + alfabetoConComas() + "}");
            }
            // La matriz delta indica a qué estado se llega desde el actual al leer este símbolo.
            int siguiente = delta[actual][columna];
            pasos.add(new Paso(i, simbolo, actual, siguiente));
            actual = siguiente;
        }
        return pasos;
    }

    /** Revisa si el último estado de la cadena es de aceptación. */
    public boolean acepta(String cadena) {
        int estadoActual = inicial;
        // Se recorren los pasos. Si la cadena está vacía, queda el estado inicial.
        for (Paso paso : simular(cadena)) {
            estadoActual = paso.getDestino();
        }
        return aceptacion[estadoActual];
    }


    // Devuelve el nombre de la máquina o del autómata.
    public String getNombre() { return nombre; }
    public String getDefinicion() { return definicion; }
    public String getAlfabeto() { return alfabeto; }
    // Devuelve el dato de cantidad estados que necesita otra parte del programa.
    public int getCantidadEstados() { return estados.length; }
    // Devuelve el dato de nombre estado que necesita otra parte del programa.
    public String getNombreEstado(int i) { return estados[i]; }
    // Devuelve el estado con el que empieza el autómata.
    public int getInicial() { return inicial; }
    public boolean esAceptacion(int estado) { return aceptacion[estado]; }
    public int getDestino(int estado, int columna) { return delta[estado][columna]; }
    public String[] getEjemplos() { return ejemplos; }

    /** El alfabeto escrito como "0, 1". */
    public String alfabetoConComas() {
        String resultado = "";
        for (int i = 0; i < alfabeto.length(); i++) {
            if (i > 0) resultado = resultado + ", ";
            resultado = resultado + alfabeto.charAt(i);
        }
        return resultado;
    }


    /** L = { w ∈ {0,1}* | w termina en 01 } */
    public static AFD terminaEn01() {
        String[] ejemplos = {"01", "1101", "100101", "0", "10", "0110", "11"};
        AFD a = new AFD("Termina en 01", "L = { w ∈ {0,1}* | w termina en 01 }",
                "01", 3, 0, new int[]{2}, ejemplos);
        // q0: aún no vimos el 0 necesario    q1: el último símbolo fue 0    q2: los dos últimos fueron 01
        a.setTransicion(0, '0', 1);  a.setTransicion(0, '1', 0);
        a.setTransicion(1, '0', 1);  a.setTransicion(1, '1', 2);
        a.setTransicion(2, '0', 1);  a.setTransicion(2, '1', 0);
        return a;
    }


}


// Clase auxiliar del mismo nivel. Se deja aquí para evitar otro archivo Java.
/**
 * Guarda el símbolo leído y los estados anterior y siguiente.
 * Se utiliza para mostrar cada paso en el diagrama.
 */
class Paso {

    private final int posicion;   // en qué lugar de la cadena estaba el símbolo (0 = primero)
    private final char simbolo;   // el símbolo que se leyó
    private final int origen;     // estado antes de leer
    private final int destino;    // estado después de leer

    public Paso(int posicion, char simbolo, int origen, int destino) {
        this.posicion = posicion;
        this.simbolo = simbolo;
        this.origen = origen;
        this.destino = destino;
    }

    // Indica en qué parte de la cadena se encuentra el paso.
    public int getPosicion() { return posicion; }
    public char getSimbolo() { return simbolo; }
    public int getOrigen() { return origen; }
    public int getDestino() { return destino; }
}
