package nivel2;

import java.util.ArrayList; // Guarda las transiciones y los pasos de la pila.


/**
 * Maneja las reglas del autómata de pila del nivel 2.
 * La pila guarda las letras A y permite revisar si hay tantas a como b.
 */
public class AP {

    public static final char LAMBDA = 'λ';
    public static final String FONDO = "Z0";

    private String nombre;
    private String definicion;
    private String alfabeto;
    private String alfabetoPila;
    private String[] estados;
    private int inicial;
    private boolean[] finales;
    private String[] ejemplos;
    private ArrayList<Transicion> transiciones = new ArrayList<Transicion>();

    // El resultado de la última simulación
    private boolean acepta = false;
    private String motivo = "";

    public AP(String nombre, String definicion, String alfabeto, String alfabetoPila,
              String[] estados, int inicial, int[] finales, String[] ejemplos) {
        this.nombre = nombre;
        this.definicion = definicion;
        this.alfabeto = alfabeto;
        this.alfabetoPila = alfabetoPila;
        this.estados = estados;
        this.inicial = inicial;
        this.finales = new boolean[estados.length];
        for (int i = 0; i < finales.length; i++) {
            this.finales[finales[i]] = true;
        }
        this.ejemplos = ejemplos;
    }

    /**
     * Agrega la regla δ(origen, símbolo, tope) = (destino, apilar).
     * "apilar" son los símbolos separados por espacios ("A Z0" apila A y debajo Z0), o "" si no se apila nada.
     */
    public void agregarRegla(String origen, char simbolo, String tope, String destino, String apilar) {
        String[] simbolosAApilar = new String[0];
        if (!apilar.isEmpty()) {
            simbolosAApilar = apilar.split(" ");
        }
        transiciones.add(new Transicion(origen, simbolo, tope, destino, simbolosAApilar));
    }

    /** Busca la regla para (estado, símbolo, tope). Devuelve su número o -1 si no existe. */
    private int buscar(String estado, char simbolo, String tope) {
        for (int i = 0; i < transiciones.size(); i++) {
            Transicion t = transiciones.get(i);
            if (t.getOrigen().equals(estado) && t.getSimbolo() == simbolo && t.getTope().equals(tope)) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Lee la cadena y guarda los movimientos de la pila.
     * Al terminar, deja registrado si la cadena se aceptó.
     */
    public ArrayList<PasoAP> simular(String cadena) {
        for (int i = 0; i < cadena.length(); i++) {
            if (alfabeto.indexOf(cadena.charAt(i)) < 0) {
                throw new IllegalArgumentException("El símbolo '" + cadena.charAt(i)
                        + "' no pertenece al alfabeto {" + alfabetoConComas() + "}");
            }
        }
        // La lista representa la pila: el último elemento agregado es el primero que se retira (LIFO).
        ArrayList<String> pila = new ArrayList<String>();    // el último elemento es el tope
        pila.add(FONDO);
        String estado = estados[inicial];
        int leidos = 0;
        ArrayList<PasoAP> pasos = new ArrayList<PasoAP>();

        for (int vuelta = 0; vuelta < 10000; vuelta++) {
            if (pila.isEmpty()) {            // sin pila ya no se puede seguir: se decide
                if (leidos < cadena.length()) {
                    terminar(false, "La pila quedó vacía, pero todavía quedan símbolos por leer.");
                } else if (!finales[indiceEstado(estado)]) {
                    terminar(false, "La pila quedó vacía, pero " + estado + " no es un estado de aceptación.");
                } else {
                    terminar(true, "La cadena se leyó completa y la pila quedó vacía.");
                }
                return pasos;
            }
            String tope = pila.get(pila.size() - 1);
            boolean quedaCadena = leidos < cadena.length();
            char simbolo = LAMBDA;           // λ solo se usa cuando ya se acabó la cadena
            if (quedaCadena) {
                simbolo = cadena.charAt(leidos);
            }
            int numero = buscar(estado, simbolo, tope);
            if (numero < 0) {
                if (quedaCadena) {
                    terminar(false, "No existe la regla δ(" + estado + ", " + simbolo + ", " + tope
                            + "): la cadena no se puede seguir leyendo.");
                } else if (pila.size() > 1) {
                    terminar(false, "La cadena terminó, pero la pila no quedó vacía: sobran fichas (" + texto(pila) + ").");
                } else {
                    terminar(false, "La cadena terminó, pero no existe la regla δ(" + estado + ", λ, " + tope
                            + ") para vaciar la pila.");
                }
                return pasos;
            }
            Transicion t = transiciones.get(numero);
            ArrayList<String> antes = new ArrayList<String>(pila);      // una copia de la pila antes de cambiarla
            pila.remove(pila.size() - 1);                                // siempre se quita el tope...
            String[] apilar = t.getApilar();
            for (int k = apilar.length - 1; k >= 0; k--) {               // ...y se apila lo que diga la regla
                pila.add(apilar[k]);
            }
            if (quedaCadena) {
                leidos++;
            }
            estado = t.getDestino();
            pasos.add(new PasoAP(numero, t.getOrigen(), t.getDestino(), simbolo, tope,
                    antes, new ArrayList<String>(pila), leidos));
        }
        terminar(false, "La simulación no terminó (demasiados pasos).");
        return pasos;
    }

    // Guarda el resultado final de la simulación y su explicación.
    private void terminar(boolean acepta, String motivo) {
        this.acepta = acepta;
        this.motivo = motivo;
    }

    /** ¿Acepta la cadena? */
    public boolean aceptaCadena(String cadena) {
        simular(cadena);
        return acepta;
    }


    /** La pila escrita como en clase: el tope a la izquierda (por ejemplo "AAZ0"). */
    public static String texto(ArrayList<String> pilaDeAbajoHaciaArriba) {
        if (pilaDeAbajoHaciaArriba.isEmpty()) return "(vacía)";
        String resultado = "";
        for (int i = pilaDeAbajoHaciaArriba.size() - 1; i >= 0; i--) {
            resultado = resultado + pilaDeAbajoHaciaArriba.get(i);
        }
        return resultado;
    }

    // Busca la posición de un estado dentro de la lista.
    public int indiceEstado(String estado) {
        for (int i = 0; i < estados.length; i++) {
            if (estados[i].equals(estado)) return i;
        }
        return -1;
    }

    // Indica si la simulación terminó en un estado de aceptación.
    public boolean isAcepta() { return acepta; }
    public String getMotivo() { return motivo; }
    public String getNombre() { return nombre; }
    public String getDefinicion() { return definicion; }
    public String getAlfabeto() { return alfabeto; }
    // Devuelve los símbolos permitidos dentro de la pila.
    public String getAlfabetoPila() { return alfabetoPila; }
    public String[] getEstados() { return estados; }
    // Devuelve el dato de estado inicial que necesita otra parte del programa.
    public String getEstadoInicial() { return estados[inicial]; }
    public int getInicial() { return inicial; }
    public boolean esFinal(int i) { return finales[i]; }
    // Devuelve las transiciones que utiliza el autómata.
    public ArrayList<Transicion> getTransiciones() { return transiciones; }
    public String[] getEjemplos() { return ejemplos; }

    /** El alfabeto escrito como "a, b". */
    public String alfabetoConComas() {
        String resultado = "";
        for (int i = 0; i < alfabeto.length(); i++) {
            if (i > 0) resultado = resultado + ", ";
            resultado = resultado + alfabeto.charAt(i);
        }
        return resultado;
    }


    /** L = { aⁿbⁿ | n ≥ 1 }: se apila una A por cada 'a' y se desapila una por cada 'b'. */
    public static AP anbn() {
        String[] ejemplos = {"ab", "aabb", "aaabbb", "aab", "abb", "ba", ""};
        AP ap = new AP("aⁿbⁿ", "L = { aⁿbⁿ | n ≥ 1 }", "ab", "A, Z0",
                new String[]{"q0", "q1", "q2"}, 0, new int[]{2}, ejemplos);
        ap.agregarRegla("q0", 'a', "Z0", "q0", "A Z0");
        ap.agregarRegla("q0", 'a', "A", "q0", "A A");
        ap.agregarRegla("q0", 'b', "A", "q1", "");
        ap.agregarRegla("q1", 'b', "A", "q1", "");
        ap.agregarRegla("q1", LAMBDA, "Z0", "q2", "");
        return ap;
    }


}


// Clase auxiliar del mismo nivel. Se deja aquí para evitar otro archivo Java.
/**
 * Una regla del autómata de pila: δ(origen, símbolo leído, tope de la pila) = (destino, lo que se apila).
 * Es una clase de datos, como Producto o Cliente en la tienda: guarda información y la entrega con getters.
 */
class Transicion {

    private String origen;
    private char simbolo;        // el símbolo que se lee ('λ' = no se lee nada de la cadena)
    private String tope;         // el símbolo que debe estar arriba de la pila
    private String destino;
    private String[] apilar;     // lo que se apila: el primero queda arriba; arreglo vacío = ε (no se apila nada)

    public Transicion(String origen, char simbolo, String tope, String destino, String[] apilar) {
        this.origen = origen;
        this.simbolo = simbolo;
        this.tope = tope;
        this.destino = destino;
        this.apilar = apilar;
    }

    // Devuelve el estado desde el cual comienza la transición.
    public String getOrigen() { return origen; }
    public char getSimbolo() { return simbolo; }
    public String getTope() { return tope; }
    public String getDestino() { return destino; }
    public String[] getApilar() { return apilar; }

    /** Lo que se apila escrito en texto: "AZ0", o "ε" si no se apila nada. */
    public String apilarTexto() {
        if (apilar.length == 0) return "ε";
        String resultado = "";
        for (int i = 0; i < apilar.length; i++) {
            resultado = resultado + apilar[i];
        }
        return resultado;
    }

    /** La regla escrita como en clase: δ(q0, a, Z0) = (q0, AZ0). */
    public String texto() {
        return "δ(" + origen + ", " + simbolo + ", " + tope + ") = (" + destino + ", " + apilarTexto() + ")";
    }

    /** La etiqueta corta que va sobre la flecha del diagrama: "entrada,tope/lo que se apila". */
    public String getEtiqueta() {
        return simbolo + "," + tope + "/" + apilarTexto();
    }
}


// Clase auxiliar del mismo nivel. Se deja aquí para evitar otro archivo Java.
/**
 * Un paso de la simulación del autómata de pila: se aplicó una regla.
 * Guarda cómo estaba la pila antes y cómo quedó después (de abajo hacia arriba).
 */
class PasoAP {

    private int transicion;                  // número de la regla usada
    private String origen;
    private String destino;
    private char simbolo;                    // símbolo leído ('λ' si no se leyó nada)
    private String tope;                     // el tope que se quitó
    private ArrayList<String> pilaAntes;
    private ArrayList<String> pilaDespues;
    private int leidos;                      // cuántos símbolos de la cadena van leídos después de este paso

    public PasoAP(int transicion, String origen, String destino, char simbolo, String tope,
                  ArrayList<String> pilaAntes, ArrayList<String> pilaDespues, int leidos) {
        this.transicion = transicion;
        this.origen = origen;
        this.destino = destino;
        this.simbolo = simbolo;
        this.tope = tope;
        this.pilaAntes = pilaAntes;
        this.pilaDespues = pilaDespues;
        this.leidos = leidos;
    }

    // Devuelve el número de la transición realizada.
    public int getTransicion() { return transicion; }
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }
    public char getSimbolo() { return simbolo; }
    public String getTope() { return tope; }
    // Devuelve una copia de la pila antes de aplicar el paso.
    public ArrayList<String> getPilaAntes() { return pilaAntes; }
    // Devuelve el dato de pila despues que necesita otra parte del programa.
    public ArrayList<String> getPilaDespues() { return pilaDespues; }
    public int getLeidos() { return leidos; }
}
