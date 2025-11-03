
/**
 * Responsable de mostrar informacion al usuario
 * Visualizacion clara y organizada
 */
package vista;

import java.util.List;
import modelo.Ficha;
import modelo.Jugador;
import modelo.Partida;

public class PantallaPartida {
    
    /**
     * Limpia la pantalla (simula limpieza en consola)
     */
    private void limpiarPantalla() {
        for (int i = 0; i < 2; i++) {
            System.out.println();
        }
    }
    
    /**
     * Muestra el estado completo del tablero
     * Incluye todas las fichas de todos los jugadores
     * @param partida Partida actual
     */
    public void mostrarTablero(Partida partida) {
        limpiarPantalla();
        
        System.out.println("==========================================");
        System.out.println("        PARCHIS STAR - TABLERO");
        System.out.println("==========================================");
        
        for (Jugador jugador : partida.getJugadores()) {
            mostrarJugadorCompacto(jugador);
        }
        
        System.out.println("==========================================\n");
    }
    
    /**
     * Muestra informacion de un jugador de forma compacta
     */
    private void mostrarJugadorCompacto(Jugador jugador) {
        // Contar estados
        int enCasa = 0, enJuego = 0, enMeta = 0;
        
        for (Ficha ficha : jugador.getFichas()) {
            if (ficha.isEnCasa()) enCasa++;
            else if (ficha.isEnMeta()) enMeta++;
            else enJuego++;
        }
        
        // Mostrar resumen
        System.out.println("\n" + jugador.getNombre() + " (" + jugador.getColor() + ")");
        System.out.println("  Casa: " + enCasa + "  |  Tablero: " + enJuego + "  |  Meta: " + enMeta);
        
        // Mostrar posiciones solo si hay fichas en juego
        if (enJuego > 0) {
            System.out.print("  Posiciones: ");
            for (Ficha ficha : jugador.getFichas()) {
                if (!ficha.isEnCasa() && !ficha.isEnMeta()) {
                    System.out.print("Ficha-" + ficha.getIdFicha() + " en [" + ficha.getPosicion() + "]  ");
                }
            }
            System.out.println();
        }
    }
    
    /**
     * Muestra el resultado del lanzamiento de dado
     * @param valor Valor obtenido (1-6)
     */
    public void mostrarResultadoDado(int valor) {
        System.out.println("\n------------------------------------------");
        System.out.println("           DADO LANZADO: " + valor);
        System.out.println("------------------------------------------");
    }
    
    /**
     * Muestra informacion del jugador en turno
     * Incluye fichas en casa y en juego
     * @param jugador Jugador actual
     */
    public void mostrarTurnoActual(Jugador jugador) {
        System.out.println("\n==========================================");
        System.out.println("  >>> TURNO DE: " + jugador.getNombre().toUpperCase() + " <<<");
        System.out.println("  Color: " + jugador.getColor());
        System.out.println("==========================================");
        
        List<Ficha> fichasCasa = jugador.getFichasEnCasa();
        List<Ficha> fichasJuego = jugador.getFichasEnJuego();
        
        System.out.println("\nEstado actual:");
        System.out.println("  - Fichas en casa: " + fichasCasa.size());
        System.out.println("  - Fichas jugando: " + fichasJuego.size());
        
        if (!fichasJuego.isEmpty()) {
            System.out.println("\nTus fichas en el tablero:");
            for (Ficha ficha : fichasJuego) {
                System.out.println("  * Ficha " + ficha.getIdFicha() + 
                                 " -> Casilla " + ficha.getPosicion());
            }
        }
        System.out.println();
    }
    
    /**
     * Actualiza y muestra el estado de una ficha especifica
     * @param ficha Ficha a mostrar
     */
    public void actualizarFicha(Ficha ficha) {
        String estado = ficha.isEnCasa() ? "Casa" : 
                       ficha.isEnMeta() ? "META" : 
                       "Casilla " + ficha.getPosicion();
        System.out.println("  -> Ficha " + ficha.getIdFicha() + 
                         " (" + ficha.getColor() + "): " + estado);
    }
    
    /**
     * Muestra un mensaje general al usuario
     * @param mensaje Texto a mostrar
     */
    public void mostrarMensaje(String mensaje) {
        System.out.println("\n  " + mensaje);
    }
}