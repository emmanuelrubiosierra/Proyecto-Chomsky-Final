package nivel1;

import maquina.MaquinaTuring; // clase de otra carpeta del proyecto


/**
 * Prepara las reglas del nivel 1 para comprobar a^n b^n c^n.
 * Las mayúsculas marcan letras revisadas y los $ limitan la cinta.
 */
public class LBAs {

    /**
     * L = { aⁿbⁿcⁿ | n ≥ 1 }. Es el LBA de la tarea del tercer corte:
     * en cada vuelta tacha una a, una b y una c; al final revisa que todo esté tachado.
     * q0, q1 y q2 son los estados A, B y C de la tarea; q3 es el regreso al inicio y q4 la revisión final.
     */
    public static MaquinaTuring anbncn() {
        String[] ejemplos = {"abc", "aabbcc", "aaabbbccc", "aabbc", "abbc", "abcabc", "acb", ""};
        MaquinaTuring m = new MaquinaTuring("aⁿbⁿcⁿ  (en orden)", "L = { aⁿbⁿcⁿ | n ≥ 1 }",
                "abc", "abcABC$", '_', true, false, "q0", new String[]{"qa"}, ejemplos);
        m.setTachados(true);
        m.agregarRegla("q0", "A", "q0", '=', 'D', "Salta una a que ya está tachada");
        m.agregarRegla("q0", "a", "q1", 'A', 'D', "Tacha la primera a sin tachar y va a buscar la b");
        m.agregarRegla("q0", "B", "q4", '=', 'D', "Ya no quedan a sin tachar: pasa a revisar lo que queda");
        m.agregarRegla("q1", "aB", "q1", '=', 'D', "Sigue a la derecha: salta a sin tachar y b tachadas");
        m.agregarRegla("q1", "b", "q2", 'B', 'D', "Tacha la primera b sin tachar y va a buscar la c");
        m.agregarRegla("q2", "bC", "q2", '=', 'D', "Sigue a la derecha: salta b sin tachar y c tachadas");
        m.agregarRegla("q2", "c", "q3", 'C', 'I', "Tacha la primera c sin tachar y empieza a regresar al $ izquierdo");
        m.agregarRegla("q3", "abcABC", "q3", '=', 'I', "Regresa hacia la izquierda");
        m.agregarRegla("q3", "$", "q0", '=', 'D', "Llegó al $ izquierdo: queda sobre la primera letra y empieza otra vuelta");
        m.agregarRegla("q4", "BC", "q4", '=', 'D', "Revisa: todo lo que sigue está tachado");
        m.agregarRegla("q4", "$", "qa", '=', 'Q', "Llegó al $ derecho y todo está tachado: ACEPTA");
        return m;
    }


}
