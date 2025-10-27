/**
 * Representa el dado del juego
 * Genera valores aleatorios entre 1 y 6
 */
package modelo;

import java.util.Random;

public class Dado {
    private int valor;
    private Random random;
    
    /**
     * Constructor del dado
     * Inicializa el generador de numeros aleatorios
     */
    public Dado() {
        this.random = new Random();
        this.valor = 0;
    }
    
    /**
     * Lanza el dado y genera un valor aleatorio
     * @return Valor del dado (1-6)
     */
    public int lanzar() {
        this.valor = random.nextInt(6) + 1;
        return this.valor;
    }
    
    public int getValor() { return valor; }
}