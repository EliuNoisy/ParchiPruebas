package red;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Sistema de descubrimiento 
 * Funciona entre diferentes subredes en la misma PC
 */
public class DescubrimientoRed {
    private static final int PUERTO_DESCUBRIMIENTO = 9999;
    private static final String MENSAJE_PING = "PARCHIS_PING";
    private static final String MENSAJE_PONG = "PARCHIS_PONG";
    
    private String nombreJugador;
    private int puertoP2P;
    private long timestamp;
    private ServerSocket serverDescubrimiento;
    private boolean escuchando;
    private Thread hiloEscucha;
    private List<JugadorEncontrado> jugadoresEncontrados;
    private ExecutorService executor;
    private Set<String> misIPs;
    
    public DescubrimientoRed(String nombreJugador, int puertoP2P) {
        this.nombreJugador = nombreJugador;
        this.puertoP2P = puertoP2P;
        this.timestamp = System.currentTimeMillis();
        this.jugadoresEncontrados = new CopyOnWriteArrayList<>();
        this.escuchando = false;
        
        // Reducir threads para evitar sobrecarga
        int maxThreads = Math.min(Runtime.getRuntime().availableProcessors() * 2, 50);
        this.executor = Executors.newFixedThreadPool(maxThreads);
        this.misIPs = new HashSet<>();
    }
    
    /**
     *  Obtiene TODAS las IPs locales de todas las interfaces
     */
    private List<InterfazRed> obtenerTodasLasInterfaces() {
        List<InterfazRed> interfaces = new ArrayList<>();
        
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                
                // Solo interfaces activas y no loopback
                if (!ni.isUp() || ni.isLoopback()) continue;
                
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    
                    // Solo IPv4
                    if (!(addr instanceof Inet4Address)) continue;
                    
                    String ip = addr.getHostAddress();
                    misIPs.add(ip);
                    
                    // Calcular subnet
                    String[] octetos = ip.split("\\.");
                    if (octetos.length == 4) {
                        String baseIP = octetos[0] + "." + octetos[1] + "." + octetos[2] + ".";
                        
                        InterfazRed interfaz = new InterfazRed();
                        interfaz.nombre = ni.getDisplayName();
                        interfaz.ip = ip;
                        interfaz.baseSubnet = baseIP;
                        interfaces.add(interfaz);
                        
                        System.out.println("[DESCUBRIMIENTO] Interfaz detectada: " + interfaz.nombre);
                        System.out.println("                 IP: " + ip);
                        System.out.println("                 Subnet: " + baseIP + "0-255");
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("[DESCUBRIMIENTO] Error obteniendo interfaces: " + e.getMessage());
        }
        
        // Si no encontro nada, intentar metodo alternativo
        if (interfaces.isEmpty()) {
            try {
                String ipLocal = InetAddress.getLocalHost().getHostAddress();
                String[] octetos = ipLocal.split("\\.");
                if (octetos.length == 4) {
                    String baseIP = octetos[0] + "." + octetos[1] + "." + octetos[2] + ".";
                    InterfazRed interfaz = new InterfazRed();
                    interfaz.nombre = "Default";
                    interfaz.ip = ipLocal;
                    interfaz.baseSubnet = baseIP;
                    interfaces.add(interfaz);
                    misIPs.add(ipLocal);
                }
            } catch (Exception e) {
                System.err.println("[DESCUBRIMIENTO] Error metodo alternativo: " + e.getMessage());
            }
        }
        
        return interfaces;
    }
    
    /**
     * Inicia modo de respuesta - servidor TCP que escucha conexiones
     */
    public void iniciarModoRespuesta() {
        hiloEscucha = new Thread(() -> {
            try {
                serverDescubrimiento = new ServerSocket(PUERTO_DESCUBRIMIENTO);
                serverDescubrimiento.setSoTimeout(1000);
                escuchando = true;
                
                System.out.println("[DESCUBRIMIENTO] Servidor escuchando en puerto " + PUERTO_DESCUBRIMIENTO);
                
                while (escuchando && !Thread.currentThread().isInterrupted()) {
                    try {
                        Socket cliente = serverDescubrimiento.accept();
                        executor.execute(() -> manejarPing(cliente));
                    } catch (SocketTimeoutException e) {
                        // Normal, continuar
                    }
                }
            } catch (Exception e) {
                if (escuchando) {
                    System.err.println("[DESCUBRIMIENTO] Error en servidor: " + e.getMessage());
                }
            } finally {
                cerrarServidor();
            }
        });
        hiloEscucha.start();
    }
    
    /**
     * Maneja solicitud de ping entrante - MEJORADO
     */
    private void manejarPing(Socket cliente) {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true);
            
            String mensaje = entrada.readLine();
            
            if (mensaje != null && mensaje.startsWith(MENSAJE_PING)) {
                String[] partes = mensaje.split(":");
                if (partes.length >= 4) {
                    String nombreRemoto = partes[1];
                    int puertoRemoto = Integer.parseInt(partes[2]);
                    long timestampRemoto = Long.parseLong(partes[3]);
                    
                    // No responderse a si mismo
                    if (!nombreRemoto.equals(nombreJugador)) {
                        System.out.println("[DESCUBRIMIENTO] >>> Ping recibido de: " + nombreRemoto + 
                                         " (desde " + cliente.getInetAddress().getHostAddress() + ")");
                        
                        // Responder inmediatamente
                        String respuesta = MENSAJE_PONG + ":" + nombreJugador + ":" + 
                                         puertoP2P + ":" + timestamp;
                        salida.println(respuesta);
                        salida.flush();
                        
                        System.out.println("[DESCUBRIMIENTO] >>> PONG enviado a " + nombreRemoto);
                    }
                }
            }
            
            cliente.close();
        } catch (Exception e) {
            // Ignorar errores de conexiones individuales
        }
    }
    
    /**
     * Busca jugadores en TODAS las subredes detectadas
     */
    public List<JugadorEncontrado> buscarJugadores(int tiempoEspera) {
        jugadoresEncontrados.clear();
        
        List<InterfazRed> interfaces = obtenerTodasLasInterfaces();
        
        if (interfaces.isEmpty()) {
            System.err.println("[DESCUBRIMIENTO] No se detectaron interfaces de red activas");
            return new ArrayList<>();
        }
        
        System.out.println("\n[DESCUBRIMIENTO] ==============================================");
        System.out.println("[DESCUBRIMIENTO] ESCANEANDO " + interfaces.size() + " SUBREDES");
        System.out.println("[DESCUBRIMIENTO] Timestamp: " + timestamp);
        System.out.println("[DESCUBRIMIENTO] ==============================================\n");
        
        // Mostrar todas las IPs propias
        System.out.println("[DESCUBRIMIENTO] Mis IPs:");
        for (String ip : misIPs) {
            System.out.println("                 - " + ip);
        }
        System.out.println();
        
        // Escanear todas las subredes en paralelo
        CountDownLatch latch = new CountDownLatch(interfaces.size() * 255);
        long inicio = System.currentTimeMillis();
        
        for (InterfazRed interfaz : interfaces) {
            String baseIP = interfaz.baseSubnet;
            
            System.out.println("[DESCUBRIMIENTO] Escaneando subnet: " + baseIP + "0-255");
            
            for (int i = 1; i <= 255; i++) {
                String ip = baseIP + i;
                
                // Saltar mis propias IPs
                if (misIPs.contains(ip)) {
                    latch.countDown();
                    continue;
                }
                
                final String ipFinal = ip;
                executor.execute(() -> {
                    try {
                        intentarConectar(ipFinal);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }
        
        try {
            boolean completado = latch.await(tiempoEspera, TimeUnit.SECONDS);
            
            if (!completado) {
                System.out.println("[DESCUBRIMIENTO] Timeout de escaneo alcanzado");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long duracion = (System.currentTimeMillis() - inicio) / 1000;
        
        System.out.println("\n[DESCUBRIMIENTO] ==============================================");
        System.out.println("[DESCUBRIMIENTO] Escaneo completado en " + duracion + " segundos");
        System.out.println("[DESCUBRIMIENTO] Jugadores encontrados: " + jugadoresEncontrados.size());
        System.out.println("[DESCUBRIMIENTO] ==============================================\n");
        
        for (JugadorEncontrado j : jugadoresEncontrados) {
            System.out.println("  >>> " + j.nombre + " @ " + j.ip + ":" + j.puerto + 
                             " (timestamp: " + j.timestamp + ")");
        }
        
        return new ArrayList<>(jugadoresEncontrados);
    }
    
    /**
     * Intenta conectar a una IP especifica
     */
    private void intentarConectar(String ip) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, PUERTO_DESCUBRIMIENTO), 500);
            
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            String mensaje = MENSAJE_PING + ":" + nombreJugador + ":" + puertoP2P + ":" + timestamp;
            salida.println(mensaje);
            salida.flush();
            
            socket.setSoTimeout(1000);
            String respuesta = entrada.readLine();
            
            if (respuesta != null && respuesta.startsWith(MENSAJE_PONG)) {
                procesarRespuesta(respuesta, ip);
            }
            
        } catch (Exception e) {
            // IP no responde, normal durante escaneo
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                // Ignorar
            }
        }
    }
    
    /**
     * Procesa respuesta PONG
     */
    private void procesarRespuesta(String mensaje, String ip) {
        try {
            String[] partes = mensaje.split(":");
            if (partes.length >= 4) {
                String nombre = partes[1];
                int puerto = Integer.parseInt(partes[2]);
                long timestampRemoto = Long.parseLong(partes[3]);
                
                // No agregarse a si mismo
                if (nombre.equals(nombreJugador)) {
                    return;
                }
                
                // Verificar duplicados
                synchronized (jugadoresEncontrados) {
                    for (JugadorEncontrado j : jugadoresEncontrados) {
                        if (j.nombre.equals(nombre)) {
                            return;
                        }
                    }
                    
                    JugadorEncontrado jugador = new JugadorEncontrado(nombre, ip, puerto, timestampRemoto);
                    jugadoresEncontrados.add(jugador);
                    
                    System.out.println("\n[DESCUBRIMIENTO] *** JUGADOR ENCONTRADO ***");
                    System.out.println("                 Nombre: " + nombre);
                    System.out.println("                 IP: " + ip);
                    System.out.println("                 Puerto P2P: " + puerto);
                    System.out.println("                 Timestamp: " + timestampRemoto);
                    System.out.println();
                }
            }
        } catch (Exception e) {
            System.err.println("[DESCUBRIMIENTO] Error procesando respuesta: " + e.getMessage());
        }
    }
    
    /**
     * Detiene el modo de respuesta
     */
    public void detenerModoRespuesta() {
        escuchando = false;
        
        if (hiloEscucha != null) {
            hiloEscucha.interrupt();
            try {
                hiloEscucha.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        cerrarServidor();
        executor.shutdown();
        
        System.out.println("[DESCUBRIMIENTO] Modo respuesta detenido");
    }
    
    /**
     * Cierra el servidor de descubrimiento
     */
    private void cerrarServidor() {
        if (serverDescubrimiento != null && !serverDescubrimiento.isClosed()) {
            try {
                serverDescubrimiento.close();
            } catch (IOException e) {
                // Ignorar
            }
        }
    }
    
    /**
     * Obtiene el timestamp de este jugador
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Clase interna para representar una interfaz de red
     */
    private static class InterfazRed {
        String nombre;
        String ip;
        String baseSubnet;
    }
    
    /**
     * Clase interna para jugador encontrado
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