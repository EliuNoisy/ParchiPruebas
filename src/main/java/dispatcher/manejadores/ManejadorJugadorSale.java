package dispatcher.manejadores;

import dispatcher.ManejadorMensaje;
import red.MensajeJuego;
import red.ConexionPeer;

/**
 * Manejador para mensajes de tipo JUGADOR_SALE
 * Procesa desconexiones de jugadores
 */
public class ManejadorJugadorSale implements ManejadorMensaje {
    
    @Override
    public void manejar(MensajeJuego mensaje, ConexionPeer desde) {
        System.out.println("[RED] Jugador desconectado: " + mensaje.getEmisor());
    }
}