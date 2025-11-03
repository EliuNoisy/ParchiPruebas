package red;

import java.io.*;
import java.net.*;

/**
 * Representa una conexin con otro jugador (peer)
 */
public class ConexionPeer extends Thread {
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private P2PNetworkManager gestor;
    private boolean conectado;
    private String nombrePeer;
    
    public ConexionPeer(Socket socket, P2PNetworkManager gestor) {
        this.socket = socket;
        this.gestor = gestor;
        this.conectado = true;
        this.nombrePeer = "Desconocido";
        
        try {
            // Importante: crear ObjectOutputStream antes que ObjectInputStream
            this.salida = new ObjectOutputStream(socket.getOutputStream());
            this.salida.flush();
            this.entrada = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("[RED] Error inicializando streams: " + e.getMessage());
            conectado = false;
        }
    }
    
    @Override
    public void run() {
        System.out.println("[RED] Iniciando escucha de mensajes del peer...");
        
        while (conectado) {
            try {
                MensajeJuego mensaje = (MensajeJuego) entrada.readObject();
                
                // Si es saludo, guardar nombre del peer
                if (mensaje.getTipo() == MensajeJuego.TipoMensaje.SALUDO) {
                    nombrePeer = mensaje.getEmisor();
                    System.out.println("[RED] Peer identificado como: " + nombrePeer);
                }
                
                gestor.alRecibirMensaje(mensaje, this);
                
            } catch (EOFException e) {
                System.out.println("[RED] Conexion cerrada por el peer");
                break;
            } catch (IOException | ClassNotFoundException e) {
                if (conectado) {
                    System.err.println("[RED] Error recibiendo mensaje: " + e.getMessage());
                }
                break;
            }
        }
        
        cerrar();
        gestor.alDesconectarPeer(this);
    }
    
    /**
     * Envía un mensaje a este peer
     */
    public synchronized boolean enviarMensaje(MensajeJuego mensaje) {
        if (!conectado || salida == null) {
            return false;
        }
        
        try {
            salida.writeObject(mensaje);
            salida.flush();
            return true;
        } catch (IOException e) {
            System.err.println("[RED] Error enviando mensaje: " + e.getMessage());
            conectado = false;
            return false;
        }
    }
    
    /**
     * Cierra la conexión con este peer
     */
    public void cerrar() {
        conectado = false;
        
        try {
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("[RED] Error cerrando conexion: " + e.getMessage());
        }
    }
    
    public boolean estaConectado() {
        return conectado && socket != null && !socket.isClosed();
    }
    
    public String getNombrePeer() {
        return nombrePeer;
    }
    
    public String getDireccion() {
        return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }
}