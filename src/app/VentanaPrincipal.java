package app;

import app.PanelControles.Estilo; // Colores y botones del diseño.


import java.awt.*; // colores, tamaños y formas de organizar los paneles
import javax.swing.*; // ventanas, botones, campos de texto y paneles
import maquina.PanelMaquina; // clase de otra carpeta del proyecto
import nivel0.Programas; // clase de otra carpeta del proyecto
import nivel1.LBAs; // clase de otra carpeta del proyecto
import nivel2.Nivel2Panel; // clase de otra carpeta del proyecto
import nivel3.Nivel3Panel; // clase de otra carpeta del proyecto


/**
 * Crea la ventana principal y el menú para entrar a cada nivel.
 * CardLayout permite cambiar de pantalla sin abrir otra ventana.
 */
public class VentanaPrincipal extends JFrame {

    // Cambia aquí tu nombre para que salga en el pie del panel principal
    private static final String AUTOR = "Emmanuel Anthony Rubio Sierra";

    // Los nombres de las cartas (para poder pedir "muéstrame esta pantalla")
    public static final String INICIO = "inicio";
    public static final String NIVEL3 = "nivel3";
    public static final String NIVEL2 = "nivel2";
    public static final String NIVEL1 = "nivel1";
    public static final String NIVEL0 = "nivel0";

    // CardLayout guarda varias pantallas en el mismo espacio y muestra solo la que se elige.
    private CardLayout cartas = new CardLayout();
    private JPanel contenido = new JPanel(cartas);

    // Crea la ventana, la barra lateral y las pantallas de los niveles.
    public VentanaPrincipal() {
        super("Simulador de Chomsky | Corte III");
        setDefaultCloseOperation(EXIT_ON_CLOSE);     // al cerrar la ventana, termina el programa
        contenido.setBackground(Estilo.FONDO);

        // Se agregan las pantallas a la baraja, cada una con su nombre
        contenido.add(crearPanelInicio(), INICIO);
        contenido.add(new Nivel3Panel(this), NIVEL3);
        contenido.add(new Nivel2Panel(this), NIVEL2);
        contenido.add(new PanelMaquina(this, "NIVEL 1", "Lenguajes sensibles al contexto · Autómata Linealmente Acotado (LBA)",
                "Memoria: cinta limitada a la entrada, con delimitadores", Estilo.OK,
                "CADENA A PROBAR", LBAs.anbncn()), NIVEL1);
        contenido.add(new PanelMaquina(this, "NIVEL 0", "Lenguajes recursivamente enumerables · Máquina de Turing",
                "Memoria: una cinta extendible en ambos sentidos", Estilo.LILA,
                "NÚMERO BINARIO", Programas.incrementoBinario()), NIVEL0);

        // Una misma barra lateral permite cambiar de nivel sin regresar al inicio.
        JPanel estructura = new JPanel(new BorderLayout());
        estructura.add(crearNavegacion(), BorderLayout.WEST);
        estructura.add(contenido, BorderLayout.CENTER);
        setContentPane(estructura);
        // El tamaño se adapta a la pantalla (en un portátil de 1366x768 también cabe)
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1200, (int) (pantalla.width * 0.94)), Math.min(780, (int) (pantalla.height * 0.92)));
        setMinimumSize(new Dimension(1050, 670));
        setLocationRelativeTo(null);                 // centrar en la pantalla
    }

    /** Muestra una pantalla de la baraja por su nombre. */
    public void mostrar(String nombre) {
        cartas.show(contenido, nombre);
    }

    /** Barra lateral compartida por las cuatro pantallas. */
    private JPanel crearNavegacion() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Estilo.PANEL);
        barra.setPreferredSize(new Dimension(162, 0));
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Estilo.LINEA));

        JLabel marca = new JLabel("<html><b>CHOMSKY</b><br><font color='#C18C63'>SIMULADOR</font></html>");
        marca.setFont(Estilo.TITULO.deriveFont(18f));
        marca.setForeground(Estilo.TEXTO);
        marca.setBorder(BorderFactory.createEmptyBorder(24, 12, 18, 5));
        barra.add(marca, BorderLayout.NORTH);

        JPanel enlaces = new JPanel(new GridLayout(5, 1, 0, 9));
        enlaces.setOpaque(false);
        enlaces.add(botonNavegar("Inicio", INICIO));
        enlaces.add(botonNavegar("Nivel 3 · AFD", NIVEL3));
        enlaces.add(botonNavegar("Nivel 2 · Pila", NIVEL2));
        enlaces.add(botonNavegar("Nivel 1 · LBA", NIVEL1));
        enlaces.add(botonNavegar("Nivel 0 · Turing", NIVEL0));
        JPanel zona = new JPanel(new BorderLayout());
        zona.setOpaque(false);
        zona.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        zona.add(enlaces, BorderLayout.NORTH);
        barra.add(zona, BorderLayout.CENTER);

        JLabel nota = new JLabel("Corte III · 2026");
        nota.setFont(Estilo.NORMAL.deriveFont(11f));
        nota.setForeground(Estilo.TEXTO_SUAVE);
        nota.setBorder(BorderFactory.createEmptyBorder(12, 12, 20, 5));
        barra.add(nota, BorderLayout.SOUTH);
        return barra;
    }

    // Crea un botón del menú lateral y le asigna su pantalla.
    private JButton botonNavegar(String texto, String destino) {
        JButton boton = Estilo.boton(texto);
        boton.setFont(Estilo.NEGRITA.deriveFont(12f));
        // Al presionar el botón se abre la pantalla indicada. La flecha -> define esa acción.
        boton.addActionListener(e -> mostrar(destino));
        return boton;
    }

    // ==========================
    // PANEL PRINCIPAL
    // ==========================

    /** Crea el panel principal: un título y 4 tarjetas (una por nivel de la Jerarquía de Chomsky). */
    private JPanel crearPanelInicio() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Estilo.FONDO);

        JLabel titulo = new JLabel("<html><font color='" + Estilo.hex(Estilo.TEXTO) + "'>CHOMSKY</font><br>"
                + "<font color='" + Estilo.hex(Estilo.TEXTO_SUAVE) + "' size='4'>SIMULADOR DE LA JERARQUÍA DE CHOMSKY · Selecciona un nivel para comenzar.</font></html>");
        titulo.setFont(Estilo.TITULO.deriveFont(30f));
        titulo.setBorder(BorderFactory.createEmptyBorder(25, 20, 10, 20));
        panel.add(titulo, BorderLayout.NORTH);

        // Las 4 tarjetas (2 filas x 2 columnas)
        JPanel rejilla = new JPanel(new GridLayout(2, 2, 12, 12));
        rejilla.setOpaque(false);
        rejilla.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        rejilla.add(crearTarjeta(NIVEL3, "NIVEL 3", "Lenguajes regulares",
                "Autómata Finito Determinista (AFD)", "Memoria: ninguna, solo el estado actual",
                "Ejemplo: cadenas binarias que terminan en 01", Estilo.CIAN));
        rejilla.add(crearTarjeta(NIVEL2, "NIVEL 2", "Lenguajes libres de contexto",
                "Autómata de Pila (AP)", "Memoria: una pila (LIFO)",
                "Ejemplo: a<sup>n</sup>b<sup>n</sup>", Estilo.AMBAR));
        rejilla.add(crearTarjeta(NIVEL1, "NIVEL 1", "Lenguajes sensibles al contexto",
                "Autómata Linealmente Acotado (LBA)", "Memoria: cinta acotada con delimitadores",
                "Ejemplo: a<sup>n</sup>b<sup>n</sup>c<sup>n</sup>", Estilo.OK));
        rejilla.add(crearTarjeta(NIVEL0, "NIVEL 0", "Lenguajes recursivamente enumerables",
                "Máquina de Turing", "Memoria: cinta extendible",
                "Ejemplo: sumar 1 a un número binario", Estilo.LILA));
        panel.add(rejilla, BorderLayout.CENTER);

        JLabel pie = new JLabel("POLITÉCNICO GRANCOLOMBIANO  |  PROYECTO CORTE III  |  " + AUTOR);
        pie.setFont(Estilo.NORMAL.deriveFont(12f));
        pie.setForeground(Estilo.TEXTO_SUAVE);
        pie.setBorder(BorderFactory.createEmptyBorder(6, 20, 14, 20));
        panel.add(pie, BorderLayout.SOUTH);
        return panel;
    }

    /** Crea una tarjeta del panel principal (un botón grande con texto HTML). */
    private JButton crearTarjeta(final String destino, String nivel, String lenguajes, String modelo,
                                 String memoria, String ejemplo, Color acento) {
        // Swing permite escribir HTML sencillo en los botones para ordenar el texto en varias líneas.
        String html = "<html><body style='width:270px'>"
                + "<font color='" + Estilo.hex(acento) + "' size='5'><b>" + nivel + "</b></font><br>"
                + "<font color='" + Estilo.hex(Estilo.TEXTO) + "' size='5'><b>" + lenguajes + "</b></font><br>"
                + "<font color='" + Estilo.hex(Estilo.TEXTO_SUAVE) + "' size='4'>" + modelo + "</font><br><br>"
                + "<font color='" + Estilo.hex(Estilo.TEXTO) + "' size='4'>" + memoria + "</font><br>"
                + "<font color='" + Estilo.hex(Estilo.TEXTO) + "' size='4'>" + ejemplo + "</font><br><br>"
                + "<font color='" + Estilo.hex(acento) + "'><b>ABRIR MÓDULO &gt;</b></font></body></html>";
        JButton tarjeta = Estilo.tarjeta(html, acento);
        tarjeta.addActionListener(e -> mostrar(destino));
        return tarjeta;
    }

    // ==========================
    // BARRA SUPERIOR DE LOS MÓDULOS
    // ==========================

    /**
     * Crea la franja de arriba que tienen todos los módulos: botón para volver al panel principal,
     * nombre del nivel, modelo y tipo de memoria. Es pública porque cada módulo la pide.
     */
    public JPanel crearBarra(String nivel, String titulo, String memoria, Color acento) {
        JPanel barra = new JPanel(new BorderLayout(18, 0));
        barra.setBackground(Estilo.FONDO);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, acento),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        barra.setPreferredSize(new Dimension(100, 76));

        JLabel etiqueta = new JLabel("<html><font color='" + Estilo.hex(acento) + "'><b>" + nivel + "</b></font>"
                + "&nbsp;&nbsp;<font color='" + Estilo.hex(Estilo.TEXTO) + "'><b>" + titulo + "</b></font>"
                + "<br><font color='" + Estilo.hex(Estilo.TEXTO_SUAVE) + "' size='3'>" + memoria + "</font></html>");
        etiqueta.setFont(Estilo.TITULO.deriveFont(20f));
        barra.add(etiqueta, BorderLayout.CENTER);
        return barra;
    }
}
