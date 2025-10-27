package controlador;

import modelo.*;
import red.*;

/**
 * Controlador que integra la red P2P con la lógica del juego
 */
public class ControladorRed implements EscuchaRed {
    private P2PNetworkManager gestorRed;
    private Tablero tablero;
    private Jugador jugadorLocal;
    private boolean esAnfitrion;
    private ControladorPartida controladorPartida;
    
    public ControladorRed(String nombreJugador, int puerto, Tablero tablero) {
        this.gestorRed = new P2PNetworkManager(nombreJugador, puerto);
        this.tablero = tablero;
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
            boolean conectado = gestorRed.conectarAPeer(ipAnfitrion, puertoAnfitrion);
            
            if (conectado) {
                esAnfitrion = false;
                System.out.println("[RED] Unido a la partida exitosamente");
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("[RED] Error uniéndose a la partida: " + e.getMessage());
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
        gestorRed.enviarMovimiento(jugadorId, fichaId, valorDado);
    }
    
    /**
     * Notifica cambio de turno
     */
    public void notificarCambioTurno(Jugador siguienteJugador) {
        int jugadorId = siguienteJugador.getIdJugador();
        gestorRed.enviarCambioTurno(jugadorId);
    }
    
    /**
     * Envía tirada de dado
     */
    public void enviarTiradaDado(int valor) {
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
        System.out.println("[RED] Mensaje recibido: " + mensaje);
        
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
            String[] partes = mensaje.getContenido().split(",");
            int jugadorId = Integer.parseInt(partes[0].split(":")[1]);
            int fichaId = Integer.parseInt(partes[1].split(":")[1]);
            int dado = Integer.parseInt(partes[2].split(":")[1]);
            
            System.out.println("[RED] Aplicando movimiento remoto: Jugador " + jugadorId + 
                             ", Ficha " + fichaId + ", Dado " + dado);
            
            // Aquí deberías aplicar el movimiento en tu tablero local
            // Necesitarás una referencia al controlador de partida o a los jugadores
            
        } catch (Exception e) {
            System.err.println("[RED] Error procesando movimiento: " + e.getMessage());
        }
    }
    
    /**
     * Procesa cambio de turno
     */
    private void procesarCambioTurno(MensajeJuego mensaje) {
        try {
            int jugadorId = Integer.parseInt(mensaje.getContenido());
            System.out.println("[RED] Turno del jugador: " + jugadorId);
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
        System.out.println("[CHAT] " + mensaje.getEmisor() + ": " + mensaje.getContenido());
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
}