package dispatcher.manejadores;

import dispatcher.ManejadorMensaje;
import red.MensajeJuego;
import red.ConexionPeer;
import controlador.ControladorRed;

/**
 * Manejador para mensajes de tipo MOVIMIENTO
 * Procesa movimientos de fichas en el tablero
 */
public class ManejadorMovimiento implements ManejadorMensaje {
    
    private ControladorRed controladorRed;
    
    public ManejadorMovimiento(ControladorRed controladorRed) {
        this.controladorRed = controladorRed;
    }
    
    @Override
    public void manejar(MensajeJuego mensaje, ConexionPeer desde) {
        controladorRed.procesarMovimiento(mensaje);
    }
}