package red;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Gestor de red P2P para el juego de Parchís
 * Maneja conexiones entre jugadores y sincronización de eventos
 */
public class P2PNetworkManager {
    private String nombreJugador;
    private int puerto;
    private ServerSocket servidorSocket;
    private List<ConexionPeer> peers;
    private ExecutorService ejecutorServicio;
    private boolean estaActivo;
    private EscuchaRed escucha;
    
    public P2PNetworkManager(String nombreJugador, int puerto) {
        this.nombreJugador = nombreJugador;
        this.puerto = puerto;
        this.peers = new CopyOnWriteArrayList<>();
        this.ejecutorServicio = Executors.newCachedThreadPool();
        this.estaActivo = false;
    }
    
    /**
     * Inicia el servidor P2P para aceptar conexiones
     */
    public void iniciarServidor() throws IOException {
        if (estaActivo) return;
        
        servidorSocket = new ServerSocket(puerto);
        estaActivo = true;
        
        ejecutorServicio.execute(() -> {
            System.out.println("[RED] Servidor P2P iniciado en puerto " + puerto);
            while (estaActivo) {
                try {
                    Socket socketCliente = servidorSocket.accept();
                    System.out.println("[RED] Nueva conexión desde: " + socketCliente.getInetAddress());
                    manejarNuevaConexion(socketCliente);
                } catch (IOException e) {
                    if (estaActivo) {
                        System.err.println("[RED] Error aceptando conexión: " + e.getMessage());
                    }
                }
            }
        });
    }
    
    /**
     * Conecta con otro jugador (peer)
     */
    public boolean conectarAPeer(String host, int puerto) {
        try {
            Socket socket = new Socket(host, puerto);
            ConexionPeer peer = new ConexionPeer(socket, this);
            
            // Enviar información de presentación
            peer.enviarMensaje(new MensajeJuego(
                MensajeJuego.TipoMensaje.SALUDO,
                nombreJugador,
                "Conexión establecida"
            ));
            
            peers.add(peer);
            peer.start();
            
            System.out.println("[RED] Conectado exitosamente a " + host + ":" + puerto);
            return true;
        } catch (IOException e) {
            System.err.println("[RED] Error conectando a peer: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Maneja una nueva conexión entrante
     */
    private void manejarNuevaConexion(Socket socket) {
        ConexionPeer peer = new ConexionPeer(socket, this);
        peers.add(peer);
        peer.start();
    }
    
    /**
     * Envía un mensaje a todos los jugadores conectados
     */
    public void difundir(MensajeJuego mensaje) {
        for (ConexionPeer peer : peers) {
            peer.enviarMensaje(mensaje);
        }
    }
    
    /**
     * Envía un movimiento de ficha
     */
    public void enviarMovimiento(int jugadorId, int fichaId, int dado) {
        MensajeJuego mensaje = new MensajeJuego(
            MensajeJuego.TipoMensaje.MOVIMIENTO,
            nombreJugador,
            String.format("jugador:%d,ficha:%d,dado:%d", jugadorId, fichaId, dado)
        );
        difundir(mensaje);
    }
    
    /**
     * Envía cambio de turno
     */
    public void enviarCambioTurno(int proximoJugadorId) {
        MensajeJuego mensaje = new MensajeJuego(
            MensajeJuego.TipoMensaje.CAMBIO_TURNO,
            nombreJugador,
            String.valueOf(proximoJugadorId)
        );
        difundir(mensaje);
    }
    
    /**
     * Envía mensaje de chat
     */
    public void enviarMensajeChat(String texto) {
        MensajeJuego mensaje = new MensajeJuego(
            MensajeJuego.TipoMensaje.CHAT,
            nombreJugador,
            texto
        );
        difundir(mensaje);
    }
    
    /**
     * Envía tirada de dado
     */
    public void enviarTiradaDado(int valor) {
        MensajeJuego mensaje = new MensajeJuego(
            MensajeJuego.TipoMensaje.TIRADA_DADO,
            nombreJugador,
            String.valueOf(valor)
        );
        difundir(mensaje);
    }
    
    /**
     * Procesa mensaje recibido de un peer
     */
    void alRecibirMensaje(MensajeJuego mensaje, ConexionPeer desde) {
        if (escucha != null) {
            escucha.alRecibirMensaje(mensaje, desde);
        }
    }
    
    /**
     * Notifica desconexión de un peer
     */
    void alDesconectarPeer(ConexionPeer peer) {
        peers.remove(peer);
        if (escucha != null) {
            escucha.alDesconectarPeer(peer);
        }
        System.out.println("[RED] Peer desconectado. Peers activos: " + peers.size());
    }
    
    /**
     * Obtiene lista de peers conectados
     */
    public List<ConexionPeer> getPeersConectados() {
        return new ArrayList<>(peers);
    }
    
    /**
     * Establece el escucha para eventos de red
     */
    public void setEscuchaRed(EscuchaRed escucha) {
        this.escucha = escucha;
    }
    
    /**
     * Cierra todas las conexiones y detiene el servidor
     */
    public void cerrar() {
        estaActivo = false;
        
        // Cerrar todas las conexiones con peers
        for (ConexionPeer peer : peers) {
            peer.cerrar();
        }
        peers.clear();
        
        // Cerrar servidor
        try {
            if (servidorSocket != null && !servidorSocket.isClosed()) {
                servidorSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[RED] Error cerrando servidor: " + e.getMessage());
        }
        
        ejecutorServicio.shutdown();
        System.out.println("[RED] Red P2P cerrada");
    }
    
    public String getNombreJugador() {
        return nombreJugador;
    }
    
    public int getPuerto() {
        return puerto;
    }
    
    public boolean estaActivo() {
        return estaActivo;
    }
}