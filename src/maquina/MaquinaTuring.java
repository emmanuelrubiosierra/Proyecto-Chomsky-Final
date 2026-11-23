package maquina;

import java.util.ArrayList; // Se usa para guardar los pasos, reglas o estados en una lista.


/**
 * Maneja las reglas, la cinta y los pasos de la máquina.
 * Se utiliza en los niveles 1 y 0; el dibujo de la cinta se hace en otra clase.
 */
public class MaquinaTuring {

    // Las formas en que puede terminar la simulación
    public static final int ACEPTA = 1;
    public static final int RECHAZA = 2;
    public static final int TERMINA = 3;     // las máquinas que calculan algo "terminan" (no aceptan ni rechazan)
    public static final int SIN_FIN = 4;
    // Previene que la interfaz quede esperando una simulación que no se detiene.
    private static final int LIMITE_PASOS = 100000;

    private String nombre;
    private String definicion;
    private String alfabeto;           // Σ: los símbolos de la entrada
    private String alfabetoCinta;      // Γ: todos los símbolos que pueden aparecer en la cinta
    private char blanco;
    // En el nivel 1, la cinta tiene límites; en el nivel 0 puede crecer cuando hace falta.
    private boolean acotada;           // true = LBA, false = Máquina de Turing
    private boolean esFuncion;         // true = la máquina calcula algo (el resultado queda en la cinta)
    private String inicial;
    private String[] aceptacion;
    private String[] ejemplos;
    private ArrayList<Regla> reglas = new ArrayList<Regla>();
    private int maximoEntrada = 24;
    private boolean tachados = false;  // ¿las mayúsculas A, B, C son letras "tachadas"? (la pantalla les pone una X)
    private String formato = null;     // la entrada debe cumplir esta expresión regular
    private String mensajeFormato = "";
    private String tipo = "ACEPTADORA";    // para explicar el resultado: "SUMA", "INCREMENTO" o "ACEPTADORA"

    // El resultado de la última simulación
    private Configuracion configInicial;
    private int veredicto;
    private String motivo = "";
    private String salida = "";

    // Prepara la cinta y las reglas que utiliza la simulación.
    public MaquinaTuring(String nombre, String definicion, String alfabeto, String alfabetoCinta, char blanco,
                         boolean acotada, boolean esFuncion, String inicial, String[] aceptacion, String[] ejemplos) {
        this.nombre = nombre;
        this.definicion = definicion;
        this.alfabeto = alfabeto;
        this.alfabetoCinta = alfabetoCinta;
        this.blanco = blanco;
        this.acotada = acotada;
        this.esFuncion = esFuncion;
        this.inicial = inicial;
        this.aceptacion = aceptacion;
        this.ejemplos = ejemplos;
    }


    // Indica si los símbolos marcados se mostrarán con una X.
    public void setTachados(boolean tachados) { this.tachados = tachados; }

    // Actualiza maximo entrada para mostrarlo o utilizarlo en la simulación.
    public void setMaximoEntrada(int maximo) { this.maximoEntrada = maximo; }

    // Actualiza tipo para mostrarlo o utilizarlo en la simulación.
    public void setTipo(String tipo) { this.tipo = tipo; }

    /** La entrada debe cumplir este formato (expresión regular); si no, se muestra el mensaje. */
    public void setFormato(String regex, String mensaje) {
        this.formato = regex;
        this.mensajeFormato = mensaje;
    }

    /** Agrega la regla δ(estado, lee) = (siguiente, escribe, mueve). Un '=' en "escribe" deja el símbolo igual. */
    public void agregarRegla(String estado, String lee, String siguiente, char escribe, char mueve, String nota) {
        for (int i = 0; i < lee.length(); i++) {
            if (buscarRegla(estado, lee.charAt(i)) >= 0) {
                throw new IllegalStateException("Regla repetida: δ(" + estado + ", " + lee.charAt(i) + ")");
            }
        }
        reglas.add(new Regla(estado, lee, siguiente, escribe, mueve, nota));
    }

    /** Busca la regla para (estado, símbolo). Devuelve su número o -1 si no existe. */
    private int buscarRegla(String estado, char simbolo) {
        for (int i = 0; i < reglas.size(); i++) {
            Regla r = reglas.get(i);
            if (r.getEstado().equals(estado) && r.getLee().indexOf(simbolo) >= 0) {
                return i;
            }
        }
        return -1;
    }

    // Comprueba si el estado actual pertenece a los estados finales.
    private boolean esAceptacion(String estado) {
        for (int i = 0; i < aceptacion.length; i++) {
            if (aceptacion[i].equals(estado)) return true;
        }
        return false;
    }


    /**
     * Ejecuta la máquina con la entrada completa y devuelve la lista de todos los pasos.
     * La pantalla solo "reproduce" esa lista. Después de llamarla, getVeredicto(), getMotivo()
     * y getSalida() dicen cómo terminó.
     */
    public ArrayList<PasoMT> simular(String entrada) {
        validar(entrada);
        // StringBuilder permite escribir, borrar y ampliar la cinta durante la simulación.
        StringBuilder cinta = new StringBuilder();
        int inicio = 0;
        int cabezal;
        if (acotada) {                       // la cinta es: $ entrada $   y el cabezal empieza en la primera letra
            cinta.append('$').append(entrada).append('$');
            cabezal = 1;
        } else {
            cinta.append(entrada);
            if (cinta.length() == 0) {
                cinta.append(blanco);
            }
            cabezal = 0;
        }
        String estado = inicial;
        configInicial = new Configuracion(estado, cinta.toString(), inicio, cabezal);
        ArrayList<PasoMT> pasos = new ArrayList<PasoMT>();

        // El límite evita que una máquina que no termina deje bloqueado el programa.
        for (int vuelta = 0; vuelta < LIMITE_PASOS; vuelta++) {
            if (esAceptacion(estado)) {
                if (esFuncion) {
                    terminar(TERMINA, "La máquina llegó al estado " + estado + " y se detuvo.", cinta);
                } else {
                    terminar(ACEPTA, "La máquina llegó al estado " + estado + " de aceptación.", cinta);
                }
                return pasos;
            }
            char leido = cinta.charAt(cabezal - inicio);
            // Se busca la regla según el estado actual y el símbolo bajo el cabezal.
            int numero = buscarRegla(estado, leido);
            if (numero < 0) {
                terminar(RECHAZA, "No existe la regla δ(" + estado + ", " + leido + "): la máquina se detiene y rechaza.", cinta);
                return pasos;
            }
            Regla r = reglas.get(numero);
            char escrito = r.getEscribe();
            if (escrito == '=') {
                escrito = leido;
            }
            int nuevo = cabezal;
            if (r.getMueve() == 'I') nuevo--;
            if (r.getMueve() == 'D') nuevo++;
            if (acotada && (nuevo - inicio < 0 || nuevo - inicio >= cinta.length())) {
                terminar(RECHAZA, "El cabezal intentó salirse de la cinta acotada: no está permitido.", cinta);
                return pasos;
            }
            cinta.setCharAt(cabezal - inicio, escrito);
            if (!acotada) {                  // la cinta crece: la casilla nueva aparece en blanco
                if (nuevo < inicio) {
                    cinta.insert(0, blanco);
                    inicio--;
                } else if (nuevo - inicio >= cinta.length()) {
                    cinta.append(blanco);
                }
            }
            String antes = estado;
            cabezal = nuevo;
            estado = r.getSiguiente();
            pasos.add(new PasoMT(numero, r, antes, leido, escrito,
                    new Configuracion(estado, cinta.toString(), inicio, cabezal)));
        }
        terminar(SIN_FIN, "La máquina no terminó en " + LIMITE_PASOS + " pasos.", cinta);
        return pasos;
    }

    /** Guarda cómo terminó la simulación. En las máquinas que calculan algo, guarda también lo que quedó en la cinta. */
    private void terminar(int veredicto, String motivo, StringBuilder cinta) {
        this.veredicto = veredicto;
        this.motivo = motivo;
        salida = "";
        if (esFuncion && veredicto == TERMINA) {
            for (int i = 0; i < cinta.length(); i++) {
                if (cinta.charAt(i) != blanco) {
                    salida = salida + cinta.charAt(i);
                }
            }
        }
    }

    /** Revisa que la entrada sirva: largo máximo, símbolos del alfabeto y formato. */
    private void validar(String entrada) {
        if (entrada.length() > maximoEntrada) {
            throw new IllegalArgumentException("La entrada es muy larga (máximo " + maximoEntrada + " símbolos).");
        }
        for (int i = 0; i < entrada.length(); i++) {
            if (alfabeto.indexOf(entrada.charAt(i)) < 0) {
                throw new IllegalArgumentException("El símbolo '" + entrada.charAt(i)
                        + "' no pertenece al alfabeto {" + alfabetoConComas(alfabeto) + "}");
            }
        }
        if (formato != null && !entrada.matches(formato)) {
            throw new IllegalArgumentException(mensajeFormato);
        }
    }


    /** Explica el resultado con palabras (por ejemplo "3 + 2 = 5"), o devuelve lo que quedó en la cinta. */
    public String interpretar(String entrada, String salida) {
        if (tipo.equals("SUMA")) {
            int m = entrada.indexOf('+');
            int n = entrada.length() - m - 1;
            return m + " + " + n + " = " + salida.length();
        }
        if (tipo.equals("INCREMENTO")) {
            long n = Long.parseLong(entrada, 2);
            return entrada + " (" + n + ") + 1 = " + salida + " (" + (n + 1) + ")";
        }
        return salida;
    }

    /** Los estados, en el orden en que aparecen en las reglas. */
    public ArrayList<String> estados() {
        ArrayList<String> lista = new ArrayList<String>();
        lista.add(inicial);
        for (int i = 0; i < reglas.size(); i++) {
            Regla r = reglas.get(i);
            if (!lista.contains(r.getEstado())) lista.add(r.getEstado());
            if (!lista.contains(r.getSiguiente())) lista.add(r.getSiguiente());
        }
        return lista;
    }

    /** Escribe los símbolos separados por comas: "a, b, c". */
    public static String alfabetoConComas(String simbolos) {
        String resultado = "";
        for (int i = 0; i < simbolos.length(); i++) {
            if (i > 0) resultado = resultado + ", ";
            resultado = resultado + simbolos.charAt(i);
        }
        return resultado;
    }

    // Devuelve el nombre de la máquina o del autómata.
    public String getNombre() { return nombre; }
    public String getDefinicion() { return definicion; }
    public String getAlfabeto() { return alfabeto; }
    // Devuelve el dato de alfabeto cinta que necesita otra parte del programa.
    public String getAlfabetoCinta() { return alfabetoCinta; }
    public char getBlanco() { return blanco; }
    // Indica si la cinta tiene límites, como en el nivel 1.
    public boolean isAcotada() { return acotada; }
    public boolean isFuncion() { return esFuncion; }
    public boolean isTachados() { return tachados; }
    public String getInicial() { return inicial; }
    public String[] getAceptacion() { return aceptacion; }
    // Devuelve la lista de reglas de la máquina.
    public ArrayList<Regla> getReglas() { return reglas; }
    public String[] getEjemplos() { return ejemplos; }
    // Devuelve el dato de config inicial que necesita otra parte del programa.
    public Configuracion getConfigInicial() { return configInicial; }
    public int getVeredicto() { return veredicto; }
    public String getMotivo() { return motivo; }
    // Devuelve el contenido final de la cinta como resultado.
    public String getSalida() { return salida; }
}


// Clase auxiliar del mismo nivel. Se deja aquí para evitar otro archivo Java.
/**
 * Una regla de la máquina: δ(estado, lee) = (siguiente, escribe, mueve).
 * "lee" puede tener varios símbolos que hacen lo mismo (por ejemplo "abc").
 * "escribe" es el símbolo que se escribe; si es '=' significa "escribe el mismo que leyó".
 * "mueve" es 'I' (izquierda), 'D' (derecha) o 'Q' (se queda quieto).
 */
class Regla {

    private String estado;
    private String lee;
    private String siguiente;
    private char escribe;
    private char mueve;
    private String nota;      // explicación de la regla con palabras

    public Regla(String estado, String lee, String siguiente, char escribe, char mueve, String nota) {
        this.estado = estado;
        this.lee = lee;
        this.siguiente = siguiente;
        this.escribe = escribe;
        this.mueve = mueve;
        this.nota = nota;
    }

    // Devuelve el estado guardado en este paso.
    public String getEstado() { return estado; }
    public String getLee() { return lee; }
    public String getSiguiente() { return siguiente; }
    public char getEscribe() { return escribe; }
    public char getMueve() { return mueve; }
    // Devuelve la explicación breve de la regla.
    public String getNota() { return nota; }

    /** Los símbolos que lee, separados por espacios: "a b c". */
    public String textoLee() {
        String resultado = "";
        for (int i = 0; i < lee.length(); i++) {
            if (i > 0) resultado = resultado + " ";
            resultado = resultado + lee.charAt(i);
        }
        return resultado;
    }

    // Prepara el símbolo que se muestra en la tabla de reglas.
    public String textoEscribe() {
        if (escribe == '=') return "(igual)";
        return String.valueOf(escribe);
    }

    /** La flecha del movimiento: ← (izquierda), → (derecha) o • (quieto). */
    public String flecha() {
        if (mueve == 'I') return "←";
        if (mueve == 'D') return "→";
        return "•";
    }

    /** La regla escrita como en clase, para un símbolo concreto: δ(q0, a) = (q1, A, →). */
    public String texto(char leido) {
        char escrito = escribe;
        if (escribe == '=') escrito = leido;
        return "δ(" + estado + ", " + leido + ") = (" + siguiente + ", " + escrito + ", " + flecha() + ")";
    }
}


// Clase auxiliar del mismo nivel. Se deja aquí para evitar otro archivo Java.
/**
 * Una "foto" de la máquina en un momento: el estado, lo que hay escrito en la cinta y dónde está el cabezal.
 * "inicio" es la posición de la primera casilla de la cinta: si la cinta crece hacia la izquierda, es negativa.
 */
class Configuracion {

    private String estado;
    private String celdas;      // las casillas que existen, de izquierda a derecha
    private int inicio;         // posición de la primera casilla
    private int cabezal;        // posición del cabezal

    public Configuracion(String estado, String celdas, int inicio, int cabezal) {
        this.estado = estado;
        this.celdas = celdas;
        this.inicio = inicio;
        this.cabezal = cabezal;
    }

    // Devuelve el estado guardado en este paso.
    public String getEstado() { return estado; }
    public String getCeldas() { return celdas; }
    public int getInicio() { return inicio; }
    public int getCabezal() { return cabezal; }

    /** El símbolo que está bajo el cabezal. */
    public char bajoCabezal() {
        return celdas.charAt(cabezal - inicio);
    }

    /** La cinta escrita en texto, con el cabezal entre corchetes: "$ A [b] c $". */
    public String texto() {
        String resultado = "";
        for (int i = 0; i < celdas.length(); i++) {
            if (i > 0) resultado = resultado + " ";
            if (i + inicio == cabezal) {
                resultado = resultado + "[" + celdas.charAt(i) + "]";
            } else {
                resultado = resultado + celdas.charAt(i);
            }
        }
        return resultado;
    }
}


// Clase auxiliar del mismo nivel. Se deja aquí para evitar otro archivo Java.
/** Un paso de la simulación de la máquina: se aplicó una regla y la máquina quedó en una nueva configuración. */
class PasoMT {

    private int numeroRegla;        // posición de la regla en la lista (para resaltarla en la tabla)
    private Regla regla;
    private String estadoAntes;
    private char leido;
    private char escrito;
    private Configuracion despues;  // cómo quedó la máquina después del paso

    public PasoMT(int numeroRegla, Regla regla, String estadoAntes, char leido, char escrito, Configuracion despues) {
        this.numeroRegla = numeroRegla;
        this.regla = regla;
        this.estadoAntes = estadoAntes;
        this.leido = leido;
        this.escrito = escrito;
        this.despues = despues;
    }

    // Indica qué regla se aplicó durante el paso.
    public int getNumeroRegla() { return numeroRegla; }
    public Regla getRegla() { return regla; }
    // Devuelve el dato de estado antes que necesita otra parte del programa.
    public String getEstadoAntes() { return estadoAntes; }
    public char getLeido() { return leido; }
    public char getEscrito() { return escrito; }
    // Devuelve cómo quedó la cinta después del paso.
    public Configuracion getDespues() { return despues; }
}
