package nivel2;

import app.PanelControles.Estilo; // clase de otra carpeta del proyecto
import java.awt.*; // colores, tamaños y formas de organizar los paneles
import java.awt.event.*; // eventos del mouse y los botones
import java.util.ArrayList; // Se usa para guardar los pasos, reglas o estados en una lista.
import javax.swing.*; // ventanas, botones, campos de texto y paneles


/**
 * Dibuja la pila como una columna de fichas y la ANIMA:
 *   - una ficha que se apila baja desde arriba y va apareciendo (verde),
 *   - una ficha que se desapila sube y se va desvaneciendo (rojo).
 * La animación dura menos de medio segundo. Un Timer repite el dibujo cada 16 milisegundos,
 * subiendo la variable "progreso" de 0 a 1. Cuando llega a 1, se detiene.
 */
// Dibuja la pila de abajo hacia arriba. Al cambiar el paso, se redibuja su contenido.
public class PanelPila extends JPanel implements ActionListener {

    private ArrayList<String> antes = new ArrayList<String>();     // la pila antes del paso (de abajo hacia arriba)
    private ArrayList<String> despues = new ArrayList<String>();   // la pila después del paso
    private float progreso = 1f;      // 0 = empieza la animación, 1 = terminó
    private int resultado = 0;        // 0 = en proceso, 1 = aceptada, 2 = rechazada
    private Timer animador;

    // Prepara la representación de la pila.
    public PanelPila() {
        antes.add(AP.FONDO);
        despues.add(AP.FONDO);
        setBackground(Estilo.PANEL);
        setPreferredSize(new Dimension(260, 100));
        // El Timer actualiza el dibujo de la pila cada 16 ms para mostrar su movimiento.
        animador = new Timer(16, this);
    }

    /**
     * Muestra la pila. Si "animar" es true, pasa de pilaAntes a pilaDespues con la animación;
     * si es false, salta directo a pilaDespues (por ejemplo al retroceder o al reiniciar).
     */
    public void mostrar(ArrayList<String> pilaAntes, ArrayList<String> pilaDespues, boolean animar, int resultado) {
        animador.stop();
        antes = new ArrayList<String>(pilaAntes);
        despues = new ArrayList<String>(pilaDespues);
        this.resultado = resultado;
        if (animar) {
            progreso = 0f;
            animador.start();
        } else {
            progreso = 1f;
        }
        repaint();
    }

    /** El Timer llama a este método cada 16 milisegundos. */
    @Override
    public void actionPerformed(ActionEvent e) {
        progreso = progreso + 16f / 380f;
        if (progreso >= 1f) {
            progreso = 1f;
            animador.stop();
        }
        repaint();
    }

    @Override
    // Dibuja de nuevo este panel cuando la ventana se actualiza.
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setFont(Estilo.NEGRITA.deriveFont(15f));
        g2.setColor(Estilo.TEXTO);
        g2.drawString("Pila (memoria LIFO)", 18, 28);
        g2.setFont(Estilo.NORMAL.deriveFont(12f));
        g2.setColor(Estilo.TEXTO_SUAVE);
        g2.drawString("El último en entrar es el primero en salir", 18, 48);

        int n = Math.max(antes.size(), despues.size());
        int piso = getHeight() - 52;                            // la línea donde se apoya la pila
        int alto = Math.max(16, Math.min(40, (piso - 90) / Math.max(n, 6)));
        int ancho = Math.min(120, getWidth() - 120);
        int x = 36;
        boolean animando = progreso < 1f;

        g2.setColor(Estilo.LINEA);
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(x - 12, piso, x + ancho + 12, piso);
        g2.setFont(Estilo.NORMAL.deriveFont(11f));
        g2.setColor(Estilo.TEXTO_SUAVE);
        g2.drawString("fondo", x - 12, piso + 16);

        // Las fichas de abajo que no cambian (donde las dos pilas son iguales)
        int comun = 0;
        while (comun < antes.size() && comun < despues.size() && antes.get(comun).equals(despues.get(comun))) {
            comun++;
        }
        for (int k = 0; k < comun; k++) {
            boolean tope = !animando && k == despues.size() - 1;
            if (tope) {
                ficha(g2, x, piso, alto, ancho, k, antes.get(k), 1f, 0, Estilo.AMBAR, Estilo.AMBAR, Estilo.FONDO);
            } else {
                ficha(g2, x, piso, alto, ancho, k, antes.get(k), 1f, 0, Estilo.PANEL_CLARO, Estilo.CIAN, Estilo.TEXTO);
            }
        }
        if (animando) {                      // las fichas que se quitan: suben y se desvanecen
            for (int k = comun; k < antes.size(); k++) {
                ficha(g2, x, piso, alto, ancho, k, antes.get(k), 1f - progreso, (int) (-progreso * 30),
                        Estilo.PANEL_CLARO, Estilo.ERROR, Estilo.TEXTO);
            }
        }
        for (int k = comun; k < despues.size(); k++) {      // las fichas que se ponen: bajan y aparecen
            boolean tope = !animando && k == despues.size() - 1;
            if (animando) {
                ficha(g2, x, piso, alto, ancho, k, despues.get(k), progreso, (int) (-(1f - progreso) * 46),
                        Estilo.PANEL_CLARO, Estilo.OK, Estilo.TEXTO);
            } else if (tope) {
                ficha(g2, x, piso, alto, ancho, k, despues.get(k), 1f, 0, Estilo.AMBAR, Estilo.AMBAR, Estilo.FONDO);
            } else {
                ficha(g2, x, piso, alto, ancho, k, despues.get(k), 1f, 0, Estilo.PANEL_CLARO, Estilo.CIAN, Estilo.TEXTO);
            }
        }

        // La marca del tope
        if (!animando && !despues.isEmpty()) {
            int yTope = piso - despues.size() * alto + alto / 2;
            g2.setFont(Estilo.NEGRITA.deriveFont(13f));
            g2.setColor(Estilo.AMBAR);
            g2.drawString("◀ tope", x + ancho + 14, yTope + 5);
        }

        // Pila vacía
        if (!animando && despues.isEmpty()) {
            g2.setFont(Estilo.NEGRITA.deriveFont(16f));
            g2.setColor(resultado == 1 ? Estilo.OK : Estilo.TEXTO_SUAVE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("PILA VACÍA", x + (ancho - fm.stringWidth("PILA VACÍA")) / 2, piso - 24);
        }

        g2.setFont(Estilo.NORMAL.deriveFont(12f));
        g2.setColor(Estilo.TEXTO_SUAVE);
        String fichas = (despues.size() == 1) ? " ficha" : " fichas";
        g2.drawString("Altura: " + despues.size() + fichas, 18, getHeight() - 14);
    }

    /**
     * Dibuja una ficha de la pila. "k" es su posición contando desde el fondo (0 = la de más abajo).
     * "transparencia" va de 0 (invisible) a 1 (sólida); "desplazamiento" mueve la ficha hacia arriba (negativo) o abajo.
     */
    private void ficha(Graphics2D g2, int x, int piso, int alto, int ancho, int k, String texto,
                       float transparencia, int desplazamiento, Color relleno, Color borde, Color colorTexto) {
        int alfa = (int) (255 * transparencia);
        int y = piso - (k + 1) * alto + desplazamiento;
        g2.setColor(Estilo.conAlfa(relleno, alfa));
        g2.fillRoundRect(x, y + 2, ancho, alto - 4, 10, 10);
        g2.setColor(Estilo.conAlfa(borde, alfa));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y + 2, ancho, alto - 4, 10, 10);
        g2.setFont(Estilo.MONO.deriveFont(alto >= 30 ? 20f : 14f));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Estilo.conAlfa(colorTexto, alfa));
        g2.drawString(texto, x + (ancho - fm.stringWidth(texto)) / 2, y + (alto + fm.getAscent()) / 2 - 3);
    }
}
