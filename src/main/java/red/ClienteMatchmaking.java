package red;

import java.io.*;
import java.net.*;

/**
 * Cliente que busca partidas en el servidor de matchmaking
 */
public class ClienteMatchmaking {
    private static final String SERVIDOR_HOST = "localhost";
    private static final int SERVIDOR_PUERTO = 9999;
    
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private boolean conectado;
    
    public boolean conectar() {
        try {
            socket = new Socket(SERVIDOR_HOST, SERVIDOR_PUERTO);
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            entrada = new ObjectInputStream(socket.getInputStream());
            conectado = true;
            System.out.println("[MATCHMAKING] Conectado al servidor");
            return true;
        } catch (IOException e) {
            System.err.println("[MATCHMAKING] Error: " + e.getMessage());
            return false;
        }
    }
    
    public InfoConexion buscarPartida(String nombreJugador) {
        if (!conectado) return null;
        
        try {
            MensajeMatchmaking solicitud = new MensajeMatchmaking(MensajeMatchmaking.Tipo.BUSCAR_PARTIDA);
            solicitud.setNombreJugador(nombreJugador);
            salida.writeObject(solicitud);
            salida.flush();
            
            System.out.println("[MATCHMAKING] Buscando oponente...");
            
            MensajeMatchmaking respuesta = (MensajeMatchmaking) entrada.readObject();
            
            if (respuesta.getTipo() == MensajeMatchmaking.Tipo.EMPAREJADO) {
                System.out.println("[MATCHMAKING] Oponente encontrado: " + respuesta.getNombreOponente());
                
                InfoConexion info = new InfoConexion();
                info.ipOponente = respuesta.getIpOponente();
                info.puertoOponente = respuesta.getPuertoOponente();
                info.nombreOponente = respuesta.getNombreOponente();
                info.rolJugador = respuesta.getRolJugador();
                
                return info;
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[MATCHMAKING] Error: " + e.getMessage());
        }
        
        return null;
    }
    
    public void desconectar() {
        conectado = false;
        try {
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("[MATCHMAKING] Error cerrando: " + e.getMessage());
        }
    }
    
    public static class InfoConexion {
        public String ipOponente;
        public int puertoOponente;
        public String nombreOponente;
        public int rolJugador;
    }
}