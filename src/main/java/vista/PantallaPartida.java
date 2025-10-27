/**
 * Capa de presentacion del patron MVC
 * Responsable de mostrar informacion al usuario
 */
package vista;

import java.util.List;
import modelo.Ficha;
import modelo.Jugador;
import modelo.Partida;

public class PantallaPartida {
    
    /**
     * Muestra el estado completo del tablero
     * Incluye todas las fichas de todos los jugadores
     * @param partida Partida actual
     */
    public void mostrarTablero(Partida partida) {
        System.out.println("\n============================================");
        System.out.println("           TABLERO PARCHIS STAR");
        System.out.println("============================================");
        
        for (Jugador jugador : partida.getJugadores()) {
            System.out.println("\n" + jugador.getNombre() + " (" + jugador.getColor() + "):");
            for (Ficha ficha : jugador.getFichas()) {
                String estado;
                if (ficha.isEnCasa()) {
                    estado = "En casa";
                } else if (ficha.isEnMeta()) {
                    estado = "EN META";
                } else {
                    estado = "Casilla " + ficha.getPosicion();
                }
                System.out.println("  Ficha " + ficha.getIdFicha() + ": " + estado);
            }
        }
        System.out.println("============================================");
    }
    
    /**
     * Muestra el resultado del lanzamiento de dado
     * @param valor Valor obtenido (1-6)
     */
    public void mostrarResultadoDado(int valor) {
        System.out.println("\nDado lanzado: " + valor);
        System.out.println("============================================");
    }
    
    /**
     * Muestra informacion del jugador en turno
     * Incluye fichas en casa y en juego
     * @param jugador Jugador actual
     */
    public void mostrarTurnoActual(Jugador jugador) {
        System.out.println("\n============================================");
        System.out.println("       TURNO DE: " + jugador.getNombre().toUpperCase());
        System.out.println("============================================");
        System.out.println("Color: " + jugador.getColor());
        
        List<Ficha> fichasCasa = jugador.getFichasEnCasa();
        List<Ficha> fichasJuego = jugador.getFichasEnJuego();
        
        System.out.println("Fichas en casa: " + fichasCasa.size());
        System.out.println("Fichas en juego: " + fichasJuego.size());
        
        if (!fichasJuego.isEmpty()) {
            System.out.println("\nPosiciones actuales:");
            for (Ficha ficha : fichasJuego) {
                System.out.println("  Ficha " + ficha.getIdFicha() + 
                                 " -> Casilla " + ficha.getPosicion());
            }
        }
    }
    
    /**
     * Actualiza y muestra el estado de una ficha especifica
     * @param ficha Ficha a mostrar
     */
    public void actualizarFicha(Ficha ficha) {
        String estado = ficha.isEnCasa() ? "Casa" : 
                       ficha.isEnMeta() ? "META" : 
                       "Casilla " + ficha.getPosicion();
        System.out.println("Ficha " + ficha.getIdFicha() + 
                         " (" + ficha.getColor() + "): " + estado);
    }
    
    /**
     * Muestra un mensaje general al usuario
     * @param mensaje Texto a mostrar
     */
    public void mostrarMensaje(String mensaje) {
        System.out.println("\n" + mensaje);
    }
}