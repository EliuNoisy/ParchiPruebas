/**
 * Representa una ficha del juego 
 * Cada jugador tiene 4 fichas con IDs sincronizados
 */
package modelo;

import java.util.concurrent.atomic.AtomicInteger;

public class Ficha {
    private static AtomicInteger contadorId = new AtomicInteger(0);
    private int idFicha;
    private String color;
    private int posicion;
    private boolean enCasa;
    private boolean enMeta;
    private int jugadorId;
    
    /**
     * Constructor basico de ficha
     * Inicializa la ficha en casa
     * @param color Color de la ficha segun el jugador
     */
    public Ficha(String color) {
        this.idFicha = contadorId.incrementAndGet();
        this.color = color;
        this.posicion = -1;
        this.enCasa = true;
        this.enMeta = false;
    }
    
    /**
     * Constructor con ID de jugador
     */
    public Ficha(String color, int jugadorId) {
        this(color);
        this.jugadorId = jugadorId;
    }
    
    /**
     * Constructor con ID explicito 
     * Para sincronizacion en red
     */
    public Ficha(String color, int jugadorId, int idFicha) {
        this.idFicha = idFicha;
        this.color = color;
        this.jugadorId = jugadorId;
        this.posicion = -1;
        this.enCasa = true;
        this.enMeta = false;
        
        if (idFicha > contadorId.get()) {
            contadorId.set(idFicha);
        }
    }
    
    /**
     * Resetea el contador de IDs
     * IMPORTANTE: Llamar este metodo al inicio del programa
     */
    public static void resetearContador() {
        contadorId.set(0);
    }
    
    /**
     * Obtiene el contador actual 
     * Util para sincronizacion
     */
    public static int getContadorActual() {
        return contadorId.get();
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
    
    public int getIdFicha() { return idFicha; }
    public String getColor() { return color; }
    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }
    public boolean isEnCasa() { return enCasa; }
    public void setEnCasa(boolean enCasa) { this.enCasa = enCasa; }
    public boolean isEnMeta() { return enMeta; }
    public void setEnMeta(boolean enMeta) { this.enMeta = enMeta; }
    public int getJugadorId() { return jugadorId; }
    public void setJugadorId(int jugadorId) { this.jugadorId = jugadorId; }
    
    @Override
    public String toString() {
        return "Ficha{" +
                "id=" + idFicha +
                ", color='" + color + '\'' +
                ", pos=" + posicion +
                ", enCasa=" + enCasa +
                ", enMeta=" + enMeta +
                '}';
    }
}