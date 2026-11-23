package nivel0;

import maquina.MaquinaTuring; // Se utiliza la máquina que ejecuta las reglas del nivel 0.


/**
 * Ejercicio del Nivel 0: una máquina que suma 1 a un número binario.
 * Se usa una sola máquina para facilitar la explicación del proyecto.
 */
public class Programas {

    /**
     * La máquina recorre el número hasta el final y suma 1 de derecha a izquierda.
     * Por ejemplo, 1011 se convierte en 1100.
     */
    public static MaquinaTuring incrementoBinario() {
        String[] ejemplos = {"1011", "111", "0", "1000", "0111", "10"};
        MaquinaTuring m = new MaquinaTuring("Sumar 1 a un número binario",
                "Calcula n + 1 con n escrito en binario",
                "01", "01_", '_', false, true, "q0", new String[]{"qf"}, ejemplos);
        m.setFormato("[01]+", "Escribe un número binario (solo 0 y 1). Ejemplo: 1011");
        m.setMaximoEntrada(30);
        m.setTipo("INCREMENTO");
        m.agregarRegla("q0", "01", "q0", '=', 'D', "Busca el último bit: avanza hacia la derecha");
        m.agregarRegla("q0", "_", "q1", '=', 'I', "Llegó al final: retrocede al último bit");
        m.agregarRegla("q1", "1", "q1", '0', 'I', "1 + 1 = 0 y me llevo 1: sigue hacia la izquierda");
        m.agregarRegla("q1", "0", "qf", '1', 'Q', "0 + 1 = 1: termina");
        m.agregarRegla("q1", "_", "qf", '1', 'Q', "Se acabaron los bits con acarreo: escribe un 1 (la cinta crece a la izquierda)");
        return m;
    }

}
