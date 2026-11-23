package app;

import app.PanelControles.Estilo; // Colores compartidos por los dibujos.


import java.awt.*; // colores, tamaños y formas de organizar los paneles
import javax.swing.JPanel; // Permite dibujar o acomodar elementos en un panel.


/**
 * Dibuja un diagrama de estados. Lo usan el Nivel 3 (AFD) y el Nivel 2 (autómata de pila).
 *   - cada estado es un círculo (doble si es de aceptación),
 *   - cada flecha puede llevar varias líneas de texto (se separan con "\n"),
 *   - el estado actual y la última flecha usada se pintan en ámbar.
 * Esta clase solo DIBUJA: quien la usa le entrega los datos con definir() y mostrar().
 */
// Dibuja estados y flechas con Java2D; los estados se calculan en AFD o AP.
public class DiagramaEstados extends JPanel {

    private static final int R = 28;    // radio de cada estado

    private String leyenda = "";
    private String[] nombres = new String[0];
    private boolean[] aceptacion = new boolean[0];
    private int inicial = 0;
    private int[] origen = new int[0];       // cada flecha k va de origen[k] a destino[k]
    private int[] destino = new int[0];
    private String[] etiquetas = new String[0];
    private double[] posX = null;            // posición de cada estado (de 0 a 1); null = en círculo
    private double[] posY = null;

    private int estadoActual = 0;
    private int aristaActiva = -1;           // flecha que se acaba de usar (-1 = ninguna)
    private int resultado = 0;               // 0 = en proceso, 1 = aceptada, 2 = rechazada

    // Prepara el área donde se dibujan los estados y las flechas.
    public DiagramaEstados() {
        setBackground(Estilo.PANEL);
    }

    /** Entrega la forma del autómata (esto cambia solo cuando se elige otro lenguaje). */
    public void definir(String leyenda, String[] nombres, boolean[] aceptacion, int inicial,
                        int[] origen, int[] destino, String[] etiquetas, double[] posX, double[] posY) {
        this.leyenda = leyenda;
        this.nombres = nombres;
        this.aceptacion = aceptacion;
        this.inicial = inicial;
        this.origen = origen;
        this.destino = destino;
        this.etiquetas = etiquetas;
        this.posX = posX;
        this.posY = posY;
    }

    /** Entrega lo que cambia en cada paso: el estado actual, la flecha usada y si ya hay resultado. */
    public void mostrar(int estadoActual, int aristaActiva, int resultado) {
        this.estadoActual = estadoActual;
        this.aristaActiva = aristaActiva;
        this.resultado = resultado;
        repaint();
    }

    @Override
    // Dibuja de nuevo este panel cuando la ventana se actualiza.
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int n = nombres.length;
        if (n == 0) return;
        // Graphics2D permite dibujar los estados y las flechas.
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1) Posición de cada estado
        int[] px = new int[n];
        int[] py = new int[n];
        for (int i = 0; i < n; i++) {
            if (posX != null) {
                px[i] = (int) (90 + posX[i] * (getWidth() - 180));
                py[i] = (int) (posY[i] * getHeight());
            } else {                         // los estados se reparten en una elipse
                double rx = Math.max(70, Math.min(getWidth() / 2.0 - 120, 320));
                double ry = Math.max(70, Math.min(getHeight() / 2.0 - 120, 190));
                double angulo = Math.PI + 2 * Math.PI * i / n;
                px[i] = (int) (getWidth() / 2.0 + rx * Math.cos(angulo));
                py[i] = (int) (getHeight() / 2.0 + ry * Math.sin(angulo));
            }
        }

        // 2) Las flechas
        for (int k = 0; k < origen.length; k++) {
            boolean activa = (k == aristaActiva);
            if (origen[k] == destino[k]) {
                dibujarBucle(g2, px[origen[k]], py[origen[k]], etiquetas[k], activa);
            } else {
                boolean hayVuelta = false;       // ¿también hay una flecha en sentido contrario?
                for (int j = 0; j < origen.length; j++) {
                    if (origen[j] == destino[k] && destino[j] == origen[k]) hayVuelta = true;
                }
                dibujarFlecha(g2, px[origen[k]], py[origen[k]], px[destino[k]], py[destino[k]], hayVuelta, etiquetas[k], activa);
            }
        }

        // 3) La flecha de entrada al estado inicial (llega desde la izquierda)
        g2.setColor(Estilo.TEXTO);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(px[inicial] - R - 46, py[inicial], px[inicial] - R, py[inicial]);
        puntaDeFlecha(g2, px[inicial] - R, py[inicial], 1, 0);

        // 4) Los estados
        for (int i = 0; i < n; i++) {
            dibujarEstado(g2, i, px[i], py[i]);
        }

        // 5) Textos
        g2.setFont(Estilo.NEGRITA.deriveFont(15f));
        g2.setColor(Estilo.TEXTO);
        g2.drawString("Diagrama de estados", 18, 28);
        g2.setFont(Estilo.NORMAL.deriveFont(12f));
        g2.setColor(Estilo.TEXTO_SUAVE);
        g2.drawString(leyenda, 18, getHeight() - 14);
    }

    // Dibuja un estado del autómata y lo resalta si está activo.
    private void dibujarEstado(Graphics2D g2, int i, int x, int y) {
        boolean actual = (i == estadoActual);
        g2.setColor(actual ? Estilo.AMBAR : Estilo.PANEL_CLARO);
        g2.fillOval(x - R, y - R, 2 * R, 2 * R);

        // El borde cambia de color al terminar la cadena: verde si se aceptó y rojo si se rechazó
        if (actual && resultado != 0) {
            g2.setColor(resultado == 1 ? Estilo.OK : Estilo.ERROR);
            g2.setStroke(new BasicStroke(4.5f));
        } else {
            g2.setColor(Estilo.CIAN);
            g2.setStroke(new BasicStroke(2.2f));
        }
        g2.drawOval(x - R, y - R, 2 * R, 2 * R);

        if (aceptacion[i]) {                 // un segundo círculo adentro = estado de aceptación
            g2.setStroke(new BasicStroke(1.8f));
            g2.setColor(actual ? Estilo.FONDO : Estilo.CIAN);
            g2.drawOval(x - R + 5, y - R + 5, 2 * (R - 5), 2 * (R - 5));
        }

        g2.setFont(Estilo.NEGRITA.deriveFont(16f));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(actual ? Estilo.FONDO : Estilo.TEXTO);
        g2.drawString(nombres[i], x - fm.stringWidth(nombres[i]) / 2, y + fm.getAscent() / 2 - 2);
    }

    /** Flecha entre dos estados distintos. Si también existe la de vuelta, se separan un poco las dos. */
    private void dibujarFlecha(Graphics2D g2, int x1, int y1, int x2, int y2, boolean hayVuelta,
                               String etiqueta, boolean activa) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double largo = Math.sqrt(dx * dx + dy * dy);
        double ux = dx / largo;              // vector de largo 1 en la dirección de la flecha
        double uy = dy / largo;
        double perpX = -uy;                  // vector perpendicular
        double perpY = ux;
        double desvio = hayVuelta ? 9 : 0;

        int sx = (int) (x1 + ux * R + perpX * desvio);
        int sy = (int) (y1 + uy * R + perpY * desvio);
        int ex = (int) (x2 - ux * R + perpX * desvio);
        int ey = (int) (y2 - uy * R + perpY * desvio);

        g2.setColor(activa ? Estilo.AMBAR : Estilo.TEXTO_SUAVE);
        g2.setStroke(new BasicStroke(activa ? 3.2f : 1.8f));
        g2.drawLine(sx, sy, ex, ey);
        puntaDeFlecha(g2, ex, ey, ux, uy);

        // El texto va al lado de la flecha (debajo si va hacia la derecha)
        int mx = (sx + ex) / 2 + (int) (perpX * (hayVuelta ? 24 : 20));
        int my = (sy + ey) / 2 + (int) (perpY * (hayVuelta ? 24 : 20));
        escribirLineas(g2, etiqueta, mx, my, activa, false);
    }

    /** Flecha de un estado hacia sí mismo (el "bucle"): un círculo pequeño encima del estado. */
    private void dibujarBucle(Graphics2D g2, int x, int y, String etiqueta, boolean activa) {
        int radio = 16;
        int cy = y - R - 8;
        g2.setColor(activa ? Estilo.AMBAR : Estilo.TEXTO_SUAVE);
        g2.setStroke(new BasicStroke(activa ? 3.2f : 1.8f));
        g2.drawOval(x - radio, cy - radio, 2 * radio, 2 * radio);
        puntaDeFlecha(g2, x + 12, y - R + 3, -0.43, 0.9);
        escribirLineas(g2, etiqueta, x, cy - radio - 8, activa, true);
    }

    /** Triángulo con la punta en (x, y) apuntando hacia (ux, uy), que debe ser un vector de largo 1. */
    private void puntaDeFlecha(Graphics2D g2, int x, int y, double ux, double uy) {
        double largo = 11;
        double ancho = 5.5;
        double baseX = x - ux * largo;
        double baseY = y - uy * largo;
        int[] xs = {x, (int) (baseX - uy * ancho), (int) (baseX + uy * ancho)};
        int[] ys = {y, (int) (baseY + ux * ancho), (int) (baseY - ux * ancho)};
        g2.fillPolygon(xs, ys, 3);
    }

    /**
     * Escribe un texto de varias líneas (separadas con "\n") centrado en x.
     * Si "haciaArriba" es true, la última línea queda en y y las demás se apilan encima.
     * Si es false, el bloque queda centrado verticalmente en y.
     */
    private void escribirLineas(Graphics2D g2, String texto, int x, int y, boolean activa, boolean haciaArriba) {
        g2.setFont(Estilo.MONO_PEQ.deriveFont(java.awt.Font.BOLD, 13f));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(activa ? Estilo.AMBAR : Estilo.CIAN);
        String[] lineas = texto.split("\n");
        for (int i = 0; i < lineas.length; i++) {
            int centroY;
            if (haciaArriba) {
                centroY = y - (lineas.length - 1 - i) * 17;
            } else {
                centroY = y - (lineas.length - 1) * 17 / 2 + i * 17;
            }
            g2.drawString(lineas[i], x - fm.stringWidth(lineas[i]) / 2, centroY + fm.getAscent() / 2 - 2);
        }
    }
}
