package red;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Sistema de descubrimiento automatico de jugadores en red local
 * Funciona con multiples instancias 
 */
public class DescubrimientoRed {
    private static final int PUERTO_BROADCAST_MIN = 8888;
    private static final int PUERTO_BROADCAST_MAX = 8898;
    private static final String MENSAJE_BUSQUEDA = "PARCHIS_BUSCAR";
    private static final String MENSAJE_RESPUESTA = "PARCHIS_DISPONIBLE";
    
    private String nombreJugador;
    private int puertoP2P;
    private int puertoPropio;
    private DatagramSocket socketEnvio;
    private DatagramSocket socketEscucha;
    private boolean buscando;
    private Thread hiloRespuesta;
    private List<JugadorEncontrado> jugadoresEncontrados;
    
    public DescubrimientoRed(String nombreJugador, int puertoP2P) {
        this.nombreJugador = nombreJugador;
        this.puertoP2P = puertoP2P;
        this.puertoPropio = encontrarPuertoLibre();
        this.jugadoresEncontrados = new CopyOnWriteArrayList<>();
        this.buscando = false;
    }
    
    /**
     * Encuentra un puerto libre entre el rango especificado
     */
    private int encontrarPuertoLibre() {
        for (int puerto = PUERTO_BROADCAST_MIN; puerto <= PUERTO_BROADCAST_MAX; puerto++) {
            try {
                DatagramSocket test = new DatagramSocket(puerto);
                test.close();
                System.out.println("[DESCUBRIMIENTO] Puerto asignado: " + puerto);
                return puerto;
            } catch (SocketException e) {
                // Puerto ocupado, probar siguiente
            }
        }
        System.err.println("[DESCUBRIMIENTO] No se encontro puerto libre");
        return PUERTO_BROADCAST_MIN;
    }
    
    /**
     * Busca jugadores disponibles en la red local
     */
    public List<JugadorEncontrado> buscarJugadores(int tiempoEspera) {
        jugadoresEncontrados.clear();
        buscando = true;
        
        try {
            socketEnvio = new DatagramSocket();
            socketEnvio.setBroadcast(true);
            
            System.out.println("[DESCUBRIMIENTO] Buscando jugadores en todos los puertos...");
            long inicio = System.currentTimeMillis();
            int intentos = 0;
            
            while (System.currentTimeMillis() - inicio < tiempoEspera * 1000) {
                enviarBroadcastATodos();
                intentos++;
                if (intentos % 4 == 0) {
                    System.out.println("[DESCUBRIMIENTO] Buscando... (" + 
                        jugadoresEncontrados.size() + " encontrados)");
                }
                Thread.sleep(500);
            }
            
            buscando = false;
            socketEnvio.close();
            
            System.out.println("[DESCUBRIMIENTO] Busqueda finalizada. Total: " + 
                jugadoresEncontrados.size());
            
        } catch (Exception e) {
            System.err.println("[DESCUBRIMIENTO] Error: " + e.getMessage());
            buscando = false;
        }
        
        return new ArrayList<>(jugadoresEncontrados);
    }
    
    /**
     * Inicia modo de respuesta automatica
     */
    public void iniciarModoRespuesta() {
        hiloRespuesta = new Thread(() -> {
            try {
                socketEscucha = new DatagramSocket(puertoPropio);
                socketEscucha.setBroadcast(true);
                
                System.out.println("[DESCUBRIMIENTO] Escuchando en puerto " + puertoPropio);
                
                byte[] buffer = new byte[1024];
                
                while (!Thread.currentThread().isInterrupted()) {
                    DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                    socketEscucha.receive(paquete);
                    
                    String mensaje = new String(paquete.getData(), 0, paquete.getLength());
                    
                    if (mensaje.startsWith(MENSAJE_BUSQUEDA)) {
                        procesarBusqueda(mensaje, paquete);
                    } else if (mensaje.startsWith(MENSAJE_RESPUESTA)) {
                        procesarRespuesta(mensaje, paquete.getAddress());
                    }
                }
                
            } catch (Exception e) {
                if (!Thread.currentThread().isInterrupted()) {
                    System.err.println("[DESCUBRIMIENTO] Error escuchando: " + e.getMessage());
                }
            } finally {
                if (socketEscucha != null && !socketEscucha.isClosed()) {
                    socketEscucha.close();
                }
            }
        });
        hiloRespuesta.start();
    }
    
    /**
     * Detiene el modo de respuesta
     */
    public void detenerModoRespuesta() {
        if (hiloRespuesta != null) {
            hiloRespuesta.interrupt();
        }
        if (socketEscucha != null && !socketEscucha.isClosed()) {
            socketEscucha.close();
        }
    }
    
    /**
     * Envia broadcast a todos los puertos posibles
     */
    private void enviarBroadcastATodos() {
        try {
            String mensaje = MENSAJE_BUSQUEDA + ":" + nombreJugador + ":" + puertoPropio;
            byte[] datos = mensaje.getBytes();
            
            // Enviar a cada puerto posible
            for (int puerto = PUERTO_BROADCAST_MIN; puerto <= PUERTO_BROADCAST_MAX; puerto++) {
                if (puerto == puertoPropio) continue; // No enviarse a si mismo
                
                // Broadcast general
                try {
                    InetAddress broadcast = InetAddress.getByName("255.255.255.255");
                    DatagramPacket paquete = new DatagramPacket(datos, datos.length, broadcast, puerto);
                    socketEnvio.send(paquete);
                } catch (Exception e) {
                    // Ignorar errores de broadcast
                }
                
                // Localhost para pruebas locales
                try {
                    InetAddress localhost = InetAddress.getByName("127.0.0.1");
                    DatagramPacket paqueteLocal = new DatagramPacket(datos, datos.length, localhost, puerto);
                    socketEnvio.send(paqueteLocal);
                } catch (Exception e) {
                    // Ignorar errores
                }
            }
            
        } catch (Exception e) {
            System.err.println("[DESCUBRIMIENTO] Error enviando broadcast: " + e.getMessage());
        }
    }
    
    /**
     * Procesa mensaje de busqueda recibido
     */
    private void procesarBusqueda(String mensaje, DatagramPacket paquete) {
        try {
            String[] partes = mensaje.split(":");
            if (partes.length >= 3) {
                String nombreRemoto = partes[1];
                int puertoRemoto = Integer.parseInt(partes[2]);
                
                // No responderse a si mismo
                if (nombreRemoto.equals(nombreJugador)) {
                    return;
                }
                
                // Enviar respuesta directa
                String respuesta = MENSAJE_RESPUESTA + ":" + nombreJugador + ":" + puertoP2P;
                byte[] datosRespuesta = respuesta.getBytes();
                
                DatagramPacket paqueteRespuesta = new DatagramPacket(
                    datosRespuesta, 
                    datosRespuesta.length, 
                    paquete.getAddress(), 
                    puertoRemoto
                );
                
                socketEscucha.send(paqueteRespuesta);
                
                System.out.println("[DESCUBRIMIENTO] Respondido a " + nombreRemoto);
            }
        } catch (Exception e) {
            // Ignorar errores al procesar
        }
    }
    
    /**
     * Procesa respuesta de un jugador encontrado
     */
    private void procesarRespuesta(String mensaje, InetAddress direccion) {
        try {
            String[] partes = mensaje.split(":");
            if (partes.length >= 3) {
                String nombre = partes[1];
                int puerto = Integer.parseInt(partes[2]);
                String ip = direccion.getHostAddress();
                
                // No agregarse a si mismo
                if (nombre.equals(nombreJugador)) {
                    return;
                }
                
                // Verificar si ya existe este jugador (solo por nombre, ignorar IP/puerto duplicados)
                synchronized (jugadoresEncontrados) {
                    for (JugadorEncontrado j : jugadoresEncontrados) {
                        if (j.nombre.equals(nombre)) {
                            return; // Ya existe, no agregar duplicado
                        }
                    }
                    
                    JugadorEncontrado jugador = new JugadorEncontrado(nombre, ip, puerto);
                    jugadoresEncontrados.add(jugador);
                    
                    System.out.println("[DESCUBRIMIENTO] >>> Jugador encontrado: " + nombre + 
                                     " (" + ip + ":" + puerto + ")");
                }
            }
        } catch (Exception e) {
            // Ignorar errores al procesar
        }
    }
    
    /**
     * Clase interna para jugador encontrado
     */
    public static class JugadorEncontrado {
        public String nombre;
        public String ip;
        public int puerto;
        
        public JugadorEncontrado(String nombre, String ip, int puerto) {
            this.nombre = nombre;
            this.ip = ip;
            this.puerto = puerto;
        }
        
        @Override
        public String toString() {
            return nombre + " @ " + ip + ":" + puerto;
        }
    }
}