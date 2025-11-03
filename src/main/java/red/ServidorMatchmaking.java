package red;

import red.MensajeMatchmaking;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Servidor que empareja jugadores automaticamente
 * EJECUTAR PRIMERO en terminal separada
 */
public class ServidorMatchmaking {
    private static final int PUERTO = 9999;
    private ServerSocket servidorSocket;
    private ExecutorService ejecutor;
    private BlockingQueue<JugadorEnEspera> colaEspera;
    private boolean activo;
    
    public ServidorMatchmaking() {
        this.ejecutor = Executors.newCachedThreadPool();
        this.colaEspera = new LinkedBlockingQueue<>();
        this.activo = true;
    }
    
    public void iniciar() {
        try {
            servidorSocket = new ServerSocket(PUERTO);
            System.out.println("\n================================================");
            System.out.println("  SERVIDOR DE MATCHMAKING INICIADO");
            System.out.println("  Puerto: " + PUERTO);
            System.out.println("  Esperando jugadores...");
            System.out.println("================================================\n");
            
            ejecutor.execute(this::procesarEmparejamientos);
            
            while (activo) {
                try {
                    Socket socketJugador = servidorSocket.accept();
                    System.out.println("[SERVIDOR] Nueva conexion: " + 
                                     socketJugador.getInetAddress().getHostAddress());
                    ejecutor.execute(() -> manejarJugador(socketJugador));
                } catch (IOException e) {
                    if (activo) {
                        System.err.println("[SERVIDOR] Error: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("[SERVIDOR] Error iniciando: " + e.getMessage());
        }
    }
    
    private void manejarJugador(Socket socket) {
        try {
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            
            MensajeMatchmaking solicitud = (MensajeMatchmaking) entrada.readObject();
            
            if (solicitud.getTipo() == MensajeMatchmaking.Tipo.BUSCAR_PARTIDA) {
                String nombreJugador = solicitud.getNombreJugador();
                String ip = socket.getInetAddress().getHostAddress();
                
                System.out.println("[SERVIDOR] Jugador '" + nombreJugador + "' buscando partida");
                
                JugadorEnEspera jugador = new JugadorEnEspera(nombreJugador, ip, socket, entrada, salida);
                colaEspera.put(jugador);
            }
            
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            System.err.println("[SERVIDOR] Error: " + e.getMessage());
        }
    }
    
    private void procesarEmparejamientos() {
        while (activo) {
            try {
                if (colaEspera.size() >= 2) {
                    JugadorEnEspera jugador1 = colaEspera.take();
                    JugadorEnEspera jugador2 = colaEspera.take();
                    
                    System.out.println("\n[SERVIDOR] Emparejamiento:");
                    System.out.println("  Jugador 1: " + jugador1.nombre);
                    System.out.println("  Jugador 2: " + jugador2.nombre);
                    
                    emparejar(jugador1, jugador2);
                } else {
                    Thread.sleep(500);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void emparejar(JugadorEnEspera j1, JugadorEnEspera j2) {
        try {
            int puertoJ1 = 5000 + (int)(Math.random() * 1000);
            
            MensajeMatchmaking respuestaJ1 = new MensajeMatchmaking(MensajeMatchmaking.Tipo.EMPAREJADO);
            respuestaJ1.setRolJugador(1);
            respuestaJ1.setNombreOponente(j2.nombre);
            respuestaJ1.setPuertoOponente(puertoJ1);
            respuestaJ1.setIpOponente(j1.ip);
            j1.salida.writeObject(respuestaJ1);
            j1.salida.flush();
            
            MensajeMatchmaking respuestaJ2 = new MensajeMatchmaking(MensajeMatchmaking.Tipo.EMPAREJADO);
            respuestaJ2.setRolJugador(2);
            respuestaJ2.setNombreOponente(j1.nombre);
            respuestaJ2.setPuertoOponente(puertoJ1);
            respuestaJ2.setIpOponente(j1.ip);
            j2.salida.writeObject(respuestaJ2);
            j2.salida.flush();
            
            System.out.println("[SERVIDOR] Informacion enviada");
            System.out.println("================================================\n");
            
            j1.socket.close();
            j2.socket.close();
            
        } catch (IOException e) {
            System.err.println("[SERVIDOR] Error emparejando: " + e.getMessage());
        }
    }
    
    private static class JugadorEnEspera {
        String nombre;
        String ip;
        Socket socket;
        ObjectInputStream entrada;
        ObjectOutputStream salida;
        
        JugadorEnEspera(String nombre, String ip, Socket socket, 
                       ObjectInputStream entrada, ObjectOutputStream salida) {
            this.nombre = nombre;
            this.ip = ip;
            this.socket = socket;
            this.entrada = entrada;
            this.salida = salida;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("\n================================================");
        System.out.println("  SERVIDOR DE MATCHMAKING - PARCHIS STAR");
        System.out.println("================================================\n");
        
        ServidorMatchmaking servidor = new ServidorMatchmaking();
        servidor.iniciar();
    }
}
