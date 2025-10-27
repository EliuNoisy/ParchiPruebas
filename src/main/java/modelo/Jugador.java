/**
 * Representa un jugador del parchis
 * Cada jugador tiene 4 fichas y un color asignado
 */
package modelo;

import java.util.ArrayList;
import java.util.List;

public class Jugador {
    private int idJugador;
    private String nombre;
    private String avatar;
    private String color;
    private List<Ficha> fichas;
    private boolean turno;
    
    /**
     * Constructor del jugador
     * Crea automaticamente 4 fichas del color asignado
     * @param id Identificador unico del jugador
     * @param nombre Nombre del jugador
     * @param color Color asignado (Amarillo, Azul, Rojo, Verde)
     */
    public Jugador(int id, String nombre, String color) {
        this.idJugador = id;
        this.nombre = nombre;
        this.color = color;
        this.fichas = new ArrayList<>();
        this.turno = false;
        
        // Crear 4 fichas
        for (int i = 0; i < 4; i++) {
            fichas.add(new Ficha(color));
        }
    }
    
    /**
     * Selecciona una ficha por su indice
     * @param indice Posicion de la ficha en la lista (0-3)
     * @return La ficha seleccionada o null si el indice es invalido
     */
    public Ficha seleccionarFicha(int indice) {
        if (indice >= 0 && indice < fichas.size()) {
            return fichas.get(indice);
        }
        return null;
    }
    
    /**
     * Obtiene las fichas que pueden moverse segun el valor del dado
     * Con 5 puede sacar fichas de casa
     * Con otros valores solo mueve fichas en juego
     * @param valorDado Valor obtenido en el dado (1-6)
     * @return Lista de fichas que pueden moverse
     */
    public List<Ficha> getFichasDisponibles(int valorDado) {
        List<Ficha> disponibles = new ArrayList<>();
        
        // Si saca 5, puede sacar una ficha de casa
        if (valorDado == 5) {
            for (Ficha ficha : fichas) {
                if (ficha.isEnCasa()) {
                    disponibles.add(ficha);
                    break; // Solo una ficha puede salir por turno
                }
            }
        }
        
        // Fichas en juego siempre pueden moverse
        for (Ficha ficha : fichas) {
            if (!ficha.isEnCasa() && !ficha.isEnMeta()) {
                disponibles.add(ficha);
            }
        }
        
        return disponibles;
    }
    
    /**
     * Obtiene todas las fichas que estan en casa
     * @return Lista de fichas en casa
     */
    public List<Ficha> getFichasEnCasa() {
        List<Ficha> enCasa = new ArrayList<>();
        for (Ficha ficha : fichas) {
            if (ficha.isEnCasa()) {
                enCasa.add(ficha);
            }
        }
        return enCasa;
    }
    
    /**
     * Obtiene todas las fichas que estan en el tablero
     * @return Lista de fichas en juego
     */
    public List<Ficha> getFichasEnJuego() {
        List<Ficha> enJuego = new ArrayList<>();
        for (Ficha ficha : fichas) {
            if (!ficha.isEnCasa() && !ficha.isEnMeta()) {
                enJuego.add(ficha);
            }
        }
        return enJuego;
    }
    
    // Getters y Setters
    public int getIdJugador() { return idJugador; }
    public String getNombre() { return nombre; }
    public String getColor() { return color; }
    public List<Ficha> getFichas() { return fichas; }
    public boolean isTurno() { return turno; }
    public void setTurno(boolean turno) { this.turno = turno; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
