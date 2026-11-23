package app;

import java.awt.*; // colores, tamaños y formas de organizar los paneles
import java.awt.event.*; // eventos del mouse y los botones
import javax.swing.*; // ventanas, botones, campos de texto y paneles


/**
 * Controles que usan los cuatro niveles: ejemplos, entrada y botones.
 * Cada botón cambia el número de paso y el módulo actualiza sus dibujos.
 */
public class PanelControles extends JPanel implements ActionListener {

    // Cada nivel usa estas funciones para responder a los botones.
    public interface Modulo {
    /** Construye el lenguaje (o la máquina) que está en la posición "indice" y devuelve su descripción. */
    String prepararLenguaje(int indice);

    /** Las cadenas de ejemplo del lenguaje actual. */
    String[] ejemplos();

    /** Lo que se escribe junto a cada ejemplo, por ejemplo "acepta" o "rechaza". */
    String etiquetaEjemplo(String cadena);

    /** Cadena que se escribe sola al elegir el lenguaje. Si devuelve null, se deja lo que había escrito. */
    String cadenaInicial(int indice);

    /**
     * Procesa la cadena COMPLETA y devuelve cuántos pasos tiene la simulación.
     * Si la cadena no sirve (símbolos que no están en el alfabeto), lanza IllegalArgumentException.
     */
    int procesar(String cadena);

    /** Dibuja el paso número "paso" (0 = antes de empezar). "avanzando" es true si se acaba de avanzar un paso. */
    void mostrarPaso(int paso, boolean avanzando);

    /** Muestra el mensaje de error cuando la cadena no es válida. */
    void mostrarError(String mensaje);
    }


    private Modulo modulo;
    private int paso = 0;
    private int totalPasos = 0;
    private boolean valida = false;
    // Evita que los eventos del menú se ejecuten mientras se llenan los ejemplos.
    private boolean actualizando = false;
    private boolean avanzando = false;
    private String[] ejemplos = new String[0];

    private JLabel descripcion = new JLabel();
    private JComboBox<String> comboEjemplos = new JComboBox<String>();
    private JTextField campoCadena = new JTextField();
    private JButton botonCargar = Estilo.boton("Cargar");
    private JButton botonAtras = Estilo.boton("◀ Atrás");
    private JButton botonPaso = Estilo.boton("Paso ▶");
    private JButton botonEjecutar = Estilo.boton("▶ Ejecutar");
    private JButton botonReiniciar = Estilo.boton("↺ Reiniciar");
    private JButton botonFinal = Estilo.boton("Ir al final");
    // Timer llama al método de los botones cada 350 milisegundos, sin detener la ventana.
    private Timer temporizador = new Timer(350, this);

    // Crea los botones, el campo de entrada y los ejemplos del nivel.
    public PanelControles(Modulo modulo, Color acento, String rotuloEntrada) {
        this.modulo = modulo;
        Estilo.estilizar(comboEjemplos);
        Estilo.estilizar(campoCadena);
        descripcion.setFont(Estilo.NORMAL.deriveFont(13f));
        descripcion.setForeground(Estilo.TEXTO_SUAVE);
        descripcion.setVerticalAlignment(JLabel.TOP);

        // Controles en la parte superior: dos filas y una descripción breve.
        setLayout(new BorderLayout(8, 5));
        setBackground(Estilo.PANEL);
        setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JPanel primeraFila = new JPanel(new GridLayout(1, 4, 10, 0));
        primeraFila.setOpaque(false);
        primeraFila.add(etiqueta("EJEMPLOS"));
        primeraFila.add(comboEjemplos);
        primeraFila.add(etiqueta(rotuloEntrada));
        primeraFila.add(campoCadena);

        JPanel segundaFila = new JPanel(new GridLayout(1, 6, 8, 0));
        segundaFila.setOpaque(false);
        segundaFila.add(botonCargar);
        segundaFila.add(botonAtras);
        segundaFila.add(botonPaso);
        segundaFila.add(botonEjecutar);
        segundaFila.add(botonReiniciar);
        segundaFila.add(botonFinal);

        JPanel filas = new JPanel(new GridLayout(2, 1, 0, 7));
        filas.setOpaque(false);
        filas.add(primeraFila);
        filas.add(segundaFila);
        add(filas, BorderLayout.NORTH);
        descripcion.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        add(descripcion, BorderLayout.SOUTH);
        comboEjemplos.addActionListener(this);
        campoCadena.addActionListener(this);
        // Todos los botones usan el mismo método actionPerformed.
        // El ciclo evita registrar cada botón por separado.
        JButton[] botones = {botonCargar, botonAtras, botonPaso,
                botonEjecutar, botonReiniciar, botonFinal};
        for (JButton boton : botones) {
            boton.addActionListener(this);
        }
    }

    /** Prepara el único lenguaje de este nivel y carga la cadena inicial. */
    public void iniciar() {
        descripcion.setText("<html><body style='width:880px'>"
                + modulo.prepararLenguaje(0) + "</body></html>");
        campoCadena.setText(modulo.cadenaInicial(0));
        llenarEjemplos();
        cargar();
    }

    // Crea el texto pequeño que aparece sobre un campo.
    private JLabel etiqueta(String texto) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(Estilo.NEGRITA.deriveFont(12f));
        rotulo.setForeground(Estilo.AMBAR);
        return rotulo;
    }

    // Se ejecuta cuando se presiona un botón, se cambia un ejemplo o el Timer indica un nuevo paso.
    @Override
    public void actionPerformed(ActionEvent evento) {
        // getSource() devuelve el control que inició el evento (por ejemplo, botonPaso).
        Object origen = evento.getSource();
        if (origen == temporizador) {
            siguiente();
            if (paso >= totalPasos) detener();
        } else if (actualizando) {
            return;
        } else if (origen == comboEjemplos) {
            int indice = comboEjemplos.getSelectedIndex();
            if (indice > 0) {
                campoCadena.setText(ejemplos[indice - 1]);
                cargar();
            }
        } else if (origen == botonCargar || origen == campoCadena) {
            cargar();
        } else if (origen == botonPaso) {
            siguiente();
        } else if (origen == botonAtras) {
            if (valida && paso > 0) {
                detener();
                paso--;
                refrescar();
            }
        } else if (origen == botonEjecutar) {
            if (temporizador.isRunning()) {
                detener();
            } else if (valida) {
                if (paso >= totalPasos) paso = 0;
                // start() activa el Timer y llama a actionPerformed de forma periódica.
                temporizador.start();
                botonEjecutar.setText("⏸ Pausar");
                refrescar();
            }
        } else if (origen == botonReiniciar) {
            detener();
            paso = 0;
            refrescar();
        } else if (origen == botonFinal) {
            if (valida) {
                detener();
                paso = totalPasos;
                refrescar();
            }
        }
    }

    /** Los ejemplos se muestran con su resultado, pero también se puede escribir otra entrada. */
    private void llenarEjemplos() {
        actualizando = true;
        ejemplos = modulo.ejemplos();
        comboEjemplos.removeAllItems();
        comboEjemplos.addItem("Elige un ejemplo…");
        for (String ejemplo : ejemplos) {
            String texto = ejemplo.isEmpty() ? "ε (cadena vacía)" : ejemplo;
            comboEjemplos.addItem(texto + "   →   " + modulo.etiquetaEjemplo(ejemplo));
        }
        comboEjemplos.setSelectedIndex(0);
        actualizando = false;
    }

    /** La simulación calcula los pasos y luego los botones permiten recorrerlos. */
    private void cargar() {
        detener();
        // try/catch permite mostrar el error de entrada sin cerrar el programa.
        try {
            totalPasos = modulo.procesar(campoCadena.getText().trim());
            valida = true;
        } catch (IllegalArgumentException error) {
            valida = false;
            totalPasos = 0;
            modulo.mostrarError(error.getMessage());
        }
        paso = 0;
        avanzando = false;
        refrescar();
    }

    // Avanza un paso en la simulación.
    private void siguiente() {
        if (valida && paso < totalPasos) {
            paso++;
            avanzando = true;
            refrescar();
        }
    }

    // Detiene la ejecución automática.
    private void detener() {
        temporizador.stop();
        botonEjecutar.setText("▶ Ejecutar");
    }

    // Se actualiza el dibujo y se habilitan solo los botones que tienen sentido en el paso actual.
    private void refrescar() {
        if (valida) modulo.mostrarPaso(paso, avanzando);
        avanzando = false;
        botonAtras.setEnabled(valida && paso > 0);
        botonPaso.setEnabled(valida && paso < totalPasos);
        botonEjecutar.setEnabled(valida);
        botonReiniciar.setEnabled(valida && paso > 0);
        botonFinal.setEnabled(valida && paso < totalPasos);
    }

    /**
     * Colores, fuentes y botones comunes de la aplicación.
     * Se juntó aquí para no abrir un archivo solo para el estilo.
     */
    // Esta clase comparte los mismos colores y fuentes entre las pantallas.
    public static class Estilo {


    // ----- Colores -----
    public static final Color FONDO = new Color(0x202124);        // grafito (fondo)
    public static final Color PANEL = new Color(0x292B2E);        // paneles
    public static final Color PANEL_CLARO = new Color(0x373A3E);  // elementos sobre los paneles
    public static final Color LINEA = new Color(0x505357);        // bordes
    public static final Color TEXTO = new Color(0xF4F1EB);        // texto principal
    public static final Color TEXTO_SUAVE = new Color(0xBBB8B0);  // texto secundario
    public static final Color CIAN = new Color(0xC18C63);         // Nivel 3
    public static final Color AMBAR = new Color(0xC18C63);        // Nivel 2 (y lo "activo")
    public static final Color OK = new Color(0x9EC5AE);           // Nivel 1 / aceptada
    public static final Color ERROR = new Color(0xD78E84);        // rechazada
    public static final Color LILA = new Color(0xC18C63);         // Nivel 0

    // ----- Fuentes -----
    public static final Font TITULO = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font NEGRITA = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font MONO = new Font("Consolas", Font.BOLD, 22);
    public static final Font MONO_PEQ = new Font("Consolas", Font.PLAIN, 13);

    /** Convierte un color a su código hexadecimal para las etiquetas HTML. */
    // String.format arma un texto con el color en hexadecimal, por ejemplo #C18C63.
    public static String hex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    /** Devuelve el mismo color con la transparencia indicada. */
    // Math.max y Math.min limitan la transparencia entre 0 y 255.
    public static Color conAlfa(Color c, int alfa) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alfa)));
    }

    // ==========================
    // BOTONES
    // ==========================

    /** Botón normal con el estilo del proyecto. */
    public static JButton boton(String texto) {
        return crearBoton(texto, CIAN, false);
    }

    /** Crea una tarjeta para seleccionar un nivel. */
    public static JButton tarjeta(String html, Color acento) {
        return crearBoton(html, acento, true);
    }

    /**
     * Crea un botón sencillo, parecido a los del menú de DarkWear.
     * Se usa Swing normal: el fondo cambia cuando se pasa el mouse.
     * Así no es necesario dibujar botones redondeados a mano.
     */
    private static JButton crearBoton(String texto, final Color acento, boolean esTarjeta) {
        JButton boton = new JButton(texto);
        boton.setBackground(PANEL);
        boton.setForeground(TEXTO);
        boton.setFont(NEGRITA);
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        // Se juntan dos bordes: uno visible y otro que deja espacio alrededor del texto.
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(esTarjeta ? acento : LINEA, 1),
                BorderFactory.createEmptyBorder(esTarjeta ? 14 : 9, 16,
                        esTarjeta ? 14 : 9, 16)));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (esTarjeta) boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.addMouseListener(new MouseAdapter() {
            // Cambia el fondo del botón cuando el mouse pasa por encima.
            public void mouseEntered(MouseEvent e) {
                if (boton.isEnabled()) boton.setBackground(PANEL_CLARO);
            }
            // Devuelve el fondo original cuando el mouse sale del botón.
            public void mouseExited(MouseEvent e) {
                boton.setBackground(PANEL);
            }
        });
        return boton;
    }

    // ==========================
    // OTROS COMPONENTES
    // ==========================

    /** Un título pequeño de sección, con el color del nivel. */
    public static JLabel seccion(String texto, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(NEGRITA.deriveFont(12f));
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
        return l;
    }

    /** Le da el aspecto oscuro a una lista desplegable. */
    public static void estilizar(JComboBox<String> combo) {
        combo.setBackground(PANEL_CLARO);
        combo.setForeground(TEXTO);
        combo.setFont(NORMAL);
        // Renderer: cambia cómo se ve cada elemento del combo, sin cambiar su contenido.
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            // Da color y espacio a cada opción de la lista desplegable.
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                JLabel l = (JLabel) super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                l.setBackground(seleccionado ? LINEA : PANEL_CLARO);
                l.setForeground(TEXTO);
                l.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return l;
            }
        });
    }

    /** Le da el aspecto oscuro a un campo de texto. */
    public static void estilizar(JTextField campo) {
        campo.setBackground(PANEL_CLARO);
        campo.setForeground(TEXTO);
        campo.setCaretColor(CIAN);
        campo.setFont(new Font("Consolas", Font.BOLD, 16));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINEA),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    /** Parte un texto largo en líneas de máximo "ancho" caracteres, separadas con &lt;br&gt; (para HTML). */
    public static String partirLineas(String texto, int ancho) {
        // StringBuilder permite ir juntando palabras sin crear un String nuevo a cada paso.
        StringBuilder resultado = new StringBuilder();
        StringBuilder linea = new StringBuilder();
        String[] palabras = texto.split(" ");
        for (int i = 0; i < palabras.length; i++) {
            if (linea.length() > 0 && linea.length() + palabras[i].length() + 1 > ancho) {
                resultado.append(linea).append("<br>");
                linea.setLength(0);
            }
            if (linea.length() > 0) linea.append(' ');
            linea.append(palabras[i]);
        }
        resultado.append(linea);
        return resultado.toString();
    }
    }

    /**
     * Muestra el estado y el registro de pasos debajo de cada nivel.
     * Dentro está PanelCadena, que dibuja la entrada símbolo por símbolo.
     */
    // Se deja aquí porque acompaña a los controles de los cuatro niveles.
    public static class PanelInferior extends JPanel {


    /**
     * Dibuja la entrada y destaca el símbolo que se va a leer.
     * Se conserva aquí porque forma parte de la zona inferior de los niveles 2 y 3.
     */
    public static class PanelCadena extends JPanel {
    private String cadena = "";
    private int leidos = 0;          // cuántos símbolos ya se leyeron
    private String error = null;     // si no es null, se muestra este mensaje en rojo

    // Prepara el espacio donde se dibujan los símbolos de la entrada.
    public PanelCadena() {
        setBackground(Estilo.PANEL);
        setPreferredSize(new Dimension(100, 118));
    }

    // Actualiza los datos que se ven en este panel.
    public void mostrar(String cadena, int leidos) {
        this.cadena = cadena;
        this.leidos = leidos;
        this.error = null;
        repaint();
    }

    /** Para cuando la cadena escrita tiene símbolos que no están en el alfabeto. */
    public void mostrarError(String mensaje) {
        this.cadena = "";
        this.leidos = 0;
        this.error = mensaje;
        repaint();
    }

    @Override
    // Dibuja de nuevo este panel cuando la ventana se actualiza.
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Se convierte Graphics a Graphics2D para dibujar figuras con más opciones.
        Graphics2D g2 = (Graphics2D) g;
        // El antialiasing suaviza los bordes de los símbolos y figuras.
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setFont(Estilo.NEGRITA);
        g2.setColor(Estilo.TEXTO_SUAVE);
        g2.drawString("Cadena de entrada (se lee de izquierda a derecha)", 18, 26);

        if (error != null) {
            g2.setFont(Estilo.NEGRITA.deriveFont(17f));
            g2.setColor(Estilo.ERROR);
            g2.drawString("✗  " + error, 18, 80);
            g2.setFont(Estilo.NORMAL);
            g2.setColor(Estilo.TEXTO_SUAVE);
            g2.drawString("Corrige la cadena y pulsa Cargar.", 18, 104);
            return;
        }

        int n = cadena.length();
        if (n == 0) {
            g2.setFont(Estilo.MONO);
            g2.setColor(Estilo.TEXTO);
            g2.drawString("ε   (cadena vacía)", 18, 78);
            return;
        }

        int hueco = 6;
        int ancho = Math.max(16, Math.min(46, (getWidth() - 40) / n - hueco));
        int alto = 50;
        int total = n * (ancho + hueco) - hueco;
        int x0 = Math.max(18, (getWidth() - total) / 2);
        int y0 = 50;

        g2.setFont(Estilo.MONO.deriveFont(ancho >= 30 ? 22f : 13f));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < n; i++) {
            int x = x0 + i * (ancho + hueco);
            boolean yaLeido = i < leidos;
            boolean siguiente = (i == leidos);

            g2.setColor(siguiente ? Estilo.AMBAR : (yaLeido ? Estilo.FONDO : Estilo.PANEL_CLARO));
            g2.fillRoundRect(x, y0, ancho, alto, 10, 10);
            g2.setColor(siguiente ? Estilo.AMBAR : (yaLeido ? Estilo.CIAN.darker() : Estilo.LINEA));
            g2.drawRoundRect(x, y0, ancho, alto, 10, 10);

            String c = String.valueOf(cadena.charAt(i));
            g2.setColor(siguiente ? Estilo.FONDO : (yaLeido ? Estilo.TEXTO_SUAVE : Estilo.TEXTO));
            g2.drawString(c, x + (ancho - fm.stringWidth(c)) / 2, y0 + (alto + fm.getAscent()) / 2 - 3);

            if (siguiente) {         // triangulito que señala el símbolo a leer
                int cx = x + ancho / 2;
                int[] xs = {cx - 7, cx + 7, cx};
                int[] ys = {y0 - 12, y0 - 12, y0 - 3};
                g2.setColor(Estilo.AMBAR);
                g2.fillPolygon(xs, ys, 3);
            }
        }
    }
    }


    private JTextArea registro = new JTextArea();
    private JLabel etiquetaEstado = new JLabel();
    private JLabel etiquetaResultado = new JLabel();

    // superiorIzquierdo puede ser la cadena o el panel que muestra cada nivel.
    public PanelInferior(JComponent superiorIzquierdo, Color acento) {
        etiquetaEstado.setFont(Estilo.NEGRITA.deriveFont(16f));
        etiquetaEstado.setForeground(Estilo.TEXTO);
        etiquetaResultado.setFont(Estilo.NEGRITA.deriveFont(18f));
        etiquetaResultado.setForeground(Estilo.TEXTO_SUAVE);

        JPanel estado = new JPanel(new BorderLayout());
        estado.setBackground(Estilo.PANEL);
        estado.setBorder(BorderFactory.createEmptyBorder(6, 18, 12, 18));
        estado.add(etiquetaEstado, BorderLayout.WEST);
        estado.add(etiquetaResultado, BorderLayout.EAST);

        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.add(superiorIzquierdo, BorderLayout.CENTER);
        izquierda.add(estado, BorderLayout.SOUTH);

        registro.setEditable(false);
        registro.setLineWrap(true);
        registro.setWrapStyleWord(true);
        registro.setBackground(Estilo.PANEL);
        registro.setForeground(Estilo.TEXTO);
        registro.setFont(Estilo.MONO_PEQ);
        registro.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        JScrollPane desplazable = new JScrollPane(registro);
        desplazable.setBorder(BorderFactory.createLineBorder(Estilo.LINEA));
        desplazable.getViewport().setBackground(Estilo.PANEL);

        JLabel titulo = new JLabel("REGISTRO DE PASOS");
        titulo.setFont(Estilo.NEGRITA.deriveFont(12f));
        titulo.setForeground(acento);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.setBackground(Estilo.PANEL);
        derecha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 2, 0, 0, Estilo.LINEA),
                BorderFactory.createEmptyBorder(10, 14, 12, 14)));
        derecha.setPreferredSize(new Dimension(320, 0));
        derecha.add(titulo, BorderLayout.NORTH);
        derecha.add(desplazable, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Estilo.LINEA));
        setPreferredSize(new Dimension(100, 164));
        add(izquierda, BorderLayout.CENTER);
        add(derecha, BorderLayout.EAST);
    }

    /** Escribe los tres textos. "color" es el color del resultado (null = gris). */
    public void mostrar(String textoRegistro, String estado, String resultado, Color color) {
        registro.setText(textoRegistro);
        // Mueve la vista del registro hasta el texto más reciente.
        // Coloca la vista del registro al final para mostrar el último paso escrito.
        registro.setCaretPosition(registro.getDocument().getLength());
        etiquetaEstado.setText(estado);
        etiquetaResultado.setText(resultado);
        etiquetaResultado.setForeground(color == null ? Estilo.TEXTO_SUAVE : color);
    }

    /** Cuando la cadena no es válida. */
    public void mostrarError(String mensaje) {
        registro.setText(mensaje);
        etiquetaEstado.setText("Cadena no válida");
        etiquetaResultado.setText("Entrada no válida");
        etiquetaResultado.setForeground(Estilo.ERROR);
    }
    }
}
