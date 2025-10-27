/**
 * Define y aplica todas las reglas del Parchis Star
 * Incluye: turno extra, comer fichas, barreras, meta, penalizaciones
 * Premios realmente aplicados
 */
package modelo;

import java.util.ArrayList;
import java.util.List;

public class ReglasJuego {
    private String nombre;
    private String descripcion;
    
    /**
     * Constructor de reglas del juego
     */
    public ReglasJuego() {
        this.nombre = "Reglas Parchis Star";
        this.descripcion = "Reglas oficiales del juego Parchis";
    }
    
    /**
     * Verifica si el jugador obtiene turno extra
     * Regla: Al sacar 6 se obtiene un turno adicional
     * @param valorDado Valor obtenido en el dado
     * @return true si saco 6, false si no
     */
    public boolean verificarTurnoExtra(int valorDado) {
        return valorDado == 6;
    }
    
    /**
     * Verifica si puede sacar ficha de casa
     * Regla: Solo con 5 se puede sacar una ficha
     * @param valorDado Valor obtenido en el dado
     * @return true si saco 5, false si no
     */
    public boolean verificarSacarFichaConCinco(int valorDado) {
        return valorDado == 5;
    }
    
    /**
     * Verifica la penalizacion por tres 6 seguidos
     * Regla: Con tres 6 consecutivos la ultima ficha movida regresa a casa
     * @param contadorSeis Numero de 6 consecutivos
     * @return true si llego a 3, false si no
     */
    public boolean verificarTresSeisSeguidos(int contadorSeis) {
        return contadorSeis >= 3;
    }
    
    /**
     * Verifica si la ficha llego a la meta
     * @param ficha Ficha a verificar
     * @return true si llego a meta, false si no
     */
    public boolean verificarMeta(Ficha ficha) {
        return ficha.getPosicion() >= 67 || ficha.isEnMeta();
    }
    
    /**
     * Aplica todas las reglas del juego despues de un movimiento
     * Verifica: comer fichas, barreras, llegada a meta
     * @param jugador Jugador que realizo el movimiento
     * @param ficha Ficha que se movio
     * @param tablero Tablero del juego
     * @return Numero de casillas de premio (0, 10 o 20)
     */
    public int aplicar(Jugador jugador, Ficha ficha, Tablero tablero) {
        int premioCasillas = 0;
        int posicion = ficha.getPosicion();
        Casilla casilla = tablero.getCasilla(posicion);
        
        if (casilla == null || ficha.isEnMeta()) return 0;
        
        // REGLA: Comer fichas en casillas normales
        if (!casilla.esSegura()) {
            List<Ficha> fichasEnCasilla = new ArrayList<>(casilla.getFichas());
            for (Ficha otraFicha : fichasEnCasilla) {
                if (!otraFicha.equals(ficha) && !otraFicha.getColor().equals(ficha.getColor())) {
                    System.out.println("\nFICHA COMIDA! " + jugador.getNombre() + 
                                     " come ficha " + otraFicha.getColor());
                    otraFicha.regresarACasa();
                    casilla.removerFicha(otraFicha);
                    premioCasillas = 20; // PREMIO: 20 casillas
                    System.out.println("PREMIO: +20 casillas para avanzar con otra ficha");
                }
            }
        }
        
        // REGLA: Barrera formada
        if (casilla.verificarBarrera()) {
            System.out.println("BARRERA FORMADA en casilla " + posicion);
        }
        
        // REGLA: Llegada a meta
        if (verificarMeta(ficha)) {
            if (!ficha.isEnMeta()) {
                ficha.llegarMeta();
                casilla.removerFicha(ficha);
                premioCasillas = 10; // PREMIO: 10 casillas
                System.out.println("FICHA EN META! +10 casillas de premio");
            }
        }
        
        return premioCasillas;
    }
}