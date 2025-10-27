/**
 * Capa de control del patrón MVC - VERSIÓN OPTIMIZADA
 * Coordina la interacción entre modelo, vista y red
 */
package controlador;

import modelo.*;
import vista.PantallaPartida;
import java.util.List;
import java.util.Scanner;

public class ControladorPartida {
    private Partida partida;
    private PantallaPartida vista;
    private Scanner scanner;
    private Ficha ultimaFichaMovida;
    private ControladorRed controladorRed;
    private int jugadorLocalId;
    
    public ControladorPartida(Partida partida, PantallaPartida vista, Scanner scanner, int jugadorLocalId) {
        this.partida = partida;
        this.vista = vista;
        this.scanner = scanner;
        this.ultimaFichaMovida = null;
        this.controladorRed = null;
        this.jugadorLocalId = jugadorLocalId;
    }
    
    public void setControladorRed(ControladorRed controladorRed) {
        this.controladorRed = controladorRed;
        controladorRed.setControladorPartida(this);
    }
    
    public boolean esTurnoLocal() {
        Jugador jugadorActual = partida.getTurnoActual();
        return jugadorActual.getIdJugador() == jugadorLocalId;
    }
    
    public void iniciarTurno() {
        Jugador jugadorActual = partida.getTurnoActual();
        
        // Si hay red y no es mi turno, no hacer nada
        if (controladorRed != null && !esTurnoLocal()) {
            return;
        }
        
        vista.mostrarTurnoActual(jugadorActual);
        
        if (controladorRed != null) {
            System.out.println("\n*** ES TU TURNO ***");
        }
        
        vista.mostrarMensaje("Presiona ENTER para lanzar el dado");
        scanner.nextLine();
        
        lanzarDado();
    }
    
    public void lanzarDado() {
        Jugador jugadorActual = partida.getTurnoActual();
        Dado dado = partida.getDado();
        
        int valorDado = dado.lanzar();
        vista.mostrarResultadoDado(valorDado);
        
        // Enviar tirada de dado por red
        if (controladorRed != null) {
            controladorRed.enviarTiradaDado(valorDado);
        }
        
        List<Ficha> fichasDisponibles = jugadorActual.getFichasDisponibles(valorDado);
        
        if (fichasDisponibles.isEmpty()) {
            vista.mostrarMensaje("No tienes fichas disponibles para mover. Pierdes el turno.");
            aplicarReglasDelTurno(valorDado);
            return;
        }
        
        vista.mostrarMensaje("Fichas disponibles para mover:");
        for (int i = 0; i < fichasDisponibles.size(); i++) {
            Ficha f = fichasDisponibles.get(i);
            String estado = f.isEnCasa() ? "En casa (saldrá a casilla de salida)" : 
                           "Posición actual: " + f.getPosicion();
            System.out.println((i + 1) + ". Ficha " + f.getIdFicha() + " - " + estado);
        }
        
        int seleccion = solicitarSeleccionFicha(fichasDisponibles.size());
        Ficha fichaSeleccionada = fichasDisponibles.get(seleccion - 1);
        
        moverFicha(fichaSeleccionada, valorDado, true);
        aplicarReglasDelTurno(valorDado);
    }
    
    private int solicitarSeleccionFicha(int maxOpciones) {
        int seleccion = -1;
        while (seleccion < 1 || seleccion > maxOpciones) {
            System.out.print("\nSelecciona una ficha (1-" + maxOpciones + "): ");
            try {
                seleccion = scanner.nextInt();
                scanner.nextLine();
                if (seleccion < 1 || seleccion > maxOpciones) {
                    System.out.println("Opción inválida. Intenta de nuevo.");
                }
            } catch (Exception e) {
                System.out.println("Entrada inválida. Ingresa un número.");
                scanner.nextLine();
            }
        }
        return seleccion;
    }
    
    /**
     * Mueve una ficha en el tablero
     */
    public void moverFicha(Ficha ficha, int pasos, boolean enviarPorRed) {
        Jugador jugadorActual = partida.getTurnoActual();
        Tablero tablero = partida.getTablero();
        ReglasJuego reglas = partida.getReglas();
        
        // Sacar ficha con 5
        if (ficha.isEnCasa() && reglas.verificarSacarFichaConCinco(pasos)) {
            ficha.setEnCasa(false);
            int posicionSalida = obtenerPosicionSalida(jugadorActual.getColor());
            ficha.setPosicion(posicionSalida);
            
            Casilla casillaSalida = tablero.getCasilla(posicionSalida);
            casillaSalida.agregarFicha(ficha);
            
            vista.mostrarMensaje("Ficha sacada de casa a la posición " + posicionSalida);
            ultimaFichaMovida = ficha;
            
        } else if (!ficha.isEnCasa()) {
            // Mover ficha normal
            tablero.moverFicha(ficha, pasos);
            vista.actualizarFicha(ficha);
            ultimaFichaMovida = ficha;
        }
        
        // Enviar movimiento por red
        if (enviarPorRed && controladorRed != null) {
            controladorRed.enviarMovimiento(jugadorActual, ficha, pasos);
        }
        
        aplicarReglasDelJuego(ficha);
    }
    
    /**
     * Aplica movimiento recibido desde la red
     */
    public void aplicarMovimientoRemoto(int jugadorId, int fichaId, int pasos) {
        // Buscar jugador
        Jugador jugador = null;
        for (Jugador j : partida.getJugadores()) {
            if (j.getIdJugador() == jugadorId) {
                jugador = j;
                break;
            }
        }
        
        if (jugador == null) {
            System.err.println("[ERROR] Jugador no encontrado: " + jugadorId);
            return;
        }
        
        // Buscar ficha
        Ficha ficha = null;
        for (Ficha f : jugador.getFichas()) {
            if (f.getIdFicha() == fichaId) {
                ficha = f;
                break;
            }
        }
        
        if (ficha == null) {
            System.err.println("[ERROR] Ficha no encontrada: " + fichaId);
            return;
        }
        
        // Aplicar movimiento SIN enviarlo por red (evitar bucle)
        moverFicha(ficha, pasos, false);
    }
    
    private int obtenerPosicionSalida(String color) {
        switch (color.toLowerCase()) {
            case "amarillo": return 5;
            case "azul": return 22;
            case "rojo": return 39;
            case "verde": return 56;
            default: return 0;
        }
    }
    
    public void aplicarReglasDelJuego(Ficha ficha) {
        ReglasJuego reglas = partida.getReglas();
        Tablero tablero = partida.getTablero();
        Jugador jugadorActual = partida.getTurnoActual();
        
        reglas.aplicar(jugadorActual, ficha, tablero);
    }
    
    private void aplicarReglasDelTurno(int valorDado) {
        ReglasJuego reglas = partida.getReglas();
        
        // Turno extra con 6
        if (reglas.verificarTurnoExtra(valorDado)) {
            partida.incrementarContadorSeis();
            
            // Tres 6 seguidos - penalización
            if (reglas.verificarTresSeisSeguidos(partida.getContadorSeis())) {
                vista.mostrarMensaje("¡TRES 6 SEGUIDOS! La última ficha movida regresa a casa.");
                if (ultimaFichaMovida != null && !ultimaFichaMovida.isEnMeta()) {
                    ultimaFichaMovida.regresarACasa();
                    Tablero tablero = partida.getTablero();
                    Casilla casilla = tablero.getCasilla(ultimaFichaMovida.getPosicion());
                    if (casilla != null) {
                        casilla.removerFicha(ultimaFichaMovida);
                    }
                }
                partida.reiniciarContadorSeis();
                partida.cambiarTurno();
                
                if (controladorRed != null) {
                    controladorRed.notificarCambioTurno(partida.getTurnoActual());
                }
            } else {
                vista.mostrarMensaje("¡Sacaste 6! Tienes un turno extra.");
                
                if (controladorRed == null || esTurnoLocal()) {
                    vista.mostrarMensaje("Presiona ENTER para continuar");
                    scanner.nextLine();
                    iniciarTurno();
                }
            }
        } else {
            partida.reiniciarContadorSeis();
            partida.cambiarTurno();
            
            if (controladorRed != null) {
                controladorRed.notificarCambioTurno(partida.getTurnoActual());
            }
        }
    }
    
    /**
     * Aplica cambio de turno recibido desde la red
     */
    public void aplicarCambioTurnoRemoto(int jugadorId) {
        partida.setTurnoActual(jugadorId);
        
        // Mostrar notificación solo si es mi turno
        if (jugadorId == jugadorLocalId) {
            System.out.println("\n>>> ¡Tu turno! Presiona ENTER en el ciclo del juego <<<");
        }
    }
    
    public boolean verificarFinPartida() {
        for (Jugador j : partida.getJugadores()) {
            int fichasEnMeta = 0;
            for (Ficha f : j.getFichas()) {
                if (f.isEnMeta()) {
                    fichasEnMeta++;
                }
            }
            if (fichasEnMeta == 4) {
                vista.mostrarMensaje("¡" + j.getNombre().toUpperCase() + " HA GANADO!");
                partida.finalizarPartida();
                return true;
            }
        }
        return false;
    }
    
    public Partida getPartida() {
        return partida;
    }
}