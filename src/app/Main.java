package app;

import javax.swing.*; // ventanas, botones, campos de texto y paneles


/**
 * Inicia el programa y abre la ventana principal.
 */
public class Main {

    // Inicia la interfaz mediante el hilo de eventos de Swing.
    public static void main(String[] args) {
        // Se utiliza el aspecto de Swing para evitar diferencias grandes entre sistemas.
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // Si no se puede aplicar, Swing utiliza su apariencia predeterminada.
        }

        // invokeLater abre la ventana en el hilo que usa Swing para atender la interfaz.
        // La flecha () -> indica las instrucciones que se ejecutan allí.
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}
