package controlador;

import modelo.*;
import red.*;

/**
 * Controlador que integra la red P2P con la lógica del juego - CORREGIDO
 */
public class ControladorRed implements EscuchaRed {
    private P2PNetworkManager gestorRed;
    private Tablero tablero;
    private int jugadorLocalId;
    private boolean esAnfitrion;
    private ControladorPartida controladorPartida;
    private int ultimoDadoRemoto = 0; // Almacena el último valor del dado remoto
    
    public ControladorRed(String nombreJugador, int puerto, Tablero tablero, int jugadorLocalId) {
        this.gestorRed = new P2PNetworkManager(nombreJugador, puerto);
        this.tablero = tablero;
        this.jugadorLocalId = jugadorLocalId;
        this.gestorRed.setEscuchaRed(this);
    }
    
    /**
     * Inicia como anfitrión (crea la partida)
     */
    public void iniciarComoAnfitrion() throws Exception {
        esAnfitrion = true;
        gestorRed.iniciarServidor();
        System.out.println("[RED] Servidor iniciado. Esperando jugadores...");
    }
    
    /**
     * Se une a una partida existente
     */
    public boolean unirseAPartida(String ipAnfitrion, int puertoAnfitrion) {
        try {
            gestorRed.iniciarServidor(); // También inicia servidor para recibir mensajes
            
            // Esperar un momento para que el servidor se inicie
            Thread.sleep(500);
            
            boolean conectado = gestorRed.conectarAPeer(ipAnfitrion, puertoAnfitrion);
            
            if (conectado) {
                esAnfitrion = false;
                System.out.println("[RED] Unido a la partida exitosamente");
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("[RED] Error uniéndose a la partida: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Conecta con otro peer (para juego multi-peer)
     */
    public boolean conectarAPeer(String host, int puerto) {
        return gestorRed.conectarAPeer(host, puerto);
    }
    
    /**
     * Envía un movimiento de ficha a todos los peers
     */
    public void enviarMovimiento(Jugador jugador, Ficha ficha, int valorDado) {
        int jugadorId = jugador.getIdJugador();
        int fichaId = ficha.getIdFicha();
        System.out.println("[RED] Enviando movimiento: J" + jugadorId + " F" + fichaId + " D" + valorDado);
        gestorRed.enviarMovimiento(jugadorId, fichaId, valorDado);
    }
    
    /**
     * Notifica cambio de turno
     */
    public void notificarCambioTurno(Jugador siguienteJugador) {
        int jugadorId = siguienteJugador.getIdJugador();
        System.out.println("[RED] Notificando cambio de turno al Jugador " + jugadorId);
        gestorRed.enviarCambioTurno(jugadorId);
    }
    
    /**
     * Envía tirada de dado
     */
    public void enviarTiradaDado(int valor) {
        System.out.println("[RED] Enviando tirada de dado: " + valor);
        gestorRed.enviarTiradaDado(valor);
    }
    
    /**
     * Envía mensaje de chat
     */
    public void enviarMensajeChat(String texto) {
        gestorRed.enviarMensajeChat(texto);
    }
    
    @Override
    public void alRecibirMensaje(MensajeJuego mensaje, ConexionPeer desde) {
        System.out.println("[RED] ← Mensaje recibido de " + mensaje.getEmisor() + ": " + mensaje.getTipo());
        
        switch (mensaje.getTipo()) {
            case MOVIMIENTO:
                procesarMovimiento(mensaje);
                break;
                
            case CAMBIO_TURNO:
                procesarCambioTurno(mensaje);
                break;
                
            case TIRADA_DADO:
                procesarTiradaDado(mensaje);
                break;
                
            case CHAT:
                procesarChat(mensaje);
                break;
                
            case SALUDO:
                System.out.println("[RED] Nuevo jugador conectado: " + mensaje.getEmisor());
                break;
                
            case JUGADOR_SALE:
                System.out.println("[RED] Jugador se fue: " + mensaje.getEmisor());
                break;
                
            default:
                System.out.println("[RED] Tipo de mensaje no manejado: " + mensaje.getTipo());
        }
    }
    
    @Override
    public void alDesconectarPeer(ConexionPeer peer) {
        System.out.println("[RED] Peer desconectado: " + peer.getNombrePeer());
        System.out.println("[RED] El juego puede continuar con los jugadores restantes");
    }
    
    /**
     * Procesa un movimiento recibido
     */
    private void procesarMovimiento(MensajeJuego mensaje) {
        try {
            // Formato: "jugador:1,ficha:2,dado:6"
            String[] partes = mensaje.getContenido().split(",");
            int jugadorId = Integer.parseInt(partes[0].split(":")[1]);
            int fichaId = Integer.parseInt(partes[1].split(":")[1]);
            int dado = Integer.parseInt(partes[2].split(":")[1]);
            
            System.out.println("[RED] Procesando movimiento: Jugador " + jugadorId + 
                             ", Ficha " + fichaId + ", Dado " + dado);
            
            // Solo aplicar si NO es movimiento propio
            if (jugadorId != jugadorLocalId) {
                if (controladorPartida != null) {
                    controladorPartida.aplicarMovimientoRemoto(jugadorId, fichaId, dado);
                } else {
                    System.err.println("[RED] Error: ControladorPartida no está configurado");
                }
            } else {
                System.out.println("[RED] Ignorando movimiento propio (eco)");
            }
            
        } catch (Exception e) {
            System.err.println("[RED] Error procesando movimiento: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Procesa cambio de turno
     */
    private void procesarCambioTurno(MensajeJuego mensaje) {
        try {
            int jugadorId = Integer.parseInt(mensaje.getContenido());
            System.out.println("[RED] Procesando cambio de turno al Jugador: " + jugadorId);
            
            if (controladorPartida != null) {
                controladorPartida.aplicarCambioTurnoRemoto(jugadorId);
                
                // Mostrar notificación visual
                if (jugadorId == jugadorLocalId) {
                    System.out.println("\n╔════════════════════════════════════╗");
                    System.out.println("║       ¡ES TU TURNO!                ║");
                    System.out.println("╚════════════════════════════════════╝");
                } else {
                    System.out.println("\n--- Turno del otro jugador ---");
                }
            }
        } catch (Exception e) {
            System.err.println("[RED] Error procesando cambio de turno: " + e.getMessage());
        }
    }
    
    /**
     * Procesa tirada de dado
     */
    private void procesarTiradaDado(MensajeJuego mensaje) {
        int valor = Integer.parseInt(mensaje.getContenido());
        System.out.println("[RED] " + mensaje.getEmisor() + " tiró el dado: " + valor);
    }
    
    /**
     * Procesa mensaje de chat
     */
    private void procesarChat(MensajeJuego mensaje) {
        System.out.println("\n[CHAT] " + mensaje.getEmisor() + ": " + mensaje.getContenido());
    }
    
    public P2PNetworkManager getGestorRed() {
        return gestorRed;
    }
    
    public void setControladorPartida(ControladorPartida controlador) {
        this.controladorPartida = controlador;
    }
    
    public void cerrar() {
        gestorRed.cerrar();
    }
    
    public boolean esAnfitrion() {
        return esAnfitrion;
    }
    
    public int getJugadorLocalId() {
        return jugadorLocalId;
    }
}