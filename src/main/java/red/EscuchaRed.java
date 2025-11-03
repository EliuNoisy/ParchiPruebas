package red;

/**
 * Interfaz para escuchar eventos de red
 */
public interface EscuchaRed {
    
    /**
     * Se llama cuando se recibe un mensaje de un peer
     */
    void alRecibirMensaje(MensajeJuego mensaje, ConexionPeer desde);
    
    /**
     * Se llama cuando un peer se desconecta
     */
    void alDesconectarPeer(ConexionPeer peer);
}