package red;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Mensaje que se intercambia entre jugadores (peers)
 */
public class MensajeJuego implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum TipoMensaje {
        SALUDO,           // Presentación inicial
        MOVIMIENTO,       // Movimiento de ficha
        CAMBIO_TURNO,     // Cambio de turno
        TIRADA_DADO,      // Tirada de dado
        CAPTURA,          // Captura de ficha
        CHAT,             // Mensaje de chat
        JUGADOR_ENTRA,    // Jugador se une
        JUGADOR_SALE,     // Jugador se va
        ESTADO_JUEGO,     // Estado completo del juego
        INICIO_JUEGO,     // Inicio de partida
        FIN_JUEGO         // Fin de partida
    }
    
    private TipoMensaje tipo;
    private String emisor;
    private String contenido;
    private LocalDateTime marcaTiempo;
    
    public MensajeJuego(TipoMensaje tipo, String emisor, String contenido) {
        this.tipo = tipo;
        this.emisor = emisor;
        this.contenido = contenido;
        this.marcaTiempo = LocalDateTime.now();
    }
    
    public TipoMensaje getTipo() {
        return tipo;
    }
    
    public String getEmisor() {
        return emisor;
    }
    
    public String getContenido() {
        return contenido;
    }
    
    public LocalDateTime getMarcaTiempo() {
        return marcaTiempo;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s (%s): %s", 
            marcaTiempo.toString(), 
            tipo, 
            emisor, 
            contenido
        );
    }
}