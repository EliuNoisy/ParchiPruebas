/**
 * Controla el flujo general de la partida
 * Gestiona jugadores, turnos y estado del juego
 */
package modelo;

import java.util.ArrayList;
import java.util.List;

public class Partida {
    private int idPartida;
    private List<Jugador> jugadores;
    private Tablero tablero;
    private Dado dado;
    private ReglasJuego reglas;
    private Jugador turnoActual;
    private int contadorSeis;
    
    /**
     * Constructor de partida
     * @param id Identificador de la partida
     */
    public Partida(int id) {
        this.idPartida = id;
        this.jugadores = new ArrayList<>();
        this.tablero = new Tablero();
        this.dado = new Dado();
        this.reglas = new ReglasJuego();
        this.contadorSeis = 0;
    }
    
    /**
     * Inicia la partida si hay al menos 2 jugadores
     * Asigna el turno al primer jugador
     */
    public void iniciarPartida() {
        if (jugadores.size() >= 2) {
            System.out.println("\n=== Iniciando Partida de Parchis Star ===");
            System.out.println("Jugadores:");
            for (Jugador j : jugadores) {
                System.out.println("- " + j.getNombre() + " (Color: " + j.getColor() + ")");
            }
            turnoActual = jugadores.get(0);
            turnoActual.setTurno(true);
        }
    }
    
    /**
     * Cambia el turno al siguiente jugador
     * Reinicia el contador de 6 seguidos
     */
    public void cambiarTurno() {
        turnoActual.setTurno(false);
        int indiceActual = jugadores.indexOf(turnoActual);
        int siguienteIndice = (indiceActual + 1) % jugadores.size();
        turnoActual = jugadores.get(siguienteIndice);
        turnoActual.setTurno(true);
        contadorSeis = 0;
    }
    
    /**
     * Finaliza la partida y muestra resultados
     * Cuenta cuantas fichas llego cada jugador a la meta
     */
    public void finalizarPartida() {
        System.out.println("\n============================================");
        System.out.println("|        PARTIDA FINALIZADA                |");
        System.out.println("============================================");
        for (Jugador j : jugadores) {
            int fichasEnMeta = 0;
            for (Ficha f : j.getFichas()) {
                if (f.isEnMeta()) {
                    fichasEnMeta++;
                }
            }
            System.out.println(j.getNombre() + ": " + fichasEnMeta + " fichas en meta");
        }
    }
    
    /**
     * Agrega un jugador a la partida
     * Maximo 4 jugadores permitidos
     * @param jugador Jugador a agregar
     */
    public void agregarJugador(Jugador jugador) {
        if (jugadores.size() < 4) {
            jugadores.add(jugador);
            System.out.println("Jugador " + jugador.getNombre() + " agregado");
        }
    }
    
    // Getters y Setters
    public int getIdPartida() { return idPartida; }
    public List<Jugador> getJugadores() { return jugadores; }
    public Tablero getTablero() { return tablero; }
    public Dado getDado() { return dado; }
    public ReglasJuego getReglas() { return reglas; }
    public Jugador getTurnoActual() { return turnoActual; }
    public int getContadorSeis() { return contadorSeis; }
    public void incrementarContadorSeis() { this.contadorSeis++; }
    public void reiniciarContadorSeis() { this.contadorSeis = 0; }
}