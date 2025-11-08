package dispatcher.manejadores;

import dispatcher.ManejadorMensaje;
import red.MensajeJuego;
import red.ConexionPeer;
import controlador.ControladorRed;

/**
 * Manejador para mensajes de tipo CAMBIO_TURNO
 * Procesa cambios de turno entre jugadores
 */
public class ManejadorCambioTurno implements ManejadorMensaje {
    
    private ControladorRed controladorRed;
    
    public ManejadorCambioTurno(ControladorRed controladorRed) {
        this.controladorRed = controladorRed;
    }
    
    @Override
    public void manejar(MensajeJuego mensaje, ConexionPeer desde) {
        controladorRed.procesarCambioTurno(mensaje);
    }
}