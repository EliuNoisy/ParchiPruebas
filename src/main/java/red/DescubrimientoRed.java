package red;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Sistema de descubrimiento automatico mejorado
 * Funciona en: misma laptop, 2 laptops, VM, netbeans + cmd
 */
public class DescubrimientoRed {
    private static final int PUERTO_BROADCAST_MIN = 8888;
    private static final int PUERTO_BROADCAST_MAX = 8898;
    private static final String MENSAJE_BUSQUEDA = "PARCHIS_BUSCAR";
    private static final String MENSAJE_RESPUESTA = "PARCHIS_DISPONIBLE";
    
    private String nombreJugador;
    private int puertoP2P;
    private int puertoPropio;
    private long timestamp;
    private DatagramSocket socketEnvio;
    private DatagramSocket socketEscucha;
    private boolean buscando;
    private Thread hiloRespuesta;
    private List<JugadorEncontrado> jugadoresEncontrados;
    
    public DescubrimientoRed(String nombreJugador, int puertoP2P) {
        this.nombreJugador = nombreJugador;
        this.puertoP2P = puertoP2P;
        this.puertoPropio = encontrarPuertoLibre();
        this.timestamp = System.currentTimeMillis();
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
        System.err.println("[DESCUBRIMIENTO] No se encontro puerto libre, usando default");
        return PUERTO_BROADCAST_MIN;
    }
    
    /**
     * Busca jugadores disponibles en la red local - MEJORADO
     * Tiempo aumentado a 5 segundos para mayor confiabilidad
     */
    public List<JugadorEncontrado> buscarJugadores(int tiempoEspera) {
        jugadoresEncontrados.clear();
        buscando = true;
        
        try {
            socketEnvio = new DatagramSocket();
            socketEnvio.setBroadcast(true);
            socketEnvio.setSoTimeout(500);
            
            System.out.println("[DESCUBRIMIENTO] Iniciando busqueda activa...");
            System.out.println("[DESCUBRIMIENTO] Tu timestamp: " + timestamp);
            
            long inicio = System.currentTimeMillis();
            int intentos = 0;
            
            while (System.currentTimeMillis() - inicio < tiempoEspera * 1000) {
                enviarBroadcastMejorado();
                intentos++;
                
                if (intentos % 6 == 0) {
                    int restantes = (int)((tiempoEspera * 1000 - (System.currentTimeMillis() - inicio)) / 1000);
                    System.out.println("[DESCUBRIMIENTO] Buscando... " + 
                        jugadoresEncontrados.size() + " encontrados | " +
                        restantes + "s restantes");
                }
                
                Thread.sleep(500);
            }
            
            buscando = false;
            socketEnvio.close();
            
            System.out.println("[DESCUBRIMIENTO] Busqueda finalizada");
            System.out.println("[DESCUBRIMIENTO] Total encontrados: " + jugadoresEncontrados.size());
            
            if (!jugadoresEncontrados.isEmpty()) {
                for (JugadorEncontrado j : jugadoresEncontrados) {
                    System.out.println("  - " + j.nombre + " @ " + j.ip + ":" + j.puerto + 
                                     " (timestamp: " + j.timestamp + ")");
                }
            }
            
        } catch (Exception e) {
            System.err.println("[DESCUBRIMIENTO] Error: " + e.getMessage());
            buscando = false;
        }
        
        return new ArrayList<>(jugadoresEncontrados);
    }
    
    /**
     * Inicia modo de respuesta automatica - MEJORADO
     */
    public void iniciarModoRespuesta() {
        hiloRespuesta = new Thread(() -> {
            try {
                socketEscucha = new DatagramSocket(puertoPropio);
                socketEscucha.setBroadcast(true);
                socketEscucha.setSoTimeout(100);
                
                System.out.println("[DESCUBRIMIENTO] Modo respuesta activo en puerto " + puertoPropio);
                System.out.println("[DESCUBRIMIENTO] Esperando senales...");
                
                byte[] buffer = new byte[2048];
                
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                        socketEscucha.receive(paquete);
                        
                        String mensaje = new String(paquete.getData(), 0, paquete.getLength());
                        
                        if (mensaje.startsWith(MENSAJE_BUSQUEDA)) {
                            procesarBusquedaMejorada(mensaje, paquete);
                        } else if (mensaje.startsWith(MENSAJE_RESPUESTA)) {
                            procesarRespuestaMejorada(mensaje, paquete.getAddress());
                        }
                    } catch (SocketTimeoutException e) {
                        // Timeout normal, continuar
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
            try {
                hiloRespuesta.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (socketEscucha != null && !socketEscucha.isClosed()) {
            socketEscucha.close();
        }
        System.out.println("[DESCUBRIMIENTO] Modo respuesta detenido");
    }
    
    /**
     * Envia broadcast mejorado con timestamp para sincronizacion
     */
    private void enviarBroadcastMejorado() {
        try {
            String mensaje = MENSAJE_BUSQUEDA + ":" + nombreJugador + ":" + 
                           puertoPropio + ":" + puertoP2P + ":" + timestamp;
            byte[] datos = mensaje.getBytes();
            
            // Broadcast a todos los puertos posibles
            for (int puerto = PUERTO_BROADCAST_MIN; puerto <= PUERTO_BROADCAST_MAX; puerto++) {
                if (puerto == puertoPropio) continue;
                
                // Broadcast general (red local)
                try {
                    InetAddress broadcast = InetAddress.getByName("255.255.255.255");
                    DatagramPacket paquete = new DatagramPacket(datos, datos.length, broadcast, puerto);
                    socketEnvio.send(paquete);
                } catch (Exception e) {
                    // Ignorar errores de broadcast
                }
                
                // Localhost (mismo equipo, VM, WSL)
                try {
                    InetAddress localhost = InetAddress.getByName("127.0.0.1");
                    DatagramPacket paqueteLocal = new DatagramPacket(datos, datos.length, localhost, puerto);
                    socketEnvio.send(paqueteLocal);
                } catch (Exception e) {
                    // Ignorar errores
                }
                
                // Direccion local (para VM con bridge)
                try {
                    InetAddress localHost = InetAddress.getLocalHost();
                    DatagramPacket paqueteLocalHost = new DatagramPacket(datos, datos.length, localHost, puerto);
                    socketEnvio.send(paqueteLocalHost);
                } catch (Exception e) {
                    // Ignorar errores
                }
            }
            
        } catch (Exception e) {
            System.err.println("[DESCUBRIMIENTO] Error enviando broadcast: " + e.getMessage());
        }
    }
    
    /**
     * Procesa mensaje de busqueda con timestamp - MEJORADO
     */
    private void procesarBusquedaMejorada(String mensaje, DatagramPacket paquete) {
        try {
            String[] partes = mensaje.split(":");
            if (partes.length >= 5) {
                String nombreRemoto = partes[1];
                int puertoRemoto = Integer.parseInt(partes[2]);
                int puertoP2PRemoto = Integer.parseInt(partes[3]);
                long timestampRemoto = Long.parseLong(partes[4]);
                
                // No responderse a si mismo
                if (nombreRemoto.equals(nombreJugador)) {
                    return;
                }
                
                System.out.println("[DESCUBRIMIENTO] Recibida busqueda de: " + nombreRemoto + 
                                 " (timestamp: " + timestampRemoto + ")");
                
                // Enviar respuesta directa CON timestamp
                String respuesta = MENSAJE_RESPUESTA + ":" + nombreJugador + ":" + 
                                 puertoP2P + ":" + timestamp;
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
            System.err.println("[DESCUBRIMIENTO] Error procesando busqueda: " + e.getMessage());
        }
    }
    
    /**
     * Procesa respuesta con timestamp - MEJORADO
     */
    private void procesarRespuestaMejorada(String mensaje, InetAddress direccion) {
        try {
            String[] partes = mensaje.split(":");
            if (partes.length >= 4) {
                String nombre = partes[1];
                int puerto = Integer.parseInt(partes[2]);
                long timestampRemoto = Long.parseLong(partes[3]);
                String ip = direccion.getHostAddress();
                
                // No agregarse a si mismo
                if (nombre.equals(nombreJugador)) {
                    return;
                }
                
                // Verificar duplicados por nombre
                synchronized (jugadoresEncontrados) {
                    for (JugadorEncontrado j : jugadoresEncontrados) {
                        if (j.nombre.equals(nombre)) {
                            return;
                        }
                    }
                    
                    JugadorEncontrado jugador = new JugadorEncontrado(nombre, ip, puerto, timestampRemoto);
                    jugadoresEncontrados.add(jugador);
                    
                    System.out.println("[DESCUBRIMIENTO] >>> Jugador encontrado: " + nombre + 
                                     " @ " + ip + ":" + puerto + " (timestamp: " + timestampRemoto + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("[DESCUBRIMIENTO] Error procesando respuesta: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el timestamp de este jugador
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Clase interna para jugador encontrado - MEJORADA
     */
    public static class JugadorEncontrado {
        public String nombre;
        public String ip;
        public int puerto;
        public long timestamp;
        
        public JugadorEncontrado(String nombre, String ip, int puerto, long timestamp) {
            this.nombre = nombre;
            this.ip = ip;
            this.puerto = puerto;
            this.timestamp = timestamp;
        }
        
        @Override
        public String toString() {
            return nombre + " @ " + ip + ":" + puerto + " (ts:" + timestamp + ")";
        }
    }
}