package nivel2;

import app.DiagramaEstados; // clase de otra carpeta del proyecto
import app.PanelControles.Estilo; // clase de otra carpeta del proyecto
import app.PanelControles.Modulo; // funciones que comparten los cuatro niveles
import app.PanelControles.PanelInferior.PanelCadena; // dibujo de la cadena de entrada
import app.PanelControles; // clase de otra carpeta del proyecto
import app.PanelControles.PanelInferior; // clase de otra carpeta del proyecto
import app.VentanaPrincipal; // clase de otra carpeta del proyecto
import java.awt.*; // colores, tamaños y formas de organizar los paneles
import java.util.ArrayList; // Se usa para guardar los pasos, reglas o estados en una lista.
import javax.swing.*; // ventanas, botones, campos de texto y paneles


/**
 * Módulo del Nivel 2: lenguajes libres de contexto con un Autómata de Pila.
 *
 * Igual que en el Nivel 3: el autómata (clase AP) procesa la cadena COMPLETA y devuelve la lista de pasos.
 * Esta pantalla dibuja el paso que le pide PanelControles: el diagrama, la cadena y la PILA animada.
 */
public class Nivel2Panel extends JPanel implements Modulo {

    // ----- Modelo -----
    private AP ap;
    private ArrayList<PasoAP> pasos = new ArrayList<PasoAP>();
    private boolean acepta = false;
    private String motivo = "";
    private String cadena = "";
    private int[] aristaDeTransicion = new int[0];     // para cada regla, qué flecha del diagrama le corresponde

    // ----- Piezas de la pantalla -----
    private DiagramaEstados diagrama = new DiagramaEstados();
    private PanelPila pila = new PanelPila();
    private PanelCadena panelCadena = new PanelCadena();
    private PanelInferior inferior = new PanelInferior(panelCadena, Estilo.AMBAR);
    private PanelControles controles;

    // Organiza el autómata de pila y conecta sus dibujos con los controles.
    public Nivel2Panel(VentanaPrincipal ventana) {
        setLayout(new BorderLayout());
        setBackground(Estilo.FONDO);
        controles = new PanelControles(this, Estilo.AMBAR, "CADENA A PROBAR");

        // En el centro van el diagrama (a la izquierda) y la pila animada (a la derecha)
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(diagrama, BorderLayout.CENTER);
        JPanel lateralPila = new JPanel(new BorderLayout());
        lateralPila.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Estilo.LINEA));
        lateralPila.add(pila, BorderLayout.CENTER);
        centro.add(lateralPila, BorderLayout.EAST);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.add(ventana.crearBarra("NIVEL 2", "Lenguajes libres de contexto · Autómata de Pila (AP)",
                "Memoria: una pila (LIFO)", Estilo.AMBAR), BorderLayout.NORTH);
        cabecera.add(controles, BorderLayout.CENTER);
        add(cabecera, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(inferior, BorderLayout.SOUTH);
        controles.iniciar();
    }

    // ------------------------------------------------------------------
    //  Los métodos de la interfaz Modulo
    // ------------------------------------------------------------------

    // Prepara el ejercicio de este nivel y devuelve su descripción.
    @Override
    public String prepararLenguaje(int opcion) {
        ap = AP.anbn();
        armarDiagrama();
        String estados = "";
        for (int i = 0; i < ap.getEstados().length; i++) {
            if (i > 0) estados = estados + ", ";
            estados = estados + ap.getEstados()[i];
        }
        return Estilo.partirLineas(ap.getDefinicion(), 34)
                + "<br>Q = {" + estados + "}<br>Σ = {" + ap.alfabetoConComas() + "}<br>Γ = {" + ap.getAlfabetoPila() + "}"
                + "<br>Acepta con la pila vacía.<br><i>La regla λ se usa solo cuando la cadena ya terminó.</i>";
    }

    /** Agrupa las reglas que van del mismo estado al mismo estado en una sola flecha (dos etiquetas por línea). */
    private void armarDiagrama() {
        int n = ap.getEstados().length;
        int cantidadReglas = ap.getTransiciones().size();
        aristaDeTransicion = new int[cantidadReglas];
        int[] origenes = new int[n * n];
        int[] destinos = new int[n * n];
        String[] etiquetas = new String[n * n];
        int cantidad = 0;
        for (int o = 0; o < n; o++) {
            for (int d = 0; d < n; d++) {
                String texto = "";
                int enLinea = 0;
                boolean hay = false;
                for (int k = 0; k < cantidadReglas; k++) {
                    Transicion t = ap.getTransiciones().get(k);
                    if (ap.indiceEstado(t.getOrigen()) == o && ap.indiceEstado(t.getDestino()) == d) {
                        if (hay) {
                            if (enLinea == 2) {
                                texto = texto + "\n";
                                enLinea = 0;
                            } else {
                                texto = texto + " | ";
                            }
                        }
                        texto = texto + t.getEtiqueta();
                        enLinea++;
                        hay = true;
                        aristaDeTransicion[k] = cantidad;
                    }
                }
                if (hay) {
                    origenes[cantidad] = o;
                    destinos[cantidad] = d;
                    etiquetas[cantidad] = texto;
                    cantidad++;
                }
            }
        }
        int[] flechaOrigen = new int[cantidad];
        int[] flechaDestino = new int[cantidad];
        String[] flechaEtiqueta = new String[cantidad];
        for (int k = 0; k < cantidad; k++) {
            flechaOrigen[k] = origenes[k];
            flechaDestino[k] = destinos[k];
            flechaEtiqueta[k] = etiquetas[k];
        }
        double[] posX = {0.12, 0.50, 0.88};
        double[] posY = {0.62, 0.62, 0.62};
        boolean[] aceptacion = new boolean[n];
        for (int i = 0; i < n; i++) {
            aceptacion[i] = ap.esFinal(i);
        }
        diagrama.definir("Leído, tope / apila  ·  Círculo doble = aceptación  ·  Ámbar = última regla",
                ap.getEstados(), aceptacion, ap.getInicial(), flechaOrigen, flechaDestino, flechaEtiqueta, posX, posY);
    }

    @Override
    // Devuelve las cadenas que aparecen como ejemplos de prueba.
    public String[] ejemplos() {
        return ap.getEjemplos();
    }

    @Override
    // Devuelve el texto que acompaña a cada ejemplo.
    public String etiquetaEjemplo(String ejemplo) {
        return ap.aceptaCadena(ejemplo) ? "acepta" : "rechaza";
    }

    @Override
    // Indica qué cadena se muestra al abrir el nivel.
    public String cadenaInicial(int indice) {
        return "aabb";
    }

    @Override
    // Calcula los pasos que debe seguir la simulación.
    public int procesar(String texto) {
        pasos = ap.simular(texto);       // si hay un símbolo fuera del alfabeto, lanza el error
        acepta = ap.isAcepta();
        motivo = ap.getMotivo();
        cadena = texto;
        return pasos.size();
    }

    @Override
    // Muestra un aviso cuando la entrada tiene un problema.
    public void mostrarError(String mensaje) {
        ArrayList<String> soloFondo = new ArrayList<String>();
        soloFondo.add(AP.FONDO);
        diagrama.mostrar(ap.getInicial(), -1, 0);
        pila.mostrar(soloFondo, soloFondo, false, 0);
        panelCadena.mostrarError(mensaje);
        inferior.mostrarError(mensaje);
    }

    @Override
    // Actualiza la pantalla con el paso seleccionado de la simulación.
    public void mostrarPaso(int paso, boolean avanzando) {
        boolean terminado = (paso == pasos.size());
        PasoAP ultimo = null;
        String estadoActual = ap.getEstadoInicial();
        ArrayList<String> pilaDespues = new ArrayList<String>();
        pilaDespues.add(AP.FONDO);
        int leidos = 0;
        if (paso > 0) {
            ultimo = pasos.get(paso - 1);
            estadoActual = ultimo.getDestino();
            pilaDespues = ultimo.getPilaDespues();
            leidos = ultimo.getLeidos();
        }
        int resultado = 0;
        if (terminado) {
            resultado = acepta ? 1 : 2;
        }

        int flecha = -1;
        if (ultimo != null) {
            flecha = aristaDeTransicion[ultimo.getTransicion()];
        }
        diagrama.mostrar(ap.indiceEstado(estadoActual), flecha, resultado);
        panelCadena.mostrar(cadena, leidos);
        // La pila solo se anima cuando se avanza un paso; al retroceder o reiniciar salta directo.
        if (avanzando && ultimo != null) {
            pila.mostrar(ultimo.getPilaAntes(), pilaDespues, true, resultado);
        } else {
            pila.mostrar(pilaDespues, pilaDespues, false, resultado);
        }

        // El registro de pasos
        StringBuilder sb = new StringBuilder();
        sb.append("Inicio: estado ").append(ap.getEstadoInicial()).append(", pila ").append(AP.FONDO)
                .append("\n(la pila se escribe con el tope a la izquierda)\n");
        for (int k = 0; k < paso; k++) {
            PasoAP p = pasos.get(k);
            Transicion t = ap.getTransiciones().get(p.getTransicion());
            sb.append("Paso ").append(k + 1).append(": ");
            if (p.getSimbolo() == AP.LAMBDA) {
                sb.append("sin leer (λ)");
            } else {
                sb.append("leo '").append(p.getSimbolo()).append("'");
            }
            sb.append("  →  ").append(t.texto()).append("\n     ")
                    .append(efecto(p.getPilaAntes(), p.getPilaDespues()))
                    .append("   ·   pila: ").append(AP.texto(p.getPilaDespues())).append("\n");
        }
        if (terminado) {
            sb.append("\nFin: ").append(acepta ? "✓ ACEPTADA — " : "✗ RECHAZADA — ").append(motivo);
        }

        String tope = "—";
        if (!pilaDespues.isEmpty()) {
            tope = pilaDespues.get(pilaDespues.size() - 1);
        }
        String estado = "Estado actual: " + estadoActual + "      Tope: " + tope
                + "      Leídos: " + leidos + " de " + cadena.length();
        String textoResultado;
        Color color = null;
        if (terminado) {
            textoResultado = acepta ? "ACEPTADA ✓" : "RECHAZADA ✗";
            color = acepta ? Estilo.OK : Estilo.ERROR;
        } else if (paso == 0) {
            textoResultado = "Listo: pulsa Paso o Ejecutar";
        } else {
            textoResultado = "Leyendo…";
        }
        inferior.mostrar(sb.toString(), estado, textoResultado, color);
    }

    /** Explica con palabras qué le pasó a la pila: apila, desapila o no cambia. */
    private String efecto(ArrayList<String> antes, ArrayList<String> despues) {
        int comun = 0;
        while (comun < antes.size() && comun < despues.size() && antes.get(comun).equals(despues.get(comun))) {
            comun++;
        }
        ArrayList<String> quitadas = new ArrayList<String>();
        for (int i = comun; i < antes.size(); i++) quitadas.add(antes.get(i));
        ArrayList<String> puestas = new ArrayList<String>();
        for (int i = comun; i < despues.size(); i++) puestas.add(despues.get(i));
        if (quitadas.isEmpty() && puestas.isEmpty()) return "la pila no cambia";
        if (quitadas.isEmpty()) return "apila " + AP.texto(puestas);
        if (puestas.isEmpty()) return "desapila " + AP.texto(quitadas);
        return "cambia " + AP.texto(quitadas) + " por " + AP.texto(puestas);
    }
}
