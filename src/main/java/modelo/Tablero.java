/**
 * Representa el tablero completo del juego
 * Contiene 68 casillas en total
 */
package modelo;

import java.util.ArrayList;
import java.util.List;

public class Tablero {
    private List<Casilla> casillas;
    private static final int TOTAL_CASILLAS = 68;
    
    /**
     * Constructor del tablero
     * Inicializa todas las casillas
     */
    public Tablero() {
        this.casillas = new ArrayList<>();
        inicializarTablero();
    }
    
    /**
     * Inicializa el tablero con casillas normales y seguras
     * Casillas seguras: 5, 22, 39, 56 (salidas) y cada 17 posiciones
     */
    private void inicializarTablero() {
        for (int i = 0; i < TOTAL_CASILLAS; i++) {
            String tipo = "normal";
            
            // Casillas seguras en posiciones estrategicas
            if (i == 5 || i == 22 || i == 39 || i == 56 || i % 17 == 0) {
                tipo = "segura";
            }
            
            casillas.add(new Casilla(i, tipo));
        }
    }
    
    /**
     * Mueve una ficha en el tablero
     * Actualiza la posicion y maneja llegada a meta
     * @param ficha Ficha a mover
     * @param pasos Numero de casillas a avanzar
     */
    public void moverFicha(Ficha ficha, int pasos) {
        int posicionActual = ficha.getPosicion();
        int nuevaPosicion = posicionActual + pasos;
        
        // Control de meta (casilla 67 es la ultima antes de meta)
        if (nuevaPosicion >= TOTAL_CASILLAS) {
            nuevaPosicion = TOTAL_CASILLAS - 1;
            ficha.llegarMeta();
        }
        
        // Remover de casilla actual
        if (posicionActual >= 0 && posicionActual < casillas.size()) {
            casillas.get(posicionActual).removerFicha(ficha);
        }
        
        ficha.setPosicion(nuevaPosicion);
        
        // Agregar a nueva casilla (solo si no llego a meta)
        if (!ficha.isEnMeta() && nuevaPosicion >= 0 && nuevaPosicion < casillas.size()) {
            casillas.get(nuevaPosicion).agregarFicha(ficha);
        }
    }
    
    /**
     * Obtiene una casilla especifica del tablero
     * @param posicion Numero de casilla (0-67)
     * @return La casilla o null si la posicion es invalida
     */
    public Casilla getCasilla(int posicion) {
        if (posicion >= 0 && posicion < casillas.size()) {
            return casillas.get(posicion);
        }
        return null;
    }
    
    // Getters
    public List<Casilla> getCasillas() { return casillas; }
    public int getTotalCasillas() { return TOTAL_CASILLAS; }
}