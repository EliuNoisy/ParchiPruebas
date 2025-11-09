/**
 * Capa de control del patron MVC 
 * Coordina la interaccion entre modelo, vista y red
 * Aplica premios de casillas 
 */
package controlador;

import modelo.*;
import vista.PantallaPartida;
import utilidades.RegistroPartidaJSON;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ControladorPartida {
    private Partida partida;
    private PantallaPartida vista;
    private Scanner scanner;
    private Ficha ultimaFichaMovida;
    private ControladorRed controladorRed;
    private int jugadorLocalId;
    private int casillasPremio;
    private AtomicBoolean procesandoPremio;
    
    public ControladorPartida(Partida partida, PantallaPartida vista, Scanner scanner, int jugadorLocalId) {
        this.partida = partida;
        this.vista = vista;
        this.scanner = scanner;
        this.ultimaFichaMovida = null;
        this.controladorRed = null;
        this.jugadorLocalId = jugadorLocalId;
        this.casillasPremio = 0;
        this.procesandoPremio = new AtomicBoolean(false);
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
        if (procesandoPremio.get()) {
            return;
        }
        
        Jugador jugadorActual = partida.getTurnoActual();
        
        if (controladorRed != null && !esTurnoLocal()) {
            return;
        }
        
        vista.mostrarTurnoActual(jugadorActual);
        
        if (controladorRed != null) {
            System.out.println("\n*** ES TU TURNO ***");
        }
        
        if (casillasPremio > 0) {
            vista.mostrarMensaje("Tienes " + casillasPremio + " casillas de premio para usar!");
            aplicarPremio(jugadorActual);
        } else {
            vista.mostrarMensaje("Presiona ENTER para lanzar el dado");
            scanner.nextLine();
            lanzarDado();
        }
    }
    
    /**
     * Aplica el premio de casillas acumuladas 
     */
    private void aplicarPremio(Jugador jugador) {
        procesandoPremio.set(true);
        List<Ficha> fichasDisponibles = jugador.getFichasEnJuego();
        
        if (fichasDisponibles.isEmpty()) {
            vista.mostrarMensaje("No tienes fichas en juego para usar el premio. Se pierde.");
            casillasPremio = 0;
            procesandoPremio.set(false);
            return;
        }
        
        vista.mostrarMensaje("Selecciona una ficha para avanzar " + casillasPremio + " casillas:");
        for (int i = 0; i < fichasDisponibles.size(); i++) {
            Ficha f = fichasDisponibles.get(i);
            System.out.println((i + 1) + ". Ficha " + f.getIdFicha() + 
                             " - Posicion actual: " + f.getPosicion());
        }
        
        int seleccion = solicitarSeleccionFicha(fichasDisponibles.size());
        Ficha fichaSeleccionada = fichasDisponibles.get(seleccion - 1);
        
        moverFicha(fichaSeleccionada, casillasPremio, true);
        casillasPremio = 0;
        procesandoPremio.set(false);
    }
    
    public void lanzarDado() {
        Jugador jugadorActual = partida.getTurnoActual();
        Dado dado = partida.getDado();
        RegistroPartidaJSON registro = partida.getRegistroJSON();
        
        int valorDado = dado.lanzar();
        vista.mostrarResultadoDado(valorDado);
        
        registro.registrarTiradaDado(jugadorActual.getNombre(), valorDado);
        
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
            String estado = f.isEnCasa() ? "En casa (saldra a casilla de salida)" : 
                           "Posicion actual: " + f.getPosicion();
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
                    System.out.println("Opcion invalida. Intenta de nuevo.");
                }
            } catch (Exception e) {
                System.out.println("Entrada invalida. Ingresa un numero.");
                scanner.nextLine();
            }
        }
        return seleccion;
    }
    
    /**
     * Mueve una ficha en el tablero 
     */
    public void moverFicha(Ficha ficha, int pasos, boolean enviarPorRed) {
        if (ficha == null) {
            vista.mostrarMensaje("Error: Ficha no valida");
            return;
        }
        
        if (ficha.isEnMeta()) {
            vista.mostrarMensaje("Error: La ficha ya esta en meta");
            return;
        }
        
        if (pasos <= 0) {
            vista.mostrarMensaje("Error: Movimiento invalido");
            return;
        }
        
        Jugador jugadorActual = partida.getTurnoActual();
        Tablero tablero = partida.getTablero();
        ReglasJuego reglas = partida.getReglas();
        RegistroPartidaJSON registro = partida.getRegistroJSON();
        
        if (ficha.isEnCasa() && reglas.verificarSacarFichaConCinco(pasos)) {
            ficha.setEnCasa(false);
            int posicionSalida = obtenerPosicionSalida(jugadorActual.getColor());
            ficha.setPosicion(posicionSalida);
            
            Casilla casillaSalida = tablero.getCasilla(posicionSalida);
            casillaSalida.agregarFicha(ficha);
            
            vista.mostrarMensaje("Ficha sacada de casa a la posicion " + posicionSalida);
            ultimaFichaMovida = ficha;
            
            registro.registrarSalidaCasa(jugadorActual.getNombre(), ficha.getIdFicha());
            
        } else if (!ficha.isEnCasa()) {
            int posicionInicial = ficha.getPosicion();
            
            tablero.moverFicha(ficha, pasos);
            vista.actualizarFicha(ficha);
            ultimaFichaMovida = ficha;
            
            registro.registrarMovimiento(
                jugadorActual.getNombre(),
                ficha.getIdFicha(),
                posicionInicial,
                ficha.getPosicion()
            );
        }
        
        if (enviarPorRed && controladorRed != null) {
            controladorRed.enviarMovimiento(jugadorActual, ficha, pasos);
        }
        
        int premio = aplicarReglasDelJuego(ficha);
        if (premio > 0) {
            casillasPremio += premio;
        }
    }
    
    /**
     * Aplica movimiento recibido desde la red con validacion
     */
    public void aplicarMovimientoRemoto(int jugadorId, int fichaId, int pasos) {
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
        
        // Validar que el movimiento remoto sea legal
        if (!validarMovimientoRemoto(ficha, pasos, jugador)) {
            System.err.println("[ERROR] Movimiento remoto invalido rechazado");
            return;
        }
        
        moverFicha(ficha, pasos, false);
    }
    
    /**
     * Valida que un movimiento recibido por red sea legal
     */
    private boolean validarMovimientoRemoto(Ficha ficha, int pasos, Jugador jugador) {
        // Validar rango del dado
        if (pasos < 1 || pasos > 6) {
            return false;
        }
        
        // No se puede mover una ficha en meta
        if (ficha.isEnMeta()) {
            return false;
        }
        
        // Si esta en casa, solo puede salir con 5
        if (ficha.isEnCasa() && pasos != 5) {
            return false;
        }
        
        // Validar que la nueva posicion sea valida
        if (!ficha.isEnCasa()) {
            int nuevaPosicion = ficha.getPosicion() + pasos;
            if (nuevaPosicion < 0) {
                return false;
            }
        }
        
        return true;
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
    
    /**
     * Aplica reglas y retorna casillas de premio
     */
    public int aplicarReglasDelJuego(Ficha ficha) {
        ReglasJuego reglas = partida.getReglas();
        Tablero tablero = partida.getTablero();
        Jugador jugadorActual = partida.getTurnoActual();
        RegistroPartidaJSON registro = partida.getRegistroJSON();
        
        return reglas.aplicar(jugadorActual, ficha, tablero, partida, registro);
    }
    
    private void aplicarReglasDelTurno(int valorDado) {
        ReglasJuego reglas = partida.getReglas();
        RegistroPartidaJSON registro = partida.getRegistroJSON();
        Jugador jugadorActual = partida.getTurnoActual();
        
        if (reglas.verificarTurnoExtra(valorDado)) {
            partida.incrementarContadorSeis();
            
            registro.registrarTurnoExtra(jugadorActual.getNombre());
            
            if (reglas.verificarTresSeisSeguidos(partida.getContadorSeis())) {
                vista.mostrarMensaje("TRES 6 SEGUIDOS! La ultima ficha movida regresa a casa.");
                if (ultimaFichaMovida != null && !ultimaFichaMovida.isEnMeta()) {
                    registro.registrarPenalizacionTresSeis(
                        jugadorActual.getNombre(),
                        ultimaFichaMovida.getIdFicha()
                    );
                    
                    ultimaFichaMovida.regresarACasa();
                    Tablero tablero = partida.getTablero();
                    Casilla casilla = tablero.getCasilla(ultimaFichaMovida.getPosicion());
                    if (casilla != null) {
                        casilla.removerFicha(ultimaFichaMovida);
                    }
                }
                partida.reiniciarContadorSeis();
                casillasPremio = 0;
                
                Jugador jugadorAnterior = partida.getTurnoActual();
                partida.cambiarTurno();
                
                registro.registrarCambioTurno(
                    jugadorAnterior.getNombre(),
                    partida.getTurnoActual().getNombre()
                );
                
                if (controladorRed != null) {
                    controladorRed.notificarCambioTurno(partida.getTurnoActual());
                }
            } else {
                vista.mostrarMensaje("Sacaste 6! Tienes un turno extra.");
                
                if (controladorRed == null || esTurnoLocal()) {
                    vista.mostrarMensaje("Presiona ENTER para continuar");
                    scanner.nextLine();
                    iniciarTurno();
                }
            }
        } else {
            partida.reiniciarContadorSeis();
            
            Jugador jugadorAnterior = partida.getTurnoActual();
            partida.cambiarTurno();
            
            registro.registrarCambioTurno(
                jugadorAnterior.getNombre(),
                partida.getTurnoActual().getNombre()
            );
            
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
        
        if (jugadorId == jugadorLocalId) {
            System.out.println("\n>>> Tu turno! Presiona ENTER en el ciclo del juego <<<");
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
                vista.mostrarMensaje(j.getNombre().toUpperCase() + " HA GANADO!");
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