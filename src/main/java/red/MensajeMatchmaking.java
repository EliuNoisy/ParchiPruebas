package red;

import java.io.Serializable;

/**
 * Mensajes para comunicacion con servidor de matchmaking
 */
public class MensajeMatchmaking implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Tipo {
        BUSCAR_PARTIDA,
        EMPAREJADO,
        INFO_CONEXION,
        CANCELAR_BUSQUEDA,
        ERROR
    }
    
    private Tipo tipo;
    private String nombreJugador;
    private String ipOponente;
    private int puertoOponente;
    private String nombreOponente;
    private int rolJugador;
    private String mensaje;
    
    public MensajeMatchmaking(Tipo tipo) {
        this.tipo = tipo;
    }
    
    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    
    public String getNombreJugador() { return nombreJugador; }
    public void setNombreJugador(String nombreJugador) { this.nombreJugador = nombreJugador; }
    
    public String getIpOponente() { return ipOponente; }
    public void setIpOponente(String ipOponente) { this.ipOponente = ipOponente; }
    
    public int getPuertoOponente() { return puertoOponente; }
    public void setPuertoOponente(int puertoOponente) { this.puertoOponente = puertoOponente; }
    
    public String getNombreOponente() { return nombreOponente; }
    public void setNombreOponente(String nombreOponente) { this.nombreOponente = nombreOponente; }
    
    public int getRolJugador() { return rolJugador; }
    public void setRolJugador(int rolJugador) { this.rolJugador = rolJugador; }
    
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}