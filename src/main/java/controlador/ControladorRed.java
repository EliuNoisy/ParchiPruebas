package controlador;

import modelo.*;
import red.*;

/**
 * Controlador de red con timeouts mejorados
 * VERSION FINAL CORREGIDA
 */
public class ControladorRed implements EscuchaRed {
    private P2PNetworkManager gestorRed;
    private Tablero tablero;
    private int jugadorLocalId;
    private boolean esAnfitrion;
    private ControladorPartida controladorPartida;
    
    private String nombreOponenteRecibido = null;
    private boolean inicioPartidaRecibido = false;
    private final Object lockNombre = new Object();
    private final Object lockInicio = new Object();
    
    public ControladorRed(String nombreJugador, int puerto, Tablero tablero, int jugadorLocalId) {
        int puertoLocal = puerto;
        if (jugadorLocalId == 2) {
            puertoLocal = puerto + 1;
        }
        
        this.gestorRed = new P2PNetworkManager(nombreJugador, puertoLocal);
        this.tablero = tablero;
        this.jugadorLocalId = jugadorLocalId;
        this.gestorRed.setEscuchaRed(this);
    }
    
    /**
     * Espera a recibir el nombre del oponente por red
     * Timeout de 30 segundos con progreso mejorado
     */
    public String esperarNombreOponente() {
        synchronized (lockNombre) {
            try {
                long tiempoInicio = System.currentTimeMillis(); // ✓ CORREGIDO
                long timeout = 30000;
                long ultimoMensaje = 0;
                
                while (nombreOponenteRecibido == null) {
                    long transcurrido = System.currentTimeMillis() - tiempoInicio;
                    long restante = timeout - transcurrido;
                    
                    if (restante <= 0) {
                        System.err.println("[RED] Timeout: No se recibio el nombre del oponente");
                        return null;
                    }
                    
                    if (transcurrido - ultimoMensaje >= 5000) {
                        System.out.println("[RED] Esperando... (" + (restante/1000) + "s restantes)");
                        ultimoMensaje = transcurrido;
                    }
                    
                    lockNombre.wait(1000);
                }
                
                return nombreOponenteRecibido;
                
            } catch (InterruptedException e) {
                System.err.println("[RED] Error esperando nombre: " + e.getMessage());
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }
    
    /**
     * Envia senal de inicio de partida (solo anfitrion)
     */
    public void enviarInicioPartida() {
        if (!esAnfitrion) return;
        
        System.out.println("[RED] Enviando senal de inicio de partida...");
        gestorRed.enviarInicioPartida();
    }
    
    /**
     * Espera senal de inicio de partida (solo cliente)
     * Timeout de 60 segundos con progreso mejorado
     */
    public void esperarInicioPartida() {
        synchronized (lockInicio) {
            try {
                long tiempoInicio = System.currentTimeMillis(); 
                long timeout = 60000;
                long ultimoMensaje = 0;
                
                while (!inicioPartidaRecibido) {
                    long transcurrido = System.currentTimeMillis() - tiempoInicio;
                    long restante = timeout - transcurrido;
                    
                    if (restante <= 0) {
                        System.err.println("[RED] Timeout: No se recibio senal de inicio");
                        return;
                    }
                    
                    if (transcurrido - ultimoMensaje >= 5000) {
                        System.out.println("[RED] Esperando inicio... (" + (restante/1000) + "s restantes)");
                        ultimoMensaje = transcurrido;
                    }
                    
                    lockInicio.wait(1000);
                }
                
            } catch (InterruptedException e) {
                System.err.println("[RED] Error esperando inicio: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Inicia como anfitrion
     */
    public void iniciarComoAnfitrion() throws Exception {
        esAnfitrion = true;
        gestorRed.iniciarServidor();
    }
    
    /**
     * Se une a una partida
     */
    public boolean unirseAPartida(String ipAnfitrion, int puertoAnfitrion) {
        try {
            gestorRed.iniciarServidor();
            Thread.sleep(1000);
            
            System.out.println("[RED] Conectando a " + ipAnfitrion + ":" + puertoAnfitrion);
            
            boolean conectado = gestorRed.conectarAPeer(ipAnfitrion, puertoAnfitrion);
            
            if (conectado) {
                esAnfitrion = false;
                Thread.sleep(500);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("[RED] Error en unirseAPartida: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Envia un movimiento de ficha
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
     * Envia tirada de dado
     */
    public void enviarTiradaDado(int valor) {
        gestorRed.enviarTiradaDado(valor);
    }
    
    /**
     * Envia mensaje de chat
     */
    public void enviarMensajeChat(String texto) {
        gestorRed.enviarMensajeChat(texto);
    }
    
    @Override
    public void alRecibirMensaje(MensajeJuego mensaje, ConexionPeer desde) {
        System.out.println("[RED] <- " + mensaje.getTipo() + " de " + mensaje.getEmisor());
        
        switch (mensaje.getTipo()) {
            case SALUDO:
                procesarSaludo(mensaje);
                break;
                
            case INICIO_JUEGO:
                procesarInicioJuego(mensaje);
                break;
                
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
                
            case JUGADOR_SALE:
                System.out.println("[RED] Jugador desconectado: " + mensaje.getEmisor());
                break;
                
            default:
                System.out.println("[RED] Mensaje no manejado: " + mensaje.getTipo());
        }
    }
    
    @Override
    public void alDesconectarPeer(ConexionPeer peer) {
        System.out.println("[RED] Peer desconectado: " + peer.getNombrePeer());
    }
    
    /**
     * Procesa mensaje de saludo
     */
    private void procesarSaludo(MensajeJuego mensaje) {
        synchronized (lockNombre) {
            nombreOponenteRecibido = mensaje.getEmisor();
            System.out.println("[RED] Oponente identificado: " + nombreOponenteRecibido);
            lockNombre.notifyAll();
        }
    }
    
    /**
     * Procesa senal de inicio de juego
     */
    private void procesarInicioJuego(MensajeJuego mensaje) {
        synchronized (lockInicio) {
            inicioPartidaRecibido = true;
            System.out.println("[RED] Senal de inicio recibida");
            lockInicio.notifyAll();
        }
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
            
            if (jugadorId != jugadorLocalId) {
                System.out.println("\n[RED] Procesando movimiento del oponente...");
                if (controladorPartida != null) {
                    controladorPartida.aplicarMovimientoRemoto(jugadorId, fichaId, dado);
                    System.out.println("[RED] Movimiento sincronizado");
                }
            }
            
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
            
            if (controladorPartida != null) {
                controladorPartida.aplicarCambioTurnoRemoto(jugadorId);
                
                if (jugadorId == jugadorLocalId) {
                    System.out.println("\n================================================");
                    System.out.println("              ES TU TURNO!");
                    System.out.println("================================================");
                } else {
                    System.out.println("\n[RED] Turno del oponente iniciado");
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
        try {
            int valor = Integer.parseInt(mensaje.getContenido());
            System.out.println("[RED] " + mensaje.getEmisor() + " lanzo el dado: " + valor);
        } catch (Exception e) {
            System.err.println("[RED] Error procesando tirada de dado: " + e.getMessage());
        }
    }
    
    /**
     * Procesa mensaje de chat
     */
    private void procesarChat(MensajeJuego mensaje) {
        System.out.println("\n[" + mensaje.getEmisor() + "]: " + mensaje.getContenido());
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
    
    /**
     * Obtiene peers conectados
     * Util para verificar estado de conexion
     */
    public java.util.List<ConexionPeer> getPeersConectados() {
        return gestorRed.getPeersConectados();
    }
}