package maquina;

import app.PanelControles.Estilo; // clase de otra carpeta del proyecto
import app.PanelControles.Modulo; // funciones que comparten los cuatro niveles
import app.PanelControles; // clase de otra carpeta del proyecto
import app.PanelControles.PanelInferior; // clase de otra carpeta del proyecto
import app.VentanaPrincipal; // clase de otra carpeta del proyecto
import java.awt.*; // colores, tamaños y formas de organizar los paneles
import java.util.ArrayList; // Guarda los pasos que aparecen en la simulación.
import javax.swing.*; // ventanas, botones, campos de texto y paneles


/**
 * La pantalla de las máquinas con cinta. La usan DOS niveles:
 *   - Nivel 1: Autómata Linealmente Acotado (cinta acotada, con delimitadores $)
 *   - Nivel 0: Máquina de Turing (cinta extendible)
 * Los dos niveles comparten esta pantalla porque ambos tienen cinta y reglas.
 *
 * En el centro se ve la cinta (con el cabezal y el estado activo) y la tabla de reglas.
 * Abajo a la izquierda se explica con palabras la regla que se acaba de aplicar.
 */
// Este panel dibuja la cinta y las reglas. La lógica de cálculo se mantiene en MaquinaTuring.
public class PanelMaquina extends JPanel implements Modulo {

    // ----- Modelo -----
    private MaquinaTuring maquina;
    private ArrayList<PasoMT> pasos = new ArrayList<PasoMT>();
    private String entrada = "";
    private Configuracion configInicial;
    private int veredicto;
    private String motivo = "";
    private String salida = "";

    // ----- Piezas de la pantalla -----
    private PanelCinta cinta = new PanelCinta();
    private PanelReglas reglas = new PanelReglas();
    private JLabel nota = new JLabel();
    private PanelInferior inferior;
    private PanelControles controles;

    // Muestra la cinta y los controles de los niveles 0 y 1.
    public PanelMaquina(VentanaPrincipal ventana, String nivel, String titulo, String memoria, Color acento,
                        String rotuloEntrada, MaquinaTuring maquina) {
        this.maquina = maquina;
        setLayout(new BorderLayout());
        setBackground(Estilo.FONDO);

        // Abajo a la izquierda: la explicación con palabras de la regla aplicada
        nota.setVerticalAlignment(JLabel.TOP);
        nota.setFont(Estilo.NORMAL.deriveFont(14f));
        nota.setForeground(Estilo.TEXTO);
        JPanel panelNota = new JPanel(new BorderLayout());
        panelNota.setBackground(Estilo.PANEL);
        panelNota.setBorder(BorderFactory.createEmptyBorder(10, 18, 4, 18));
        panelNota.add(nota, BorderLayout.CENTER);
        panelNota.setPreferredSize(new Dimension(100, 110));
        inferior = new PanelInferior(panelNota, acento);

        controles = new PanelControles(this, acento, rotuloEntrada);

        // En el centro: la cinta arriba y, debajo, la tabla de reglas con su título
        JLabel tituloReglas = new JLabel("Reglas de transición δ     la regla del último paso se resalta en ámbar; si falta una regla, la máquina rechaza");
        tituloReglas.setFont(Estilo.NEGRITA.deriveFont(13f));
        tituloReglas.setForeground(Estilo.TEXTO);
        tituloReglas.setBorder(BorderFactory.createEmptyBorder(8, 18, 6, 18));
        JScrollPane desplazable = new JScrollPane(reglas, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        desplazable.setColumnHeaderView(reglas.crearEncabezado());
        desplazable.setBorder(BorderFactory.createEmptyBorder(0, 18, 8, 18));
        desplazable.getViewport().setBackground(Estilo.PANEL);
        desplazable.setBackground(Estilo.PANEL);
        JPanel panelReglas = new JPanel(new BorderLayout());
        panelReglas.setBackground(Estilo.PANEL);
        panelReglas.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Estilo.LINEA));
        panelReglas.add(tituloReglas, BorderLayout.NORTH);
        panelReglas.add(desplazable, BorderLayout.CENTER);
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(cinta, BorderLayout.NORTH);
        centro.add(panelReglas, BorderLayout.CENTER);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.add(ventana.crearBarra(nivel, titulo, memoria, acento), BorderLayout.NORTH);
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
        cinta.configurar(maquina.isAcotada(), maquina.isTachados(), maquina.getBlanco(), maquina.getAlfabetoCinta());
        reglas.definir(maquina);

        ArrayList<String> estados = maquina.estados();
        String aceptacion = "";
        for (int i = 0; i < maquina.getAceptacion().length; i++) {
            if (i > 0) aceptacion = aceptacion + ", ";
            aceptacion = aceptacion + maquina.getAceptacion()[i];
        }
        String verbo = maquina.isFuncion() ? "termina en " : "acepta en ";
        return Estilo.partirLineas(maquina.getDefinicion(), 36)
                + "<br>Σ = {" + MaquinaTuring.alfabetoConComas(maquina.getAlfabeto()) + "}"
                + "<br>Γ = {" + MaquinaTuring.alfabetoConComas(maquina.getAlfabetoCinta()) + "}"
                + "<br>" + estados.get(0) + " … " + estados.get(estados.size() - 1) + " · inicial " + maquina.getInicial()
                + " · " + verbo + aceptacion;
    }

    @Override
    // Devuelve las cadenas que aparecen como ejemplos de prueba.
    public String[] ejemplos() {
        return maquina.getEjemplos();
    }

    @Override
    // Devuelve el texto que acompaña a cada ejemplo.
    public String etiquetaEjemplo(String ejemplo) {
        maquina.simular(ejemplo);
        if (maquina.isFuncion()) {
            return maquina.interpretar(ejemplo, maquina.getSalida());
        }
        return maquina.getVeredicto() == MaquinaTuring.ACEPTA ? "acepta" : "rechaza";
    }

    @Override
    // Indica qué cadena se muestra al abrir el nivel.
    public String cadenaInicial(int indice) {
        return maquina.getEjemplos()[0];
    }

    @Override
    // Calcula los pasos que debe seguir la simulación.
    public int procesar(String texto) {
        pasos = maquina.simular(texto);     // si la entrada no sirve, lanza el error
        configInicial = maquina.getConfigInicial();
        veredicto = maquina.getVeredicto();
        motivo = maquina.getMotivo();
        salida = maquina.getSalida();
        entrada = texto;
        return pasos.size();
    }

    @Override
    // Muestra un aviso cuando la entrada tiene un problema.
    public void mostrarError(String mensaje) {
        cinta.mostrarError(mensaje);
        reglas.marcar(-1);
        nota.setText("");
        inferior.mostrarError(mensaje);
    }

    @Override
    // Actualiza la pantalla con el paso seleccionado de la simulación.
    public void mostrarPaso(int paso, boolean avanzando) {
        boolean terminado = (paso == pasos.size());
        PasoMT ultimo = null;
        Configuracion c = configInicial;
        if (paso > 0) {
            ultimo = pasos.get(paso - 1);
            c = ultimo.getDespues();
        }
        boolean bien = (veredicto == MaquinaTuring.ACEPTA || veredicto == MaquinaTuring.TERMINA);
        int resultado = 0;
        if (terminado) {
            resultado = bien ? 1 : 2;
        }

        cinta.mostrar(c, resultado);
        reglas.marcar(ultimo == null ? -1 : ultimo.getNumeroRegla());

        // El resultado final escrito con palabras
        String textoFinal = "";
        if (terminado) {
            if (veredicto == MaquinaTuring.TERMINA) {
                String escrito = salida.isEmpty() ? "ε (la cinta quedó en blanco)" : salida;
                textoFinal = "Terminó. Resultado escrito en la cinta: " + escrito
                        + "   (" + maquina.interpretar(entrada, salida) + ")";
            } else if (veredicto == MaquinaTuring.ACEPTA) {
                textoFinal = "✓ ACEPTADA — " + motivo;
            } else {
                textoFinal = "✗ RECHAZADA — " + motivo;
            }
        }

        // El registro de pasos
        StringBuilder sb = new StringBuilder();
        sb.append("Inicio: estado ").append(maquina.getInicial()).append(", cabezal en la casilla ")
                .append(configInicial.getCabezal()).append("\n     cinta: ").append(configInicial.texto()).append("\n");
        for (int k = 0; k < paso; k++) {
            PasoMT p = pasos.get(k);
            sb.append("Paso ").append(k + 1).append(": ").append(p.getRegla().texto(p.getLeido()))
                    .append("\n     cinta: ").append(p.getDespues().texto()).append("\n");
        }
        if (terminado) {
            sb.append("\nFin: ").append(textoFinal);
        }

        // La explicación de la regla aplicada (abajo a la izquierda)
        String colorHtml = Estilo.hex(bien ? Estilo.OK : Estilo.ERROR);
        StringBuilder texto = new StringBuilder("<html><body style='width:380px'>");
        if (ultimo == null) {
            texto.append("<b>Estado inicial:</b> ").append(maquina.getInicial()).append(". El cabezal está en la casilla ")
                    .append(c.getCabezal()).append(". Pulsa <i>Paso</i> para aplicar la primera regla.");
        } else {
            texto.append("<b>Paso ").append(paso).append(":</b> ").append(ultimo.getRegla().texto(ultimo.getLeido()))
                    .append("<br><i>").append(ultimo.getRegla().getNota()).append("</i>");
        }
        if (terminado) {
            texto.append("<br><font color='").append(colorHtml).append("'><b>").append(textoFinal).append("</b></font>");
        }
        texto.append("</body></html>");
        nota.setText(texto.toString());

        // La línea de estado y el resultado
        char bajo = c.bajoCabezal();
        String lee = String.valueOf(bajo);
        if (maquina.isTachados() && bajo >= 'A' && bajo <= 'C') {
            lee = bajo + " (" + Character.toLowerCase(bajo) + " tachada)";
        }
        String marca = "";
        for (int i = 0; i < maquina.getAceptacion().length; i++) {
            if (maquina.getAceptacion()[i].equals(c.getEstado())) marca = " ✓";
        }
        String estado = "Estado activo: " + c.getEstado() + marca
                + "      Cabezal: casilla " + c.getCabezal() + "      Lee: " + lee;
        String textoResultado;
        Color color = null;
        if (terminado) {
            if (veredicto == MaquinaTuring.ACEPTA) textoResultado = "ACEPTADA ✓";
            else if (veredicto == MaquinaTuring.TERMINA) textoResultado = "TERMINÓ ✓";
            else if (veredicto == MaquinaTuring.RECHAZA) textoResultado = "RECHAZADA ✗";
            else textoResultado = "SIN FIN ✗";
            color = bien ? Estilo.OK : Estilo.ERROR;
        } else if (paso == 0) {
            textoResultado = "Listo: pulsa Paso o Ejecutar";
        } else {
            textoResultado = "Paso " + paso + " de " + pasos.size();
        }
        inferior.mostrar(sb.toString(), estado, textoResultado, color);
    }

    /**
     * Muestra las reglas de la máquina y marca la que se acaba de usar.
     * Está aquí porque solo la utiliza este panel de la máquina.
     */
    public static class PanelReglas extends JPanel {


    private static final int ALTO_FILA = 26;
    private static final int ALTO_ENCABEZADO = 30;
    // La posición (en x) donde empieza cada columna
    private static final int[] COLUMNAS = {12, 84, 212, 304, 372, 458};
    private static final String[] TITULOS = {"Estado", "Lee", "Escribe", "Mueve", "Siguiente", "Qué hace"};

    private ArrayList<Regla> reglas = new ArrayList<Regla>();
    private int activa = -1;        // la regla resaltada (-1 = ninguna)

    // Prepara el recuadro de reglas de la máquina.
    public PanelReglas() {
        setBackground(Estilo.PANEL);
    }

    /** Llena la tabla con las reglas de la máquina. */
    public void definir(MaquinaTuring maquina) {
        reglas = maquina.getReglas();
        activa = -1;
        setPreferredSize(new Dimension(760, reglas.size() * ALTO_FILA + 8));
        revalidate();
        repaint();
    }

    /** Resalta la regla número "regla" (o ninguna si es -1) y baja la tabla hasta que se vea. */
    public void marcar(int regla) {
        activa = regla;
        if (regla >= 0) {
            scrollRectToVisible(new Rectangle(0, regla * ALTO_FILA, 10, ALTO_FILA));
        }
        repaint();
    }

    /** La fila de títulos. Va aparte, para que no se mueva cuando la tabla se desplaza. */
    public JPanel crearEncabezado() {
        JPanel encabezado = new JPanel() {
            // Dibuja el panel cuando cambia un paso o se actualiza la ventana.
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(Estilo.NEGRITA.deriveFont(12f));
                g2.setColor(Estilo.CIAN);
                for (int i = 0; i < TITULOS.length; i++) {
                    g2.drawString(TITULOS[i], COLUMNAS[i], 20);
                }
            }
        };
        encabezado.setBackground(Estilo.PANEL_CLARO);
        encabezado.setPreferredSize(new Dimension(760, ALTO_ENCABEZADO));
        return encabezado;
    }

    @Override
    // Dibuja de nuevo este panel cuando la ventana se actualiza.
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        for (int i = 0; i < reglas.size(); i++) {
            Regla r = reglas.get(i);
            int y = i * ALTO_FILA;
            // El fondo de la fila: ámbar si es la regla usada; si no, colores alternados
            if (i == activa) {
                g2.setColor(Estilo.AMBAR);
            } else if (i % 2 == 0) {
                g2.setColor(Estilo.PANEL);
            } else {
                g2.setColor(Estilo.PANEL_CLARO.darker());
            }
            g2.fillRect(0, y, getWidth(), ALTO_FILA);
            g2.setColor(i == activa ? Estilo.FONDO : Estilo.TEXTO);
            g2.setFont(Estilo.MONO_PEQ.deriveFont(java.awt.Font.BOLD, 14f));
            int base = y + 18;
            g2.drawString(r.getEstado(), COLUMNAS[0], base);
            g2.drawString(r.textoLee(), COLUMNAS[1], base);
            g2.drawString(r.textoEscribe(), COLUMNAS[2], base);
            g2.drawString(r.flecha(), COLUMNAS[3], base);
            g2.drawString(r.getSiguiente(), COLUMNAS[4], base);
            g2.setFont(Estilo.NORMAL.deriveFont(12f));
            g2.drawString(r.getNota(), COLUMNAS[5], base);
        }
    }
    }
}
