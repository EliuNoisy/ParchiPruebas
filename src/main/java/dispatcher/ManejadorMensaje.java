package dispatcher;

import red.MensajeJuego;
import red.ConexionPeer;

/**
 * Interfaz base para todos los manejadores de mensajes
 * Cada tipo de mensaje tendra su propio manejador
 */
public interface ManejadorMensaje {
    
    /**
     * Procesa un mensaje especifico
     * @param mensaje Mensaje recibido
     * @param desde Peer que envio el mensaje
     */
    void manejar(MensajeJuego mensaje, ConexionPeer desde);
}