package maquina;

import app.PanelControles.Estilo; // clase de otra carpeta del proyecto
import java.awt.*; // colores, tamaños y formas de organizar los paneles
import javax.swing.JPanel; // Permite dibujar o acomodar elementos en un panel.


/**
 * Dibuja la cinta como una fila de casillas, con el cabezal (el triángulo ámbar) y el estado activo encima.
 *   - En el LBA la cinta es acotada: se ven los delimitadores $ y una línea que marca la zona permitida.
 *   - En la Máquina de Turing la cinta se extiende: a los lados se ven puntos suspensivos y las casillas
 *     nuevas aparecen en blanco. Debajo de cada casilla va su número de posición.
 * Las letras tachadas (mayúsculas en el LBA) se dibujan en minúscula con una X roja.
 */
// Representa las casillas de la cinta y resalta la posición del cabezal.
public class PanelCinta extends JPanel {

    private Configuracion config = null;
    private boolean acotada = false;
    private boolean tachados = false;
    private char blanco = '_';
    private String gamma = "";
    private int resultado = 0;       // 0 = en proceso, 1 = bien (aceptó o terminó), 2 = rechazó
    private String error = null;

    // Prepara la representación de las casillas de la cinta.
    public PanelCinta() {
        setBackground(Estilo.PANEL);
        setPreferredSize(new Dimension(100, 262));
    }

    /** Le dice al panel con qué tipo de máquina va a trabajar. */
    public void configurar(boolean acotada, boolean tachados, char blanco, String alfabetoCinta) {
        this.acotada = acotada;
        this.tachados = tachados;
        this.blanco = blanco;
        this.gamma = alfabetoCinta;
    }

    // Actualiza los datos que se ven en este panel.
    public void mostrar(Configuracion config, int resultado) {
        this.config = config;
        this.resultado = resultado;
        this.error = null;
        repaint();
    }

    // Muestra un aviso cuando la entrada tiene un problema.
    public void mostrarError(String mensaje) {
        this.config = null;
        this.error = mensaje;
        repaint();
    }

    @Override
    // Dibuja de nuevo este panel cuando la ventana se actualiza.
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Los textos de arriba
        g2.setFont(Estilo.NEGRITA.deriveFont(15f));
        g2.setColor(Estilo.TEXTO);
        g2.drawString(acotada ? "Cinta acotada (LBA)" : "Cinta extendible (Máquina de Turing)", 18, 28);
        g2.setFont(Estilo.NORMAL.deriveFont(12f));
        g2.setColor(Estilo.TEXTO_SUAVE);
        if (acotada) {
            g2.drawString("Solo existen las casillas de la entrada, encerradas entre dos delimitadores $. El cabezal no puede salirse.", 18, 48);
        } else {
            g2.drawString("Infinita hacia los dos lados: cuando el cabezal llega a una casilla nueva, esa casilla aparece en blanco ( _ ).", 18, 48);
        }
        String linea3 = "Alfabeto de la cinta  Γ = {" + MaquinaTuring.alfabetoConComas(gamma) + "}";
        if (tachados) linea3 = linea3 + "     (A, B, C son a, b, c ya tachadas)";
        if (!acotada) linea3 = linea3 + "     ( _  es el blanco)";
        g2.drawString(linea3, 18, 66);

        if (error != null) {
            g2.setFont(Estilo.NEGRITA.deriveFont(17f));
            g2.setColor(Estilo.ERROR);
            g2.drawString("✗  " + error, 18, 150);
            g2.setFont(Estilo.NORMAL);
            g2.setColor(Estilo.TEXTO_SUAVE);
            g2.drawString("Corrige la entrada y pulsa Cargar.", 18, 176);
            return;
        }
        if (config == null) return;

        String celdas = config.getCeldas();
        int total = celdas.length();

        // 1) Qué casillas se ven (la cinta puede ser más larga que la pantalla)
        int hueco = 6;
        int ancho = 46;
        int alto = 54;
        int disponible = getWidth() - 70;
        int primera;
        int ultima;
        if (acotada) {
            primera = config.getInicio();
            ultima = config.getInicio() + total - 1;
            if (total * (ancho + hueco) - hueco > disponible) {
                ancho = Math.max(20, (disponible + hueco) / total - hueco);
            }
        } else {
            primera = config.getInicio() - 2;           // siempre se ven dos casillas en blanco a cada lado
            ultima = config.getInicio() + total + 1;
            int visibles = Math.max(5, (disponible + hueco) / (ancho + hueco));
            if (ultima - primera + 1 > visibles) {      // no cabe todo: la ventana sigue al cabezal
                primera = config.getCabezal() - visibles / 2;
                ultima = primera + visibles - 1;
            }
        }
        int cantidad = ultima - primera + 1;
        int anchoTotal = cantidad * (ancho + hueco) - hueco;
        int x0 = (getWidth() - anchoTotal) / 2;
        int y0 = 150;

        g2.setFont(Estilo.MONO.deriveFont(ancho >= 34 ? 22f : 14f));
        FontMetrics fm = g2.getFontMetrics();

        // 2) Las casillas
        for (int j = 0; j < cantidad; j++) {
            int pos = primera + j;
            int x = x0 + j * (ancho + hueco);
            boolean existe = pos >= config.getInicio() && pos < config.getInicio() + total;
            char c = blanco;
            if (existe) {
                c = celdas.charAt(pos - config.getInicio());
            }
            boolean esCabezal = (pos == config.getCabezal());
            boolean delimitador = acotada && c == '$';
            boolean enBlanco = !acotada && c == blanco;

            Color relleno = Estilo.PANEL_CLARO;
            if (esCabezal) relleno = Estilo.AMBAR;
            else if (enBlanco || delimitador) relleno = Estilo.FONDO;
            Color borde = Estilo.LINEA;
            float grosor = 2f;
            if (delimitador) borde = Estilo.CIAN;
            if (esCabezal) {
                borde = Estilo.AMBAR;
                if (resultado == 1) borde = Estilo.OK;
                if (resultado == 2) borde = Estilo.ERROR;
                if (resultado != 0) grosor = 4.5f;
            }
            if (!existe) {                      // casillas "futuras" de la cinta infinita: más tenues
                relleno = Estilo.FONDO;
                borde = Estilo.LINEA.darker();
            }
            g2.setColor(relleno);
            g2.fillRoundRect(x, y0 - alto / 2, ancho, alto, 10, 10);
            g2.setColor(borde);
            g2.setStroke(new BasicStroke(grosor));
            g2.drawRoundRect(x, y0 - alto / 2, ancho, alto, 10, 10);

            // Lo que está escrito en la casilla (si está tachada, primero se dibuja la X y encima la letra)
            boolean marcada = tachados && (c == 'A' || c == 'B' || c == 'C');
            String texto = String.valueOf(c);
            if (marcada) {
                texto = String.valueOf(Character.toLowerCase(c));
                g2.setColor(esCabezal ? Estilo.ERROR.darker() : Estilo.ERROR);
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawLine(x + 6, y0 - alto / 2 + 6, x + ancho - 6, y0 + alto / 2 - 6);
                g2.drawLine(x + ancho - 6, y0 - alto / 2 + 6, x + 6, y0 + alto / 2 - 6);
            }
            if (c == blanco && !acotada) {
                g2.setColor(existe ? Estilo.TEXTO_SUAVE : Estilo.LINEA);
            } else if (delimitador) {
                g2.setColor(Estilo.TEXTO_SUAVE);
            } else {
                g2.setColor(esCabezal ? Estilo.FONDO : Estilo.TEXTO);
            }
            g2.drawString(texto, x + (ancho - fm.stringWidth(texto)) / 2, y0 + fm.getAscent() / 2 - 3);

            // El número de posición debajo de la casilla
            g2.setFont(Estilo.NORMAL.deriveFont(11f));
            g2.setColor(esCabezal ? Estilo.AMBAR : Estilo.TEXTO_SUAVE);
            String numero = String.valueOf(pos);
            g2.drawString(numero, x + (ancho - g2.getFontMetrics().stringWidth(numero)) / 2, y0 + alto / 2 + 17);
            g2.setFont(Estilo.MONO.deriveFont(ancho >= 34 ? 22f : 14f));

            // El cabezal: un triángulo y, encima, el estado activo
            if (esCabezal) {
                int cx = x + ancho / 2;
                int[] xs = {cx - 8, cx + 8, cx};
                int[] ys = {y0 - alto / 2 - 18, y0 - alto / 2 - 18, y0 - alto / 2 - 6};
                g2.setColor(Estilo.AMBAR);
                g2.fillPolygon(xs, ys, 3);

                g2.setFont(Estilo.NEGRITA.deriveFont(15f));
                FontMetrics fe = g2.getFontMetrics();
                int anchoEtiqueta = fe.stringWidth(config.getEstado()) + 24;
                int xe = cx - anchoEtiqueta / 2;
                g2.setColor(Estilo.AMBAR);
                g2.fillRoundRect(xe, y0 - alto / 2 - 48, anchoEtiqueta, 26, 14, 14);
                g2.setColor(Estilo.FONDO);
                g2.drawString(config.getEstado(), xe + 12, y0 - alto / 2 - 29);
                g2.setFont(Estilo.MONO.deriveFont(ancho >= 34 ? 22f : 14f));
            }
        }

        // 3) Adornos: puntos suspensivos (cinta infinita) o una línea (cinta acotada)
        if (!acotada) {
            g2.setFont(Estilo.NEGRITA.deriveFont(22f));
            g2.setColor(Estilo.TEXTO_SUAVE);
            g2.drawString("…", Math.max(4, x0 - 28), y0 + 8);
            g2.drawString("…", Math.min(getWidth() - 24, x0 + anchoTotal + 8), y0 + 8);
        } else {
            int yLinea = y0 + alto / 2 + 34;
            g2.setColor(Estilo.CIAN);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x0, yLinea, x0 + anchoTotal, yLinea);
            g2.drawLine(x0, yLinea - 6, x0, yLinea);
            g2.drawLine(x0 + anchoTotal, yLinea - 6, x0 + anchoTotal, yLinea);
            g2.setFont(Estilo.NORMAL.deriveFont(12f));
            String leyenda = "zona acotada: solo el tamaño de la entrada más los dos delimitadores $";
            g2.drawString(leyenda, (getWidth() - g2.getFontMetrics().stringWidth(leyenda)) / 2, yLinea + 18);
        }
    }
}
