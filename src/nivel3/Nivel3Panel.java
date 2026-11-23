package nivel3;

import app.DiagramaEstados; // clase de otra carpeta del proyecto
import app.PanelControles.Estilo; // clase de otra carpeta del proyecto
import app.PanelControles.Modulo; // funciones que comparten los cuatro niveles
import app.PanelControles.PanelInferior.PanelCadena; // dibujo de la cadena de entrada
import app.PanelControles; // clase de otra carpeta del proyecto
import app.PanelControles.PanelInferior; // clase de otra carpeta del proyecto
import app.VentanaPrincipal; // clase de otra carpeta del proyecto
import java.awt.*; // colores, tamaños y formas de organizar los paneles
import java.util.ArrayList; // Se usa para guardar los pasos, reglas o estados en una lista.
import javax.swing.JPanel; // Permite dibujar o acomodar elementos en un panel.


/**
 * Módulo del Nivel 3: lenguajes regulares con un Autómata Finito Determinista.
 *
 * Funciona así:
 *   1) El autómata (clase AFD) procesa la cadena COMPLETA y produce una lista de pasos.
 *   2) Esta pantalla solo "reproduce" esa lista: el contador "paso" vive en PanelControles,
 *      que nos pide dibujar el paso número N (método mostrarPaso).
 */
public class Nivel3Panel extends JPanel implements Modulo {

    // ----- Modelo -----
    private AFD afd;
    private ArrayList<Paso> pasos = new ArrayList<Paso>();
    private String cadena = "";

    // ----- Piezas de la pantalla -----
    private DiagramaEstados diagrama = new DiagramaEstados();
    private PanelCadena panelCadena = new PanelCadena();
    private PanelInferior inferior = new PanelInferior(panelCadena, Estilo.CIAN);
    private PanelControles controles;

    // Las flechas del diagrama: la flecha k va de flechaOrigen[k] a flechaDestino[k]
    private int[] flechaOrigen;
    private int[] flechaDestino;

    // Organiza el autómata finito y conecta su diagrama con los controles.
    public Nivel3Panel(VentanaPrincipal ventana) {
        setLayout(new BorderLayout());
        setBackground(Estilo.FONDO);
        controles = new PanelControles(this, Estilo.CIAN, "CADENA A PROBAR");

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.add(ventana.crearBarra("NIVEL 3", "Lenguajes regulares · Autómata Finito Determinista (AFD)",
                "Memoria: ninguna, solo el estado actual", Estilo.CIAN), BorderLayout.NORTH);
        cabecera.add(controles, BorderLayout.CENTER);
        add(cabecera, BorderLayout.NORTH);
        add(diagrama, BorderLayout.CENTER);
        add(inferior, BorderLayout.SOUTH);
        controles.iniciar();
    }

    // ------------------------------------------------------------------
    //  Los métodos de la interfaz Modulo
    // ------------------------------------------------------------------

    /** Se construye un único AFD para el lenguaje que termina en 01. */
    @Override
    public String prepararLenguaje(int opcion) {
        afd = AFD.terminaEn01();
        armarDiagrama();
        return afd.getDefinicion().replace(" | ", " |<br>&nbsp;&nbsp;")
                + "<br>Alfabeto Σ = {" + afd.alfabetoConComas() + "}<br>"
                + afd.getCantidadEstados() + " estados: " + afd.getNombreEstado(0) + " … "
                + afd.getNombreEstado(afd.getCantidadEstados() - 1);
    }

    /**
     * Arma las flechas del diagrama a partir de la tabla delta.
     * Si desde un mismo estado, dos símbolos llevan al mismo destino, se dibuja una sola flecha con "0, 1".
     */
    private void armarDiagrama() {
        int n = afd.getCantidadEstados();
        String alfabeto = afd.getAlfabeto();
        int[] origenes = new int[n * n];
        int[] destinos = new int[n * n];
        String[] etiquetas = new String[n * n];
        int cantidad = 0;
        for (int s = 0; s < n; s++) {
            for (int d = 0; d < n; d++) {
                String simbolos = "";
                for (int c = 0; c < alfabeto.length(); c++) {
                    if (afd.getDestino(s, c) == d) {
                        if (!simbolos.isEmpty()) simbolos = simbolos + ", ";
                        simbolos = simbolos + alfabeto.charAt(c);
                    }
                }
                if (!simbolos.isEmpty()) {
                    origenes[cantidad] = s;
                    destinos[cantidad] = d;
                    etiquetas[cantidad] = simbolos;
                    cantidad++;
                }
            }
        }
        // Se copian a arreglos del tamaño justo
        flechaOrigen = new int[cantidad];
        flechaDestino = new int[cantidad];
        String[] etiquetasJustas = new String[cantidad];
        for (int k = 0; k < cantidad; k++) {
            flechaOrigen[k] = origenes[k];
            flechaDestino[k] = destinos[k];
            etiquetasJustas[k] = etiquetas[k];
        }
        String[] nombres = new String[n];
        boolean[] aceptacion = new boolean[n];
        for (int i = 0; i < n; i++) {
            nombres[i] = afd.getNombreEstado(i);
            aceptacion[i] = afd.esAceptacion(i);
        }
        diagrama.definir("Círculo doble = estado de aceptación  ·  Ámbar = estado actual y última transición usada",
                nombres, aceptacion, afd.getInicial(), flechaOrigen, flechaDestino, etiquetasJustas, null, null);
    }

    @Override
    // Devuelve las cadenas que aparecen como ejemplos de prueba.
    public String[] ejemplos() {
        return afd.getEjemplos();
    }

    @Override
    // Devuelve el texto que acompaña a cada ejemplo.
    public String etiquetaEjemplo(String ejemplo) {
        return afd.acepta(ejemplo) ? "acepta" : "rechaza";
    }

    @Override
    // Indica qué cadena se muestra al abrir el nivel.
    public String cadenaInicial(int indice) {
        return "1101";
    }

    /** Calcula la lista de pasos de la cadena completa. */
    @Override
    public int procesar(String texto) {
        pasos = afd.simular(texto);      // si hay un símbolo fuera del alfabeto, lanza el error
        cadena = texto;
        return pasos.size();
    }

    @Override
    // Muestra un aviso cuando la entrada tiene un problema.
    public void mostrarError(String mensaje) {
        diagrama.mostrar(afd.getInicial(), -1, 0);
        panelCadena.mostrarError(mensaje);
        inferior.mostrarError(mensaje);
    }

    /** Dibuja el paso número "paso" (0 = antes de leer nada). */
    @Override
    public void mostrarPaso(int paso, boolean avanzando) {
        boolean terminado = (paso == pasos.size());
        int estadoActual = afd.getInicial();
        Paso ultimo = null;
        if (paso > 0) {
            ultimo = pasos.get(paso - 1);
            estadoActual = ultimo.getDestino();
        }
        int resultado = 0;
        if (terminado) {
            resultado = afd.esAceptacion(estadoActual) ? 1 : 2;
        }

        // Qué flecha del diagrama es la última que se usó
        int flechaUsada = -1;
        if (ultimo != null) {
            for (int k = 0; k < flechaOrigen.length; k++) {
                if (flechaOrigen[k] == ultimo.getOrigen() && flechaDestino[k] == ultimo.getDestino()) {
                    flechaUsada = k;
                }
            }
        }
        diagrama.mostrar(estadoActual, flechaUsada, resultado);
        panelCadena.mostrar(cadena, paso);

        // El registro de pasos (texto)
        StringBuilder sb = new StringBuilder();
        sb.append("Inicio en ").append(afd.getNombreEstado(afd.getInicial())).append("\n");
        for (int k = 0; k < paso; k++) {
            Paso p = pasos.get(k);
            sb.append("Paso ").append(k + 1).append(": leo '").append(p.getSimbolo()).append("'  →  δ(")
                    .append(afd.getNombreEstado(p.getOrigen())).append(", ").append(p.getSimbolo()).append(") = ")
                    .append(afd.getNombreEstado(p.getDestino())).append("\n");
        }
        if (terminado) {
            sb.append("\nFin de la cadena. ").append(afd.getNombreEstado(estadoActual));
            if (resultado == 1) {
                sb.append(" es de aceptación → ACEPTADA");
            } else {
                sb.append(" NO es de aceptación → RECHAZADA");
            }
        }

        String estado = "Estado actual: " + afd.getNombreEstado(estadoActual)
                + "      Símbolos leídos: " + paso + " de " + pasos.size();
        String textoResultado;
        Color color = null;
        if (terminado) {
            textoResultado = (resultado == 1) ? "ACEPTADA ✓" : "RECHAZADA ✗";
            color = (resultado == 1) ? Estilo.OK : Estilo.ERROR;
        } else if (paso == 0) {
            textoResultado = "Listo: pulsa Paso o Ejecutar";
        } else {
            textoResultado = "Leyendo…";
        }
        inferior.mostrar(sb.toString(), estado, textoResultado, color);
    }
}
