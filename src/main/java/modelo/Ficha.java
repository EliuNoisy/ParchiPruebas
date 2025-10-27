/**
 * Representa una ficha del juego
 * Cada jugador tiene 4 fichas
 */
package modelo;

public class Ficha {
    private static int contadorId = 0;
    private int idFicha;
    private String color;
    private int posicion;
    private boolean enCasa;
    private boolean enMeta;
    
    /**
     * Constructor de ficha
     * Inicializa la ficha en casa
     * @param color Color de la ficha segun el jugador
     */
    public Ficha(String color) {
        this.idFicha = ++contadorId;
        this.color = color;
        this.posicion = -1;
        this.enCasa = true;
        this.enMeta = false;
    }
    
    /**
     * Mueve la ficha un numero determinado de pasos
     * Solo funciona si la ficha esta en juego
     * @param pasos Numero de casillas a avanzar
     */
    public void mover(int pasos) {
        if (!enCasa && !enMeta) {
            posicion += pasos;
        }
    }
    
    /**
     * Regresa la ficha a la casa
     * Se usa cuando la ficha es comida
     */
    public void regresarACasa() {
        this.posicion = -1;
        this.enCasa = true;
        this.enMeta = false;
    }
    
    /**
     * Marca la ficha como llegada a la meta
     * La ficha ya no puede moverse
     */
    public void llegarMeta() {
        this.enMeta = true;
        this.enCasa = false;
    }
    
    // Getters y Setters
    public int getIdFicha() { return idFicha; }
    public String getColor() { return color; }
    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }
    public boolean isEnCasa() { return enCasa; }
    public void setEnCasa(boolean enCasa) { this.enCasa = enCasa; }
    public boolean isEnMeta() { return enMeta; }
    public void setEnMeta(boolean enMeta) { this.enMeta = enMeta; }
}